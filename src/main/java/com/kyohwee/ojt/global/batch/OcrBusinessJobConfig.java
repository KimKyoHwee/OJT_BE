package com.kyohwee.ojt.global.batch;

import com.kyohwee.ojt.domain.dto.BusinessDocument;
import com.kyohwee.ojt.domain.dto.ClovaOcrResponseDto;
import com.kyohwee.ojt.domain.entity.BusinessDocumentEntity;
import com.kyohwee.ojt.domain.repository.BusinessDocumentRepository;
import com.kyohwee.ojt.domain.service.ocr.BusinessVerificationService;
import com.kyohwee.ojt.domain.service.ocr.ClovaOcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class OcrBusinessJobConfig {

    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final ClovaOcrService ocrService;
    private final BusinessVerificationService verificationService;
    private final BusinessDocumentRepository documentRepository;

    // ── 1) OCR 추출 Step ───────────────────────────────────────────
    @Bean
    public Step ocrStep() {
        return new StepBuilder("ocrStep", jobRepository)
                .<BusinessDocumentEntity, BusinessDocumentEntity>chunk(CHUNK_SIZE, txManager)
                .reader(ocrItemReader())
                .processor(ocrItemProcessor())
                .writer(ocrItemWriter())
                .listener(new StepExecutionListenerSupport() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("[OCR Step] Starting step: {}", stepExecution.getStepName());
                    }
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("[OCR Step] Completed step: {} with status {}",
                                stepExecution.getStepName(), stepExecution.getStatus());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public RepositoryItemReader<BusinessDocumentEntity> ocrItemReader() {
        return new RepositoryItemReaderBuilder<BusinessDocumentEntity>()
                .name("ocrItemReader")
                .repository(documentRepository)
                .methodName("findByOcrProcessedFalseOrderByIdAsc")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<BusinessDocumentEntity, BusinessDocumentEntity> ocrItemProcessor() {
        return entity -> {
            ClovaOcrResponseDto dto = ocrService.extractText(entity.getImageUrl());
            System.out.println("dto.toString() = " + dto.toString());
            // Document OCR v2의 bizLicense 결과에서 텍스트 추출
            String text = dto.getImages().stream()
                    .flatMap(imgRes -> {
                        if (imgRes.getBizLicense() == null || imgRes.getBizLicense().getResult() == null) {
                            return Stream.<ClovaOcrResponseDto.Field>empty();
                        }
                        ClovaOcrResponseDto.Result res = imgRes.getBizLicense().getResult();
                        return Stream.of(
                                        res.getCompanyName(),
                                        res.getRepName(),
                                        res.getRegisterNumber(),
                                        res.getOpenDate(),
                                        res.getIssuanceDate()
                                )
                                .filter(Objects::nonNull)
                                .flatMap(List::stream);
                    })
                    .map(ClovaOcrResponseDto.Field::getText)
                    .collect(Collectors.joining(" "));
            entity.setOcrResult(text);
            entity.setOcrProcessed(true);
            return entity;
        };
    }

    @Bean
    public ItemWriter<BusinessDocumentEntity> ocrItemWriter() {
        return items -> documentRepository.saveAll(items);
    }

    // ── 2) 사업자 진위검증 Step ────────────────────────────────────
    @Bean
    public Step verifyStep() {
        return new StepBuilder("verifyStep", jobRepository)
                .<BusinessDocumentEntity, BusinessDocumentEntity>chunk(CHUNK_SIZE, txManager)
                .reader(verifyItemReader())
                .processor(verifyItemProcessor())
                .writer(verifyItemWriter())
                .listener(new StepExecutionListenerSupport() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("[Verify Step] Starting step: {}", stepExecution.getStepName());
                    }
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("[Verify Step] Completed step: {} with status {}",
                                stepExecution.getStepName(), stepExecution.getStatus());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public RepositoryItemReader<BusinessDocumentEntity> verifyItemReader() {
        return new RepositoryItemReaderBuilder<BusinessDocumentEntity>()
                .name("verifyItemReader")
                .repository(documentRepository)
                .methodName("findByOcrProcessedTrueAndVerifiedFalseOrderByIdAsc")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<BusinessDocumentEntity, BusinessDocumentEntity> verifyItemProcessor() {
        return entity -> {
            BusinessDocument doc = new BusinessDocument();
            doc.setId(entity.getId());
            doc.setImageUrl(entity.getImageUrl());
            doc.setOcrResult(entity.getOcrResult());
            String status = verificationService.checkBusiness(doc);
            entity.setVerificationStatus(status);
            entity.setVerificationMessage(doc.getVerificationMessage());
            entity.setVerified(true);
            entity.setSuccess("01".equals(status));
            return entity;
        };
    }

    @Bean
    public ItemWriter<BusinessDocumentEntity> verifyItemWriter() {
        return items -> documentRepository.saveAll(items);
    }

    // ── 3) Job 정의 ─────────────────────────────────────────────────
    @Bean("ocrBusinessCheckJob")
    public Job ocrBusinessCheckJob(Step ocrStep, Step verifyStep) {
        return new JobBuilder("ocrBusinessCheckJob", jobRepository)
                .start(ocrStep)
                .next(verifyStep)
                .build();
    }
}

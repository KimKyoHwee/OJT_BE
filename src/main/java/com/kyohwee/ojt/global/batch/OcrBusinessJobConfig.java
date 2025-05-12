package com.kyohwee.ojt.global.batch;

import com.kyohwee.ojt.domain.dto.BusinessDocument;
import com.kyohwee.ojt.domain.dto.ClovaOcrResponseDto;
import com.kyohwee.ojt.domain.dto.ValidateResponse;
import com.kyohwee.ojt.domain.entity.BusinessDocumentEntity;
import com.kyohwee.ojt.domain.entity.OcrResultEntity;
import com.kyohwee.ojt.domain.entity.VerificationResultEntity;
import com.kyohwee.ojt.domain.repository.BusinessDocumentRepository;
import com.kyohwee.ojt.domain.repository.OcrResultEntityRepository;
import com.kyohwee.ojt.domain.repository.VerificationResultEntityRepository;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
    private final OcrResultEntityRepository ocrResultRepository;
    private final VerificationResultEntityRepository verificationResultRepository;

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
            // 1) OCR 호출하고, 첫 번째 이미지 응답·결과 꺼내기
            ClovaOcrResponseDto dto = ocrService.extractText(entity.getImageUrl());
            ClovaOcrResponseDto.ImageResponse mainImageResponse = dto.getImages().get(0);
            ClovaOcrResponseDto.Result    mainResult        = mainImageResponse.getBizLicense().getResult();

            // 2) OcrResultEntity 채우기
            OcrResultEntity ocr = OcrResultEntity.from(dto);
            ocrResultRepository.save(ocr);
            entity.setOcrResultEntity(ocr);


            // 3) 전체 텍스트 조합 (예: companyName, repName, registerNumber, openDate, issuanceDate)
            String aggregatedText = dto.getImages().stream()
                    .flatMap(imageResp -> {
                        if (imageResp.getBizLicense() == null
                                || imageResp.getBizLicense().getResult() == null) {
                            return Stream.<ClovaOcrResponseDto.Field>empty();
                        }
                        ClovaOcrResponseDto.Result r = imageResp.getBizLicense().getResult();
                        return Stream.of(
                                        r.getCompanyName(),
                                        r.getRepName(),
                                        r.getRegisterNumber(),
                                        r.getOpenDate(),
                                        r.getIssuanceDate()
                                )
                                .filter(Objects::nonNull)
                                .flatMap(List::stream);
                    })
                    .map(ClovaOcrResponseDto.Field::getText)
                    .collect(Collectors.joining(" "));

            // (선택) aggregatedText 를 OcrResultEntity 에 저장하도록 메서드가 있다면 호출
            // ocr.setAggregatedText(aggregatedText);

            // 4) 1:1 매핑하고 프로세스 완료 표시
            entity.setOcrResult(aggregatedText);
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
            log.info("[Verify] 시작: documentId={}", entity.getId());
            try {
                // 1) OCR 결과 조회
                log.info("[Verify] OCR 결과 조회 시도: imageUrl={}", entity.getImageUrl());
                OcrResultEntity ocr = entity.getOcrResultEntity();
                if (ocr == null) {
                    throw new IllegalStateException("[Verify][ERROR] OCR 결과가 없습니다: documentId=" + entity.getId());
                }
                log.info("[Verify] OCR 결과 로드 완료: registerNumber={}, openDate={}, repName={}",
                        ocr.getRegisterNumber(), ocr.getOpenDate(), ocr.getRepName());

                // 2) BusinessDocument DTO 세팅
                BusinessDocument doc = new BusinessDocument();
                doc.setId(entity.getId());
                doc.setBusinessNumber(ocr.getRegisterNumber());
                doc.setStartDate(ocr.getOpenDate());
                doc.setOwnerName(ocr.getRepName());
                log.info("[Verify] BusinessDocument 준비: b_no={}, start_dt={}, p_nm={}",
                        doc.getBusinessNumber(), doc.getStartDate(), doc.getOwnerName());

                // 3) 진위확인 API 호출
                log.info("[Verify] 진위확인 API 호출 전");
                ValidateResponse.BusinessData data = verificationService.checkBusiness(doc);
                String status = data != null ? data.getValid() : null;
                log.info("[Verify] 진위확인 API 호출 후: status={}, message={}",
                        status, doc.getVerificationMessage());

                // 4) Entity에 결과 반영
                entity.setVerificationStatus(status);
                entity.setVerificationMessage(doc.getVerificationMessage());
                boolean isValid = "01".equals(status);  //01 status값이 와야 정상
                entity.setVerified(isValid);
                entity.setSuccess(isValid);
                log.info("[Verify] Entity 반영 완료: verified={}, success={}",
                        entity.isVerified(), entity.isSuccess());

                // 5) VerificationResultEntity 생성·저장 후 연관 설정
                if (data != null && data.getStatus() != null) {
                    VerificationResultEntity vr = VerificationResultEntity.from(data);
                    vr = verificationResultRepository.save(vr);
                    entity.setVerificationResultEntity(vr);
                    log.info("[Verify] VerificationResultEntity 저장 완료: id={}", vr.getId());
                }

                return entity;
            } catch (Exception ex) {
                log.error("[Verify][Exception] documentId={} 처리 중 오류 발생", entity.getId(), ex);
                throw ex;
            }
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

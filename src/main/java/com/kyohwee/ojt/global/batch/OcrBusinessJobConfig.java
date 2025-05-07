package com.kyohwee.ojt.global.batch;

import com.kyohwee.ojt.domain.dto.BusinessDocument;
import com.kyohwee.ojt.domain.dto.ClovaOcrResponseDto;
import com.kyohwee.ojt.domain.entity.BusinessDocumentEntity;
import com.kyohwee.ojt.domain.repository.BusinessDocumentRepository;
import com.kyohwee.ojt.domain.service.ocr.BusinessVerificationService;
import com.kyohwee.ojt.domain.service.ocr.ClovaOcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
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
                .build();
    }

    // OCR이 수행되지 않은 레코드를 ID 오름차순으로 1개씩 읽어오기
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

    // DTO 안의 모든 inferText 필드를 꺼내 하나의 문자열로 합친 뒤
    //entity.setOcrResult(text) 에 저장하고,
    //entity.setOcrProcessed(true) 로 “OCR 완료” 상태로 표시합니다.
    @Bean
    public ItemProcessor<BusinessDocumentEntity, BusinessDocumentEntity> ocrItemProcessor() {
        return entity -> {
            // 1) DTO 전체를 받습니다.
            ClovaOcrResponseDto dto = ocrService.extractText(entity.getImageUrl());

            // 2) DTO.images 안의 모든 Field.inferText 를 모아 하나의 문자열로 만듭니다.
            String text = dto.getImages().stream()
                    .flatMap(img -> img.getFields().stream())
                    .map(field -> field.getInferText())
                    .collect(Collectors.joining(" "));

            // 3) 엔티티에 저장
            entity.setOcrResult(text);
            entity.setOcrProcessed(true);
            return entity;
        };
    }


    //처리된 BusinessDocumentEntity 객체들을 한꺼번에
    //documentRepository.saveAll(...) 로 DB에 반영
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
                .build();
    }

    // OCR은 됐지만, 검증은 안된 레코드를 ID 오름차순으로 1개씩 읽어오기
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

    //Entity -> DTO 변환 TODO: OCR결과에서 추출된 사업자번호, 개업일자, 대표자명도 doc에 세팅
    @Bean
    public ItemProcessor<BusinessDocumentEntity, BusinessDocumentEntity> verifyItemProcessor() {
        return entity -> {
            // 1) Entity → DTO 변환
            BusinessDocument doc = new BusinessDocument();
            doc.setId(entity.getId());
            doc.setImageUrl(entity.getImageUrl());
            doc.setOcrResult(entity.getOcrResult());
            // TODO: OCR 결과에서 추출된 사업자번호, 개업일자, 대표자명도 doc에 세팅
            // 예) doc.setBusinessNumber(parseBizNo(entity.getOcrResult()));
            //     doc.setStartDate(parseStartDt(entity.getOcrResult()));
            //     doc.setOwnerName(parseOwner(entity.getOcrResult()));

            // 2) 검증 서비스 호출
            String status = verificationService.checkBusiness(doc);

            // 3) DTO → Entity 결과 반영
            entity.setVerificationStatus(status);
            entity.setVerificationMessage(doc.getVerificationMessage()); // service에서 세팅됐다면
            entity.setVerified(true);
            entity.setSuccess("01".equals(status)); // “01” 정상 코드라고 가정

            return entity;
        };
    }


    //변경된 BusinessDocumentEntity 객체들을 한꺼번에 DB에 저장
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

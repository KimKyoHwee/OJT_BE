package com.kyohwee.ojt.global.batch;


import com.kyohwee.ojt.domain.service.ClovaOcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class OcrBusinessJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final ClovaOcrService ocrService;
    private final BusinessVerificationService verificationService;

    // ▶ 1) “OCR 추출” Step 정의
    @Bean
    public Step ocrStep() {
        return new StepBuilder("ocrStep", jobRepository)
                .<BusinessDocument, BusinessDocument>chunk(10, txManager)
                .reader(ocrItemReader())
                .processor(ocrItemProcessor())
                .writer(ocrItemWriter())
                .build();
    }

    @Bean
    public ItemReader<BusinessDocument> ocrItemReader() {
        // 예: QuartzBatchJob이 넘겨준 scheduleId로 DB에서 처리할 BusinessDocument를 가져오는 Reader
        return new BusinessDocumentReader();
    }

    @Bean
    public ItemProcessor<BusinessDocument, BusinessDocument> ocrItemProcessor() {
        return document -> {
            // 이미지 URL 등 document 정보를 OCR에 보내고, 결과를 document에 담아 리턴
            var ocrResult = ocrService.extractText(document.getImageUrl());
            document.setOcrResult(ocrResult);
            return document;
        };
    }

    @Bean
    public ItemWriter<BusinessDocument> ocrItemWriter() {
        return items -> {
            // OCR 결과를 DB에 저장하거나, 다음 스텝에서 사용할 수 있도록 넘겨줍니다.
            // (여기서는 단순히 로그로 남기는 예)
            items.forEach(doc -> System.out.println("OCR 완료: " + doc.getOcrResult()));
        };
    }


    // ▶ 2) “사업자 상태 조회” Step 정의
    @Bean
    public Step verifyStep() {
        return new StepBuilder("verifyStep", jobRepository)
                .<BusinessDocument, BusinessDocument>chunk(10, txManager)
                .reader(verifyItemReader())
                .processor(verifyItemProcessor())
                .writer(verifyItemWriter())
                .build();
    }

    @Bean
    public ItemReader<BusinessDocument> verifyItemReader() {
        // ocrStep이 기록한 OCR 결과가 담긴 document를 읽어 옵니다.
        return new BusinessDocumentOcrResultReader();
    }

    @Bean
    public ItemProcessor<BusinessDocument, BusinessDocument> verifyItemProcessor() {
        return document -> {
            // OCR로 추출한 번호를 기반으로 사업자 상태 조회 API 호출
            var status = verificationService.checkBusiness(document.getOcrResult());
            document.setVerificationStatus(status);
            return document;
        };
    }

    @Bean
    public ItemWriter<BusinessDocument> verifyItemWriter() {
        return items -> {
            // 최종 결과를 DB에 저장하거나 로그로 남깁니다.
            items.forEach(doc -> System.out.println("검증 결과: " + doc.getVerificationStatus()));
        };
    }


    // ▶ 3) Job 정의 (두 Step을 순차 실행)
    @Bean("ocrBusinessCheckJob")
    public Job ocrBusinessCheckJob(Step ocrStep, Step verifyStep) {
        return new JobBuilder("ocrBusinessCheckJob", jobRepository)
                .start(ocrStep)
                .next(verifyStep)
                .build();
    }
}

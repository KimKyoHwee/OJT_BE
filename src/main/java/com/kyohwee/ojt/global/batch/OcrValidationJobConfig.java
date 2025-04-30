package com.kyohwee.ojt.global.batch;

import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class OcrValidationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "ocrValidationJob") // Quartz에서 이 이름으로 찾음
    public Job ocrValidationJob() {
        return new JobBuilder("ocrValidationJob", jobRepository)
                .start(ocrExtractStep())
                .next(bizValidationStep())
                .build();
    }

    @Bean
    public Step ocrExtractStep() {
        return new StepBuilder("ocrExtractStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 이미지 경로 가져오기 (scheduleId 또는 jobId 기반)
                    // CLOVA OCR 요청 → 결과 저장
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step bizValidationStep() {
        return new StepBuilder("bizValidationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // OCR 결과 기반으로 사업자 상태조회 API 호출
                    // 결과 로깅/저장
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}


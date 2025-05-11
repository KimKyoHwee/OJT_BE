package com.kyohwee.ojt.global.batch;

import com.kyohwee.ojt.domain.entity.BatchJob;
import com.kyohwee.ojt.domain.entity.BatchLog;
import com.kyohwee.ojt.domain.repository.BatchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BatchJobExecutor {

    private final RestTemplate restTemplate;
    private final BatchLogRepository logRepository;

    // Spring Batch 실행을 위해 추가 주입
    private final ApplicationContext applicationContext;
    private final JobLauncher jobLauncher;

    /**
     * 1) 단순 HTTP 호출 스케줄용
     */
    public void execute(BatchJob job) {
        BatchLog log = new BatchLog();
        log.setBatchJob(job);
        log.setExecutedAt(LocalDateTime.now());

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(job.getEndpointUrl(), null, String.class);
            log.setStatus("SUCCESS");
            log.setResponse(resp.getBody());
        } catch (Exception ex) {
            log.setStatus("FAIL");
            log.setResponse(ex.getMessage());
        }

        logRepository.save(log);
    }

    /**
     * 2) Spring Batch JobLauncher 사용
     */
    public void executeSpringBatch(BatchJob batchJob) {
        BatchLog log = new BatchLog();
        log.setBatchJob(batchJob);
        log.setExecutedAt(LocalDateTime.now());

        try {
            // 1) Job 이름을 @Bean(name="...") 으로 정확히 설정한 뒤 가져옵니다.
            String beanName = batchJob.getName();
            Job job = applicationContext.getBean(beanName, Job.class);

            // 2) Job 실행을 위한 JobParameters 설정
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("jobId", batchJob.getId())
                    .addLong("timestamp", System.currentTimeMillis()) // 유니크 값
                    .toJobParameters();

            // 3) Job 실행
            jobLauncher.run(job, jobParameters);

            // 4) 성공 시 상태 업데이트
            log.setStatus("SUCCESS");
            log.setResponse("Spring Batch Job '" + beanName + "' executed successfully");

        } catch (Exception ex) {
            // 5) 실패 시 상태 업데이트 및 로그 기록
            log.setStatus("FAIL");
            log.setResponse("Spring Batch execution failed: " + ex.getMessage());

            // 상세 예외 정보 추가
            ex.printStackTrace();
        }

        // 6) 실행 결과 로그 저장
        logRepository.save(log);
    }
}

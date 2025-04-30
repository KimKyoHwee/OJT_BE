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
            // Bean 이름: 배치 구성 시 @Bean(name="myJob") 으로 등록한 이름을 일치시켜 주세요.
            String beanName = batchJob.getName();
            Job job = applicationContext.getBean(beanName, Job.class);

            JobParameters params = new JobParametersBuilder()
                    .addLong("scheduleId", batchJob.getId())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(job, params);
            log.setStatus("SUCCESS");
            log.setResponse("Spring Batch Job '" + beanName + "' executed");
        } catch (Exception ex) {
            log.setStatus("FAIL");
            log.setResponse("Spring Batch execution failed: " + ex.getMessage());
        }

        logRepository.save(log);
    }
}

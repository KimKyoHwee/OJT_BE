package com.kyohwee.ojt.global.batch;

import com.kyohwee.ojt.domain.entity.BatchJob;
import com.kyohwee.ojt.domain.entity.BatchLog;
import com.kyohwee.ojt.domain.repository.BatchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BatchJobExecutor {

    private final RestTemplate restTemplate;
    private final BatchLogRepository logRepository;

    public void execute(BatchJob job) {
        BatchLog log = new BatchLog();
        log.setBatchJob(job);
        log.setExecutedAt(LocalDateTime.now());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(job.getEndpointUrl(), null, String.class);
            log.setStatus("SUCCESS");
            log.setResponse(response.getBody());
        } catch (Exception e) {
            log.setStatus("FAIL");
            log.setResponse(e.getMessage());
        }

        logRepository.save(log);
    }
}

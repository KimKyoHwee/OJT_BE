package com.kyohwee.ojt.global.batch;

import com.kyohwee.ojt.domain.entity.BatchJob;
import com.kyohwee.ojt.domain.entity.BatchLog;
import com.kyohwee.ojt.domain.repository.BatchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobExecutor {

    private final RestTemplate restTemplate;
    private final BatchLogRepository logRepository;

    public void
    execute(BatchJob job) {
        BatchLog batchLog = new BatchLog();
        batchLog.setBatchJob(job);
        batchLog.setExecutedAt(LocalDateTime.now());
        System.out.println("배치요청은 들어옴");

        try {
            System.out.println("배치요청은 끝까지 들어감");
            log.info("🚀 [배치 실행] URL 호출 시작: {}", job.getEndpointUrl());
            ResponseEntity<String> response = restTemplate.getForEntity(job.getEndpointUrl(), null, String.class);
            batchLog.setStatus("SUCCESS");
            batchLog.setResponse(response.getBody());
        } catch (Exception e) {
            batchLog.setStatus("FAIL");
            batchLog.setResponse(e.getMessage());
        }

        logRepository.save(batchLog);
    }
}

package com.kyohwee.ojt.domain.controller;

import com.kyohwee.ojt.domain.entity.BatchJob;

import com.kyohwee.ojt.domain.service.BatchJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batch-jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobService batchJobService;

    @PostMapping
    public ResponseEntity<BatchJob> createBatchJob(@RequestBody BatchJob batchJob) {
        BatchJob createdBatchJob = batchJobService.createBatchJob(batchJob);
        return ResponseEntity.ok(createdBatchJob);
    }
}
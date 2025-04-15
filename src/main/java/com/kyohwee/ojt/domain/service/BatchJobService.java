package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.entity.BatchJob;

import com.kyohwee.ojt.domain.repository.BatchJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;


    public BatchJob createBatchJob(BatchJob batchJob) {
        return batchJobRepository.save(batchJob);
    }
}
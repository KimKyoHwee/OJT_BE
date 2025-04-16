package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.dto.BatchJobListDto;
import com.kyohwee.ojt.domain.dto.BatchJobRequestDto;
import com.kyohwee.ojt.domain.dto.BatchJobResponseDto;
import com.kyohwee.ojt.domain.entity.BatchJob;

import com.kyohwee.ojt.domain.entity.User;
import com.kyohwee.ojt.domain.repository.BatchJobRepository;
import com.kyohwee.ojt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final UserRepository  userRepository;


    public BatchJobResponseDto createBatchJob(BatchJobRequestDto dto) {
        User user=User.findUser(userRepository, dto.getUserId());
        BatchJob batchJob=BatchJob.fromDto(dto, user);
        batchJobRepository.save(batchJob);
        return BatchJobResponseDto.fromEntity(batchJob);
    }

    public List<BatchJobListDto> getBatchJobsByUserId(Long userId) {
        User user = User.findUser(userRepository, userId);
        List<BatchJob> batchJobs = batchJobRepository.findByUser(user);

        return batchJobs.stream()
                .map(BatchJobListDto::fromEntity)
                .toList();
    }
}
package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.dto.BatchJobListDto;
import com.kyohwee.ojt.domain.dto.BatchJobRequestDto;
import com.kyohwee.ojt.domain.dto.BatchJobResponseDto;
import com.kyohwee.ojt.domain.entity.BatchJob;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.entity.User;
import com.kyohwee.ojt.domain.repository.BatchJobRepository;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import com.kyohwee.ojt.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final UserRepository  userRepository;
    private final BatchScheduleRepository batchScheduleRepository;


    public BatchJobResponseDto createBatchJob(BatchJobRequestDto dto) {
        // 1. 사용자 조회
        User user = User.findUser(userRepository, dto.getUserId());

        // 2. BatchJob 생성 및 저장
        BatchJob batchJob = BatchJob.fromDto(dto, user);
        batchJobRepository.save(batchJob);

        // 3. BatchSchedule 생성 및 저장
        BatchSchedule schedule = new BatchSchedule();
        schedule.setBatchJob(batchJob);
        schedule.setScheduleTime(dto.getScheduleTime());
        schedule.setRepeatIntervalMinutes(dto.getRepeatIntervalMinutes());
        schedule.setNextExecutionTime(dto.getScheduleTime()); // 최초 실행 예정 시간
        batchScheduleRepository.save(schedule);

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
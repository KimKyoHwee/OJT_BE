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
import com.kyohwee.ojt.global.exception.GlobalErrorCode;             // ✱ 추가
import com.kyohwee.ojt.global.exception.GlobalException;             // ✱ 추가
import com.kyohwee.ojt.domain.service.quartz.SchedulerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository       batchJobRepository;
    private final UserRepository           userRepository;
    private final BatchScheduleRepository  batchScheduleRepository;
    private final SchedulerService         schedulerService;

    /**
     * 배치 작업과 스케줄을 함께 생성하고 Quartz에 등록합니다.
     */
    private void setAndSaveBatchScheduleByDto(BatchJobRequestDto dto, BatchSchedule sched) {
        sched.setStartTime(dto.getStartTime());
        sched.setCronExpression(dto.getCronExpression());
        sched.setRepeatIntervalMinutes(dto.getRepeatIntervalMinutes());
        sched.setNextExecutionTime(dto.getStartTime());
        batchScheduleRepository.save(sched);
    }

    /**
     * 배치 작업과 스케줄을 함께 생성하고 Quartz에 등록합니다.
     */
    @Transactional
    public BatchJobResponseDto createBatchJob(BatchJobRequestDto dto) {
        // 1) BatchJob 저장
        User user = User.findUser(userRepository, dto.getUserId());  // 내부에서 USER_NOT_FOUND 던진다고 가정
        BatchJob job = BatchJob.fromDto(dto, user);
        batchJobRepository.save(job);

        // 2) BatchSchedule 저장
        BatchSchedule sched = new BatchSchedule();
        sched.setBatchJob(job);
        setAndSaveBatchScheduleByDto(dto, sched);

        // 3) Quartz에 트리거 등록 (예외 발생 시 GlobalException으로 래핑)
        try {
            schedulerService.register(sched);
        } catch (SchedulerException e) {
            throw new GlobalException(
                    GlobalErrorCode.SCHEDULER_REGISTRATION_ERROR,
                    e.getMessage()
            );
        }

        return BatchJobResponseDto.fromEntity(job);
    }

    /**
     * 기존 배치 작업과 스케줄을 수정하고 Quartz 트리거도 갱신합니다.
     */
    @Transactional
    public BatchJobResponseDto updateBatchJobWithSchedule(Long jobId, BatchJobRequestDto dto) {
        // 1) 기존 BatchJob 조회 · 수정
        BatchJob job = batchJobRepository.findById(jobId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.BATCH_JOB_NOT_FOUND));

        job.setName(dto.getName());
        job.setDescription(dto.getDescription());
        job.setEndpointUrl(dto.getEndpointUrl());
        batchJobRepository.save(job);

        // 2) 기존 Schedule 조회 · 수정
        BatchSchedule sched = batchScheduleRepository.findByBatchJob(job)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.SCHEDULE_NOT_FOUND));
        setAndSaveBatchScheduleByDto(dto, sched);

        // 3) Quartz 트리거 교체 등록
        try {
            schedulerService.register(sched);
        } catch (SchedulerException e) {
            throw new GlobalException(
                    GlobalErrorCode.SCHEDULER_REGISTRATION_ERROR,
                    e.getMessage()
            );
        }

        return BatchJobResponseDto.fromEntity(job);
    }

    /**
     * 사용자별 배치 작업 목록 조회
     */
    public List<BatchJobListDto> getBatchJobsByUserId(Long userId) {
        User user = User.findUser(userRepository, userId); // USER_NOT_FOUND 처리
        List<BatchJob> batchJobs = batchJobRepository.findByUser(user);
        List<BatchJobListDto> batchJobListDtos = new ArrayList<>();
        for (BatchJob batchJob : batchJobs) {
            BatchJobListDto dto = BatchJobListDto.fromEntity(batchJob,
                    batchScheduleRepository.findByBatchJob(batchJob)
                            .orElseThrow(() -> new GlobalException(GlobalErrorCode.SCHEDULE_NOT_FOUND)));
            batchJobListDtos.add(dto);
        }
        return batchJobListDtos;
    }
}

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
import com.kyohwee.ojt.domain.service.quartz.SchedulerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final UserRepository  userRepository;
    private final BatchScheduleRepository batchScheduleRepository;
    private final SchedulerService  schedulerService;


    @Transactional
    public BatchJobResponseDto createBatchJob(BatchJobRequestDto dto) throws SchedulerException {
        // 1) BatchJob 저장
        User user = User.findUser(userRepository, dto.getUserId());
        BatchJob job = BatchJob.fromDto(dto, user);
        batchJobRepository.save(job);

        // 2) BatchSchedule 저장
        BatchSchedule sched = new BatchSchedule();
        sched.setBatchJob(job);
        sched.setStartTime(dto.getStartTime());
        sched.setCronExpression(dto.getCronExpression());
        sched.setRepeatIntervalMinutes(dto.getRepeatIntervalMinutes());
        sched.setNextExecutionTime(dto.getStartTime());
        batchScheduleRepository.save(sched);

        // 3) Quartz 혹은 Polling 스케줄러에 등록 호출
        //    (이 부분은 다음 단계에서 구체화)
        schedulerService.register(sched);

        return BatchJobResponseDto.fromEntity(job);
    }


    public List<BatchJobListDto> getBatchJobsByUserId(Long userId) {
        User user = User.findUser(userRepository, userId);
        List<BatchJob> batchJobs = batchJobRepository.findByUser(user);

        return batchJobs.stream()
                .map(BatchJobListDto::fromEntity)
                .toList();
    }
}
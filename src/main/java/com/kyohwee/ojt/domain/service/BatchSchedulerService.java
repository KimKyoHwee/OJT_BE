package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.entity.BatchJob;
import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import com.kyohwee.ojt.global.batch.BatchJobExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchSchedulerService {

    private final BatchScheduleRepository scheduleRepository;
    private final BatchJobExecutor jobExecutor;

    @Scheduled(fixedRate = 60000) // 1분마다 polling
    public void pollAndExecuteSchedules() {
        LocalDateTime now = LocalDateTime.now();

        List<BatchSchedule> schedulesToRun = scheduleRepository.findAllToExecute(now);

        for (BatchSchedule schedule : schedulesToRun) {
            BatchJob job = schedule.getBatchJob();

            // 1. 실행
            jobExecutor.execute(job);

            // 2. 마지막 실행 시각 업데이트
            schedule.setLastExecutedAt(now);

            // 3. 반복 주기 있는 경우 → 다음 실행 시간 계산
            if (schedule.getRepeatIntervalHour() != null) {
                schedule.setNextExecutionTime(schedule.getNextExecutionTime().plusHours(schedule.getRepeatIntervalHour()));
            } else {
                // 반복 없음 → 비활성화 처리
                schedule.setIsActive(false);
            }
        }

        // 상태 일괄 저장
        scheduleRepository.saveAll(schedulesToRun);
    }
}


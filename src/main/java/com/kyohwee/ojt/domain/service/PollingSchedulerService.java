package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.entity.BatchJob;
import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import com.kyohwee.ojt.global.batch.BatchJobExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollingSchedulerService {

    private final BatchScheduleRepository scheduleRepository;
    private final BatchJobExecutor jobExecutor;

    //@Scheduled(fixedRate = 60000) // 1분마다 polling
    public void pollAndExecuteSchedules() {
        LocalDateTime now = LocalDateTime.now();

        List<BatchSchedule> schedulesToRun = scheduleRepository.findAllToExecute(now);

        for (BatchSchedule schedule : schedulesToRun) {
            log.info("Executing schedule ID : {}", schedule.getId());
            BatchJob job = schedule.getBatchJob();

            // 1. 실행
            jobExecutor.execute(job);

            // 2. 마지막 실행 시각 업데이트
            //schedule.setLastExecutedAt(now);

            // 3. 반복 주기 있는 경우 → 다음 실행 시간 계산
            if (schedule.getRepeatIntervalMinutes() != null) {
                schedule.setNextExecutionTime(
                        schedule.getNextExecutionTime().plusMinutes(schedule.getRepeatIntervalMinutes()) // ✅ 올바른 방식
                );
            } else {
                // 반복 없음 → 비활성화 처리
                schedule.setIsActive(false);
            }
        }
        //TODO: 최신화 되고 DB에 저장도 되지만 실제 로직이 수행되지는 않는다. 즉, 호출이 되지 않는다.
        // 상태 일괄 저장
        scheduleRepository.saveAll(schedulesToRun);
    }
}


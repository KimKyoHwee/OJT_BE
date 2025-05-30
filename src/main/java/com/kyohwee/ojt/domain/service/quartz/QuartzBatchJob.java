package com.kyohwee.ojt.domain.service.quartz;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;

import com.kyohwee.ojt.global.batch.BatchJobExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzBatchJob extends QuartzJobBean {
    private final BatchScheduleRepository scheduleRepo;
    private final BatchJobExecutor jobExecutor;  // 이 클래스에 executeSpringBatch(...) 추가

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Long scheduleId = context.getMergedJobDataMap().getLong("scheduleId");
        BatchSchedule schedule = scheduleRepo.findWithBatchJobById(scheduleId)
                .orElseThrow(() -> new IllegalStateException("No schedule " + scheduleId));

        log.info("QuartzSpringBatchJob 실행 – Spring Batch 호출: scheduleId={}", scheduleId);
        jobExecutor.executeSpringBatch(schedule.getBatchJob());  // Spring Batch JobLauncher 사용

        if (schedule.getRepeatIntervalMinutes() != null) {
            schedule.updateNextExecutionTime();
            scheduleRepo.save(schedule);
        }
    }
}

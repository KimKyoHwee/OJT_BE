package com.kyohwee.ojt.domain.service.quartz;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import com.kyohwee.ojt.global.batch.BatchJobExecutor;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuartzBatchJob extends QuartzJobBean {
    private final BatchScheduleRepository scheduleRepo;
    private final BatchJobExecutor jobExecutor;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Long scheduleId = context.getMergedJobDataMap().getLong("scheduleId");
        BatchSchedule schedule = scheduleRepo.findWithBatchJobById(scheduleId)
                .orElseThrow(() -> new IllegalStateException("No schedule " + scheduleId));
        // 1) 실제 작업 실행
        jobExecutor.execute(schedule.getBatchJob());
        // 2) 간격 기반인 경우만 nextExecutionTime 갱신
        if (schedule.getRepeatIntervalMinutes() != null) {
            schedule.updateNextExecutionTime();
            scheduleRepo.save(schedule);
        }
    }
}

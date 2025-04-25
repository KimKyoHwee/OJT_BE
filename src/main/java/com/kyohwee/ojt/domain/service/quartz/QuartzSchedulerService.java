package com.kyohwee.ojt.domain.service.quartz;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuartzSchedulerService implements SchedulerService {
    private final org.quartz.Scheduler quartz;
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final BatchScheduleRepository scheduleRepo;

    @Override
    public void register(BatchSchedule schedule) throws SchedulerException {
        // 1) JobDetail 생성
        JobDetail detail = JobBuilder.newJob(QuartzBatchJob.class)
                .withIdentity(jobKey(schedule))
                .usingJobData("scheduleId", schedule.getId())
                .build();

        // 2) Trigger 생성 (cronExpression 있으면 CronTrigger, 아니면 SimpleTrigger)
        Trigger trigger = (schedule.getCronExpression() != null && !schedule.getCronExpression().isBlank())
                ? buildCronTrigger(schedule)
                : buildSimpleTrigger(schedule);

        // 3) 기존 스케줄 교체
        if (quartz.checkExists(detail.getKey())) {
            quartz.deleteJob(detail.getKey());
        }
        quartz.scheduleJob(detail, trigger);
        log.info("Registered scheduleId={} with trigger={}", schedule.getId(), trigger.getKey());
    }

    private String jobKey(BatchSchedule s) {
        return "job_schedule_" + s.getId();
    }

    private Trigger buildCronTrigger(BatchSchedule s) {
        return TriggerBuilder.newTrigger()
                .withIdentity("trigger_schedule_" + s.getId())
                .startAt(Date.from(s.getStartTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(CronScheduleBuilder
                        .cronSchedule(s.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    private Trigger buildSimpleTrigger(BatchSchedule s) {
        return TriggerBuilder.newTrigger()
                .withIdentity("trigger_schedule_" + s.getId())
                .startAt(Date.from(s.getStartTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(s.getRepeatIntervalMinutes())
                        .repeatForever()
                        .withMisfireHandlingInstructionNextWithExistingCount())
                .build();
    }


}

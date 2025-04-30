package com.kyohwee.ojt.domain.service.quartz;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import com.kyohwee.ojt.global.enums.JobType;
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
    private final JobLauncher jobLauncher;     // Spring Batch 실행용
    private final JobRegistry jobRegistry;     // Spring Batch Job 조회용
    private final BatchScheduleRepository scheduleRepo;

    @Override
    public void register(BatchSchedule schedule) throws SchedulerException {
        // 0) 작업 타입에 따라 사용할 QuartzJobBean 클래스 결정
        Class<? extends Job> jobClass;
        if (schedule.getBatchJob().getJobType() == JobType.SPRING_BATCH) {
            jobClass = QuartzBatchJob.class;
        } else {
            jobClass = QuartzRestJob.class;
        }

        // 1) JobDetail 생성
        JobDetail detail = JobBuilder.newJob(jobClass)
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
        log.info("Registered scheduleId={} as {} with trigger={}",
                schedule.getId(),
                schedule.getBatchJob().getJobType(),
                trigger.getKey());
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

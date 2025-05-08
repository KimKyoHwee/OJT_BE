package com.kyohwee.ojt.domain.service.quartz;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import com.kyohwee.ojt.global.enums.JobType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuartzSchedulerService implements SchedulerService {
    private final Scheduler quartz;
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final BatchScheduleRepository scheduleRepo;

    @Override
    public void register(BatchSchedule schedule) throws SchedulerException {
        // 0) 작업 타입에 따라 사용할 QuartzJobBean 클래스 결정
        Class<? extends Job> jobClass =
                schedule.getBatchJob().getJobType() == JobType.SPRING_BATCH
                        ? QuartzBatchJob.class
                        : QuartzRestJob.class;

        // 그룹명으로 JobType 사용
        String group = schedule.getBatchJob().getJobType().name();

        // 1) JobDetail 생성 (그룹 지정)
        JobDetail detail = JobBuilder.newJob(jobClass)  //아까 선택한 Quartz*Job 클래스로 실행할 작업을 시작
                .withIdentity(jobKey(schedule), group)  //  Quartz 내부 식별자 (name=job_schedule_<id>, group=<JobType>) 설정
                .usingJobData("scheduleId", schedule.getId()) // 실행 시점에 scheduleId 키로 DB PK를 전달
                .build();

        // 2) Trigger 생성 (그룹 지정)
        Trigger trigger = (schedule.getCronExpression() != null && !schedule.getCronExpression().isBlank())
                ? buildCronTrigger(schedule, group)         //크론식이거나
                : buildSimpleTrigger(schedule, group);      //단순 반복식이거나

        // 3) 기존 스케줄 교체
        if (quartz.checkExists(detail.getKey())) {      //이미 동일한 (name, group)으로 등록된 Job이 있으면 삭제
            quartz.deleteJob(detail.getKey());
        }
        quartz.scheduleJob(detail, trigger);
        log.info("Registered scheduleId={} as {} with trigger={}",
                schedule.getId(), schedule.getBatchJob().getJobType(), trigger.getKey());
    }

    @Override
    public void update(BatchSchedule schedule) throws SchedulerException {
        unregister(schedule);
        register(schedule);
        log.info("Updated scheduleId={}", schedule.getId());
    }

    @Override
    public void unregister(BatchSchedule schedule) throws SchedulerException {
        JobKey key = JobKey.jobKey(jobKey(schedule), schedule.getBatchJob().getJobType().name());
        if (quartz.checkExists(key)) {
            quartz.deleteJob(key);
            log.info("Unregistered scheduleId={}", schedule.getId());
        }
    }

    @Override
    public void pause(BatchSchedule schedule) throws SchedulerException {
        JobKey key = JobKey.jobKey(jobKey(schedule), schedule.getBatchJob().getJobType().name());
        quartz.pauseJob(key);
        log.info("Paused scheduleId={}", schedule.getId());
    }

    @Override
    public void resume(BatchSchedule schedule) throws SchedulerException {
        JobKey key = JobKey.jobKey(jobKey(schedule), schedule.getBatchJob().getJobType().name());
        quartz.resumeJob(key);
        log.info("Resumed scheduleId={}", schedule.getId());
    }

    // Quartz 상의 JobKey 생성
    private String jobKey(BatchSchedule schedule) {
        return "job_schedule_" + schedule.getId();
    }

    // CronTrigger 생성 (그룹 지정)
    private Trigger buildCronTrigger(BatchSchedule schedule, String group) {
        return TriggerBuilder.newTrigger()
                .withIdentity("trigger_schedule_" + schedule.getId(), group)
                .startAt(Date.from(schedule.getStartTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(CronScheduleBuilder
                        .cronSchedule(schedule.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    // SimpleTrigger 생성 (그룹 지정)
    private Trigger buildSimpleTrigger(BatchSchedule schedule, String group) {
        return TriggerBuilder.newTrigger()
                .withIdentity("trigger_schedule_" + schedule.getId(), group)
                .startAt(Date.from(schedule.getStartTime().atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(schedule.getRepeatIntervalMinutes())
                        .repeatForever()
                        .withMisfireHandlingInstructionNextWithExistingCount())
                .build();
    }
}
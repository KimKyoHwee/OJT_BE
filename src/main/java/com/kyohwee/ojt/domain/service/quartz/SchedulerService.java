package com.kyohwee.ojt.domain.service.quartz;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import org.quartz.SchedulerException;

public interface SchedulerService {
    void register(BatchSchedule schedule) throws SchedulerException;
    void update(BatchSchedule schedule) throws SchedulerException;
    void unregister(BatchSchedule schedule) throws SchedulerException;
    void pause(BatchSchedule schedule) throws SchedulerException;
    void resume(BatchSchedule schedule) throws SchedulerException;
}

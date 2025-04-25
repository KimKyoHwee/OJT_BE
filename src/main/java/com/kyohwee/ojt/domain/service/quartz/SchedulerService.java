package com.kyohwee.ojt.domain.service.quartz;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import org.quartz.SchedulerException;

public interface SchedulerService {
    /**
     * 스케줄 엔티티 한 건을 받아서 Quartz에 등록(또는 갱신)합니다.
     */
    void register(BatchSchedule schedule) throws SchedulerException;
}

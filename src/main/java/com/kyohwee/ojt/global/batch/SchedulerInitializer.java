package com.kyohwee.ojt.global.batch;

import com.kyohwee.ojt.domain.repository.BatchScheduleRepository;
import com.kyohwee.ojt.domain.service.quartz.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulerInitializer {
    private final BatchScheduleRepository scheduleRepo;
    private final SchedulerService schedulerService;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        scheduleRepo.findAllByIsActiveTrue().forEach(sched -> {
            try {
                schedulerService.register(sched);
            } catch (SchedulerException e) {
                // 로깅
            }
        });
    }
}

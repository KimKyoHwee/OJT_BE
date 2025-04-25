package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.BatchSchedule;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BatchScheduleRepository extends JpaRepository<BatchSchedule, Long> {
    @Query("SELECT s FROM BatchSchedule s JOIN FETCH s.batchJob WHERE s.isActive = true AND s.nextExecutionTime <= :now")
    List<BatchSchedule> findAllToExecute(@Param("now") LocalDateTime now);

    List<BatchSchedule> findAllByIsActiveTrue();


}

package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.BatchLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;           // ⭐ 스프링 Data JPA Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface BatchLogRepository extends JpaRepository<BatchLog, Long> {
    Page<BatchLog> findByBatchJobIdOrderByExecutedAtDesc(Long jobId, Pageable pageable);

}

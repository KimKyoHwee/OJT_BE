package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.BatchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchLogRepository extends JpaRepository<BatchLog, Long> {
}

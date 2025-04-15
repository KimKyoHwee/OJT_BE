package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {
}

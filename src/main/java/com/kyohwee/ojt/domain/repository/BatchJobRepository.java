package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.BatchJob;
import com.kyohwee.ojt.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {
    List<BatchJob> findByUser(User user);
}

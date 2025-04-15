package com.kyohwee.ojt.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 배치 작업의 실행 시간대를 저장하는 엔티티
 */
@Entity
@Getter
@Setter
public class BatchSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_job_id", nullable = false)
    private BatchJob batchJob; // 배치 작업과의 연관 관계

    @Column(nullable = false)
    private LocalDateTime scheduleTime; // 실행 시간대

    @Column(nullable = false)
    private Boolean isActive = true; // 활성화 여부

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성 시간

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정 시간

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // 수정 시 updatedAt 갱신
    }
}
package com.kyohwee.ojt.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 배치 작업 실행 결과를 저장하는 엔티티
 */
@Entity
@Getter
@Setter
public class BatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_job_id", nullable = false)
    private BatchJob batchJob; // 배치 작업과의 연관 관계

    @Column(nullable = false, length = 50)
    private String status; // 실행 상태 (예: SUCCESS, FAILURE)

    @Column(columnDefinition = "TEXT")
    private String response; // API 응답 데이터

    @Column(nullable = false, updatable = false)
    private LocalDateTime executedAt = LocalDateTime.now(); // 실행 시간
}

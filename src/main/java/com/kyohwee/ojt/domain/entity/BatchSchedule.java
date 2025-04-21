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
    private Long id;

    // 어떤 배치 작업인지 (Job과 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_job_id", nullable = false)
    private BatchJob batchJob;

    // 최초 시작 시간 (첫 실행 기준)
    @Column(nullable = false)
    private LocalDateTime scheduleTime;

    // 몇 시간마다 반복할지 (null이면 단발성 실행)
    @Column
    private Integer repeatIntervalHour;

    // 마지막으로 실행된 시각
    @Column
    private LocalDateTime lastExecutedAt;

    // 다음 실행 예정 시간
    @Column
    private LocalDateTime nextExecutionTime;

    // 실행 유효 여부
    @Column(nullable = false)
    private Boolean isActive = true;

    // 생성/수정 시간
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 다음 실행 시간 갱신 (repeatIntervalHour 있을 경우)
     */
    public void updateNextExecutionTime() {
        if (this.repeatIntervalHour != null) {
            this.nextExecutionTime = (this.nextExecutionTime != null ? this.nextExecutionTime : this.scheduleTime)
                    .plusHours(this.repeatIntervalHour);
        } else {
            this.isActive = false; // 반복 없음 → 비활성화
        }
    }
}

package com.kyohwee.ojt.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 배치 작업의 실행 시간대를 저장하는 엔티티
 */
@Entity
@Getter @Setter
@Table(name="quartz_triggers")
public class BatchSchedule {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="batch_job_id", nullable=false)
    private BatchJob batchJob;

    /** 언제부터 실행할지 */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** Cron 표현식 (null 이면 간격 기반) */
    @Column(length = 100)
    private String cronExpression;

    /** 반복 간격(분, null 이면 Cron 기반) */
    @Column
    private Integer repeatIntervalMinutes;

    /** 다음 실행 예정 시각 (간격 기반 갱신용) */
    @Column
    private LocalDateTime nextExecutionTime;

    @Column(nullable=false)
    private Boolean isActive = true;

    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable=false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    private void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void updateNextExecutionTime() {
        if (repeatIntervalMinutes != null) {
            LocalDateTime base = nextExecutionTime != null ? nextExecutionTime : startTime;
            this.nextExecutionTime = base.plusMinutes(repeatIntervalMinutes);
        } else {
            this.isActive = false;
        }
    }
}


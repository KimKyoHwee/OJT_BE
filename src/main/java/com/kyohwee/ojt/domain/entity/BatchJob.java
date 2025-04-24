package com.kyohwee.ojt.domain.entity;

import com.kyohwee.ojt.domain.dto.BatchJobRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 배치 작업 정보를 저장하는 엔티티
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    @Column(nullable = false, length = 100)
    private String cronExpression;

    @Column(nullable = false, length = 255)
    private String name; // 배치 작업 이름

    @Column(columnDefinition = "TEXT")
    private String description; // 배치 작업 설명

    @Column(nullable = false, length = 500)
    private String endpointUrl; // API 요청을 보낼 엔드포인트 URL

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성 시간

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정 시간

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // 수정 시 updatedAt 갱신
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static BatchJob fromDto(BatchJobRequestDto dto, User user) {
        return BatchJob.builder()
                .user(user)
                .name(dto.getName())
                .description(dto.getDescription())
                .endpointUrl(dto.getEndpointUrl())
                .cronExpression(dto.getCronExpression())
                .build();

    }
}
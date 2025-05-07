package com.kyohwee.ojt.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_documents")
@Getter
@Setter
@NoArgsConstructor
public class BusinessDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OCR 대상 이미지 URL
    @Column(nullable = false)
    private String imageUrl;

    // OCR 결과 텍스트
    @Column(columnDefinition = "TEXT")
    private String ocrResult;

    // OCR 처리 여부
    @Column(nullable = false)
    private boolean ocrProcessed = false;

    // 진위 검증 여부
    @Column(nullable = false)
    private boolean verified = false;

    // 최종 성공 여부 (OCR + 검증 모두 성공 시)
    @Column(nullable = false)
    private boolean success = false;

    // 검증 API에서 반환하는 상태 코드
    @Column(length = 20)
    private String verificationStatus;

    // 검증 API에서 반환하는 메시지
    @Column(columnDefinition = "TEXT")
    private String verificationMessage;

    // 생성/수정 타임스탬프
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

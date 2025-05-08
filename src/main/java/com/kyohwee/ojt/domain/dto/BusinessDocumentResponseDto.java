package com.kyohwee.ojt.domain.dto;

import com.kyohwee.ojt.domain.entity.BusinessDocumentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDocumentResponseDto {
    private Long id;
    private String imageUrl;
    private LocalDateTime createdAt;
    private boolean ocrProcessed;
    private boolean verified;
    private boolean success;

    static public BusinessDocumentResponseDto fromEntity(BusinessDocumentEntity entity) {
        return BusinessDocumentResponseDto.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .ocrProcessed(entity.isOcrProcessed())
                .verified(entity.isVerified())
                .success(entity.isSuccess())
                .build();
    }
}
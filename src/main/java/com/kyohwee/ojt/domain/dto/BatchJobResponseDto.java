package com.kyohwee.ojt.domain.dto;

import com.kyohwee.ojt.domain.entity.BatchJob;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class BatchJobResponseDto {
    private Long id;
    private String name;
    private String description;
    private String endpointUrl;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BatchJobResponseDto fromEntity(BatchJob batchJob) {
        return BatchJobResponseDto.builder().
                id(batchJob.getId()).
                name(batchJob.getName()).
                description(batchJob.getDescription()).
                endpointUrl(batchJob.getEndpointUrl()).
                userId(batchJob.getUser().getId()).
                createdAt(batchJob.getCreatedAt()).
                updatedAt(batchJob.getUpdatedAt()).
                build();
    }
}
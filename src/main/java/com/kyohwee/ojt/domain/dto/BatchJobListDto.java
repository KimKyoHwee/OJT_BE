package com.kyohwee.ojt.domain.dto;

import com.kyohwee.ojt.domain.entity.BatchJob;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatchJobListDto {
    private Long id;
    private String name;
    private String description;
    private String endpointUrl;

    public static BatchJobListDto fromEntity(BatchJob batchJob) {
        return BatchJobListDto.builder()
                .id(batchJob.getId())
                .name(batchJob.getName())
                .description(batchJob.getDescription())
                .endpointUrl(batchJob.getEndpointUrl())
                .build();
    }
}

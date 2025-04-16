package com.kyohwee.ojt.domain.dto;

import com.kyohwee.ojt.domain.entity.BatchJob;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BatchJobRequestDto {
    private String name;
    private String description;
    private String endpointUrl;
    private Long userId;


}
package com.kyohwee.ojt.domain.dto;

import com.kyohwee.ojt.domain.entity.BatchJob;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @Builder
public class BatchJobRequestDto {
    private String name;
    private String description;
    private String endpointUrl;
    private Long   userId;

    // 스케줄 정보
    private LocalDateTime startTime;
    private String        cronExpression;
    private Integer       repeatIntervalMinutes;
}

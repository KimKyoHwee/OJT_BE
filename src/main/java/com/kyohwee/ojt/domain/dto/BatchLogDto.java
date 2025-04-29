package com.kyohwee.ojt.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 한 로그 항목
@Getter
@AllArgsConstructor
public class BatchLogDto {
    private Long batchLogId;
    private String status;
    private String response;
    private LocalDateTime executedAt;
}


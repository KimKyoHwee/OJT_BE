package com.kyohwee.ojt.domain.controller;

import com.kyohwee.ojt.domain.dto.BatchLogDto;
import com.kyohwee.ojt.domain.dto.PageResponse;
import com.kyohwee.ojt.domain.service.BatchLogService;
import com.kyohwee.ojt.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "BatchLog", description = "실행 로그 조회")
public class BatchLogController {
    private final BatchLogService logService;

    @Operation(summary = "작업 실행 로그 조회 (페이징)",
            description = "jobId 에 해당하는 실행 로그를 page, size 단위로 페이징 조회합니다.")
    @GetMapping("/job/{jobId}")
    public ApiResponse<PageResponse<BatchLogDto>> getJobLogs(
            @Parameter(description="BatchJob ID", required=true) @PathVariable Long jobId,
            @Parameter(description="페이지 번호 (0부터)", example="0") @RequestParam(defaultValue="0") int page,
            @Parameter(description="페이지 크기", example="10") @RequestParam(defaultValue="10") int size
    ) {
        PageResponse<BatchLogDto> pageResp = logService.getLogs(jobId, page, size);
        return ApiResponse.onSuccessOK(pageResp);
    }
}

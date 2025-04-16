package com.kyohwee.ojt.domain.controller;

import com.kyohwee.ojt.domain.dto.BatchJobRequestDto;
import com.kyohwee.ojt.domain.dto.BatchJobResponseDto;
import com.kyohwee.ojt.domain.entity.BatchJob;

import com.kyohwee.ojt.domain.service.BatchJobService;
import com.kyohwee.ojt.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.kyohwee.ojt.global.uri.RequestUri.BATCH_JOB_URI;

@RestController
@RequestMapping(BATCH_JOB_URI)
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobService batchJobService;

    @PostMapping
    @Operation(summary = "배치 작업 생성", description = "새로운 배치 작업을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "배치 작업 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 형식이 잘못되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 에러")
    })
    public ResponseEntity<ApiResponse<BatchJobResponseDto>> createBatchJob(@RequestBody BatchJobRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccessCREATED(batchJobService.createBatchJob(request)));
    }
}
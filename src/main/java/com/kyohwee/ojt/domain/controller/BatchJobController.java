package com.kyohwee.ojt.domain.controller;

import com.kyohwee.ojt.domain.dto.BatchJobListDto;
import com.kyohwee.ojt.domain.dto.BatchJobRequestDto;
import com.kyohwee.ojt.domain.dto.BatchJobResponseDto;
import com.kyohwee.ojt.domain.entity.BatchJob;

import com.kyohwee.ojt.domain.service.BatchJobService;
import com.kyohwee.ojt.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kyohwee.ojt.global.uri.RequestUri.BATCH_JOB_URI;

@RestController
@RequestMapping(BATCH_JOB_URI)
@RequiredArgsConstructor
@Slf4j
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

    @PutMapping(
            path     = "/{jobId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary     = "배치 작업 수정",
            description = "기존 배치 작업 및 스케줄을 수정하고 Quartz 트리거를 갱신합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배치 작업 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 jobId이거나 잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<BatchJobResponseDto> updateBatchJob(
            @Parameter(description = "수정할 배치 작업의 ID", required = true)
            @PathVariable Long jobId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 배치 작업 및 스케줄 정보",
                    required    = true,
                    content     = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema    = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = BatchJobRequestDto.class
                            )
                    )
            )
            @RequestBody BatchJobRequestDto dto
    ) throws SchedulerException {
        log.info("-----------BatchJobController.updateBatchJob");
        BatchJobResponseDto result = batchJobService.updateBatchJobWithSchedule(jobId, dto);
        return ResponseEntity.ok(result);
    }

    //사용자의 BatchJob을 조회하는 API
    @GetMapping("/{userId}")
    @Operation(summary = "사용자의 배치 작업 조회", description = "특정 사용자가 생성한 배치 작업들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<BatchJobListDto>>> getBatchJobsByUserId(@PathVariable Long userId) {
        List<BatchJobListDto> batchJobs = batchJobService.getBatchJobsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(HttpStatus.OK, "사용자의 배치 작업 조회 성공", batchJobs));
    }
}
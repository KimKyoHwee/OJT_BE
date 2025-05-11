package com.kyohwee.ojt.domain.dto;

import com.kyohwee.ojt.domain.entity.BatchJob;
import com.kyohwee.ojt.domain.entity.BatchSchedule;
import com.kyohwee.ojt.global.enums.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BatchJobListDto {
    private Long batchJobId;
    private String name;
    private String description;
    private String endpointUrl;
    private LocalDateTime updateAt; //최근 배치 작업 시간
    private LocalDateTime nextExecutionTime;  //다음 배치 수행 시간
    private String cronExpression; //cron 표현식
    private Integer repeatIntervalMinutes; //배치 수행 간격

    private JobType jobType;
    private LocalDateTime  startTime;

    public static BatchJobListDto fromEntity(BatchJob batchJob, BatchSchedule batchSchedule) {
        return BatchJobListDto.builder()
                .batchJobId(batchJob.getId())
                .name(batchJob.getName())
                .description(batchJob.getDescription())
                .endpointUrl(batchJob.getEndpointUrl())
                .updateAt(batchJob.getUpdatedAt())
                .nextExecutionTime(batchSchedule.getNextExecutionTime())
                .cronExpression(batchSchedule.getCronExpression())
                .repeatIntervalMinutes(batchSchedule.getRepeatIntervalMinutes())
                .jobType(batchJob.getJobType())    // 추가
                .startTime(batchSchedule.getStartTime())
                .build();
    }
}

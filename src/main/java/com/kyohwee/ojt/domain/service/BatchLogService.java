package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.dto.BatchLogDto;
import com.kyohwee.ojt.domain.dto.PageResponse;
import com.kyohwee.ojt.domain.entity.BatchLog;
import com.kyohwee.ojt.domain.repository.BatchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchLogService {
    private final BatchLogRepository logRepository;

    public PageResponse<BatchLogDto> getLogs(Long jobId, int page, int size) {
        Page<BatchLog> logsPage = logRepository.findByBatchJobIdOrderByExecutedAtDesc(
                jobId, PageRequest.of(page, size)
        );
        List<BatchLogDto> dtos = logsPage.getContent().stream()
                .map(l -> new BatchLogDto(l.getId(),l.getStatus(), l.getResponse(), l.getExecutedAt()))
                .toList();
        return new PageResponse<>(
                dtos,
                logsPage.getNumber(),
                logsPage.getSize(),
                logsPage.getTotalElements(),
                logsPage.getTotalPages()
        );
    }
}

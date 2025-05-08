package com.kyohwee.ojt.domain.controller;

import com.kyohwee.ojt.domain.dto.BusinessDocumentRequestDto;
import com.kyohwee.ojt.domain.dto.BusinessDocumentResponseDto;
import com.kyohwee.ojt.domain.service.BusinessDocumentService;
import com.kyohwee.ojt.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
public class BusinessDocumentController {
    private final BusinessDocumentService businessDocumentService;

    @PostMapping
    public ResponseEntity<ApiResponse<BusinessDocumentResponseDto>> uploadDocument(
            @RequestParam("userId") Long userId,
            @RequestBody MultipartFile file) {
        BusinessDocumentRequestDto requestDto = new BusinessDocumentRequestDto();
        requestDto.setFile(file);
        BusinessDocumentResponseDto responseDto = businessDocumentService.uploadDocument(userId, requestDto);
        return ResponseEntity.ok(ApiResponse.onSuccessOK(responseDto));
    }
}
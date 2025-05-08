package com.kyohwee.ojt.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BusinessDocumentRequestDto {
    private MultipartFile file;

}


package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.dto.BusinessDocumentResponseDto;
import com.kyohwee.ojt.domain.entity.BusinessDocumentEntity;
import com.kyohwee.ojt.domain.entity.User;
import com.kyohwee.ojt.domain.repository.BusinessDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.kyohwee.ojt.domain.dto.BusinessDocumentRequestDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BusinessDocumentService {
    private final S3Service s3Service;
    private final BusinessDocumentRepository businessDocumentRepository;
    private final UserService userService;  // 사용자 서비스 주입

    public BusinessDocumentResponseDto uploadDocument(Long userId, BusinessDocumentRequestDto requestDto) {
        try {
            // 사용자 조회
            User user = userService.getUserById(userId);
            
            // S3에 이미지 업로드
            String imageUrl = s3Service.uploadImage(requestDto.getFile(), "documents");

            // 엔티티 생성 및 저장 (사용자 정보 포함)
            BusinessDocumentEntity document = new BusinessDocumentEntity(user, imageUrl);
            BusinessDocumentEntity savedDocument = businessDocumentRepository.save(document);

            return BusinessDocumentResponseDto.fromEntity(savedDocument);
        } catch (Exception e) {
            log.error("문서 업로드 중 오류 발생", e);
            throw new RuntimeException("문서 업로드에 실패했습니다.", e);
        }
    }

    // 사용자별 문서 조회 메서드 추가
    public List<BusinessDocumentResponseDto> getDocumentsByUserId(Long userId) {
        User user = userService.getUserById(userId);
        List<BusinessDocumentEntity> documents = businessDocumentRepository.findByUser(user);
        return documents.stream()
                .map(BusinessDocumentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

}
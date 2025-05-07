package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.BusinessDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessDocumentRepository
        extends JpaRepository<BusinessDocumentEntity, Long> {

    // OCR 미처리된 것만 가져오기
    List<BusinessDocumentEntity> findByOcrProcessedFalseOrderByIdAsc();

    // OCR 처리 완료 & 검증 미완료인 것만 가져오기
    List<BusinessDocumentEntity> findByOcrProcessedTrueAndVerifiedFalseOrderByIdAsc();
}

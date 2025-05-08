package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.BusinessDocumentEntity;
import com.kyohwee.ojt.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BusinessDocumentEntity 조회용 JPA 리포지토리
 */
@Repository
public interface BusinessDocumentRepository extends JpaRepository<BusinessDocumentEntity, Long> {

    List<BusinessDocumentEntity> findByUserId(Long userId);

    List<BusinessDocumentEntity> findByUserIdAndOcrProcessedOrderByCreatedAtDesc(Long userId, boolean ocrProcessed);

    List<BusinessDocumentEntity> findByUser(User user);

    /**
     * OCR 처리되지 않은 엔티티를 ID 오름차순으로 페이지 단위 조회
     */
    Page<BusinessDocumentEntity> findByOcrProcessedFalseOrderByIdAsc(Pageable pageable);

    /**
     * OCR은 됐지만 검증되지 않은 엔티티를 ID 오름차순으로 페이지 단위 조회
     */
    Page<BusinessDocumentEntity> findByOcrProcessedTrueAndVerifiedFalseOrderByIdAsc(Pageable pageable);
}

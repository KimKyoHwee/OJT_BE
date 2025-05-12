package com.kyohwee.ojt.domain.repository;

import com.kyohwee.ojt.domain.entity.OcrResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OcrResultEntityRepository extends JpaRepository<OcrResultEntity, Long> {
    // 문서별 최신 OCR 결과를 가져오는 쿼리
    Optional<OcrResultEntity> findTopByImageNameOrderByTimestampDesc(String imageName);
}

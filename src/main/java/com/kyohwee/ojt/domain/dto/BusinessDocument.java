package com.kyohwee.ojt.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배치 프로세스의 각 Step 사이에서 전달되는 문서 모델.
 * - ocrStep() 에서 OCR 결과(ocrResult)를 채운 뒤
 * - verifyStep() 에서 이 객체를 이용해 사업자 진위 확인 API 호출
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDocument {
    private Long id;                // DB PK (선택)
    private String imageUrl;        // OCR 대상 이미지 URL
    private String ocrResult;       // OCR로 뽑은 원문 텍스트
    private String businessNumber;  // ocrResult 에서 추출한 사업자번호 (하이픈 제거)
    private String startDate;       // ocrResult 에서 추출한 개업일자 (YYYYMMDD)
    private String ownerName;       // ocrResult 에서 추출한 대표자명
    private String verificationStatus; // API 검증 결과 코드 (e.g. “01”: 정상)
    private String verificationMessage; // API 응답 메시지 (optional)
}

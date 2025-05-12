package com.kyohwee.ojt.domain.entity;

import com.kyohwee.ojt.domain.dto.ClovaOcrResponseDto;
import jakarta.persistence.*;
import lombok.*;

/**
 * OCR 결과를 저장하는 엔티티
 * Clova OCR Document v2의 BizLicense(사업자등록증) 모델 결과를 포함
 */
@Entity
@Table(name = "ocr_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultEntity {
    /**
     * 기본 키 (Auto-increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- OCR 메타 정보 ---

    /**
     * 호출한 OCR API 버전 (예: "V1", "V2")
     */
    @Column(length = 10)
    private String version;

    /**
     * OCR 요청 시 생성한 고유 ID (UUID)
     */
    @Column(length = 64)
    private String requestId;

    /**
     * 요청 발생 시각 (밀리초 단위 UNIX epoch)
     */
    private Long timestamp;

    // --- 이미지 응답 정보 ---

    /**
     * OCR 응답에서 제공하는 이미지 식별자
     */
    @Column(length = 64)
    private String imageUid;

    /**
     * 요청 페이로드에 설정한 이미지 이름 (예: "img-<UUID>")
     */
    @Column(length = 255)
    private String imageName;

    /**
     * OCR 전체 처리 결과 코드 (예: "SUCCESS", "FAIL")
     */
    @Column(length = 20)
    private String inferResult;

    /**
     * OCR 처리 관련 메시지 (성공 또는 에러 설명)
     */
    @Column(columnDefinition = "TEXT")
    private String inferMessage;

    /**
     * API가 추정한 문서 언어 (예: "ko", "en")
     */
    @Column(length = 10)
    private String estimatedLanguage;

    // --- BizLicense (사업자등록증) 모델 결과 ---

    /**
     * 추출된 회사명 (여러 필드를 합친 문자열)
     */
    @Column(length = 255)
    private String companyName;

    /**
     * 추출된 대표자명 (여러 필드를 합친 문자열)
     */
    @Column(length = 255)
    private String repName;

    /**
     * 추출된 사업자등록번호 (예: "104-81-36565")
     */
    @Column(length = 20)
    private String registerNumber;

    /**
     * 추출된 개업일자 (예: "1995 년 03 월 15 일")
     */
    @Column(length = 20)
    private String openDate;

    /**
     * 추출된 발급일자 (예: "2023 년 08 월 01 일")
     */
    @Column(length = 20)
    private String issuanceDate;

    /**
     * DTO에서 핵심 메타·이미지 정보만 매핑하여 OcrResultEntity 생성
     * 이후 별도 필드(companyName 등)는 수동 할당 필요
     */
    public static OcrResultEntity from(ClovaOcrResponseDto dto) {
        var img = dto.getImages().get(0);
        var biz = img.getBizLicense().getResult();
        // Helper to join text fields
        java.util.function.Function<java.util.List<ClovaOcrResponseDto.Field>, String> join = list ->
                list == null ? "" : list.stream()
                        .map(ClovaOcrResponseDto.Field::getText)
                        .collect(java.util.stream.Collectors.joining(" "));

        return OcrResultEntity.builder()
                .version(dto.getVersion())
                .requestId(dto.getRequestId())
                .timestamp(dto.getTimestamp())
                .imageUid(img.getUid())
                .imageName(img.getName())
                .inferResult(img.getInferResult())
                .inferMessage(img.getMessage())
                .estimatedLanguage(img.getBizLicense().getMeta().getEstimatedLanguage())
                .companyName(join.apply(biz.getCompanyName()))
                .repName(join.apply(biz.getRepName()))
                .registerNumber(join.apply(biz.getRegisterNumber()))
                .openDate(join.apply(biz.getOpenDate()))
                .issuanceDate(join.apply(biz.getIssuanceDate()))
                .build();
    }
}

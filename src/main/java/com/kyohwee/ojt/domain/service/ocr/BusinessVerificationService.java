package com.kyohwee.ojt.domain.service.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kyohwee.ojt.domain.dto.BusinessDocument;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Clova OCR로 추출된 사업자 정보를
 * 공공데이터포털 “사업자등록증 진위확인(validate)” API에 보내
 * 결과를 돌려받는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessVerificationService {

    private final RestTemplate restTemplate;

    /** 공공데이터 포털 진위확인 엔드포인트 (application.yml 에서 설정) */
    @Value("${openapi.business.validate-url}")
    private String validateUrl;

    /** 발급받은 서비스 키 (application.yml) */
    @Value("${openapi.business.service-key}")
    private String serviceKey;

    /**
     * 기존 verify 메소드 유지 (직접 DTO를 넘겼을 때)
     */
    public String verify(BusinessDocument doc) {
        String url = validateUrl + "?serviceKey=" + serviceKey + "&returnType=JSON";

        // 1) 숫자만 남겨서 포맷 정리
        String bNo     = doc.getBusinessNumber().replaceAll("\\D+", "");
        String startDt = doc.getStartDate().replaceAll("\\D+", ""); // ex: "19950315"

        // 2) 페이로드 준비
        Map<String,String> biz = new HashMap<>();
        biz.put("b_no",     bNo);
        biz.put("start_dt", startDt);
        biz.put("p_nm",     doc.getOwnerName());
        // TODO: 대표자명이 2개 이상일 때 어떻게 처리할지 고민
        biz.put("p_nm2",    "");
        // TODO: 회사명을 현재 긁어올 수 없음
        biz.put("b_nm",     "");
        biz.put("corp_no",  "");      // 없으면 빈 문자열
        biz.put("b_sector", "");
        biz.put("b_type",   "");
        biz.put("b_adr",    "");

        Map<String,Object> payload = Map.of("businesses", List.of(biz));

        log.info("Verify payload: {}", payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(payload, headers);

        // 3) 호출
        ResponseEntity<ValidateResponse> resp =
                restTemplate.postForEntity(url, request, ValidateResponse.class);

        // --- 응답 처리 부분 ---
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("진위확인 API 오류: HTTP " + resp.getStatusCode());
        }

        ValidateResponse body = resp.getBody();

        // 에러 응답인 경우 status_code 만 로그
        if (!"OK".equals(body.getStatusCode())) {
            log.error("진위확인 API 에러 status_code={}", body.getStatusCode());
            return null;
        }

        // 정상 응답이지만 data가 없으면 예외
        if (body.getData() == null || body.getData().isEmpty()) {
            throw new RuntimeException("진위확인 결과 없음");
        }

        // 첫 번째 엔트리에서 valid, validMsg 추출
        ValidateResponse.BusinessData entry = body.getData().get(0);
        doc.setVerificationStatus(entry.getValid());
        doc.setVerificationMessage(entry.getValidMsg());
        return entry.getValid();
    }

    /**
     * 배치 코드 쪽에서 사용하기 위한 alias 메소드
     */
    public String checkBusiness(BusinessDocument doc) {
        return verify(doc);
    }

    // --- 응답 DTO들 ---
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidateResponse {
        /** "OK" 또는 에러 코드 */
        @JsonProperty("status_code")
        private String statusCode;

        /** 요청 건수 */
        @JsonProperty("request_cnt")
        private int requestCnt;

        /** 유효 사업자 수 */
        @JsonProperty("valid_cnt")
        private int validCnt;

        /** 결과 데이터 배열 */
        private List<BusinessData> data;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class BusinessData {
            /** 사업자등록번호 */
            @JsonProperty("b_no")
            private String bNo;

            /** 진위여부 ("01"=유효 등) */
            @JsonProperty("valid")
            private String valid;

            /** 응답 메시지 */
            @JsonProperty("valid_msg")
            private String validMsg;

            /** 호출 시 사용된 파라미터 원본 */
            @JsonProperty("request_param")
            private RequestParam requestParam;

            /** 상태조회 결과 */
            private Status status;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class RequestParam {
                @JsonProperty("b_no")     private String bNo;
                @JsonProperty("start_dt") private String startDt;
                @JsonProperty("p_nm")     private String pNm;
                @JsonProperty("p_nm2")    private String pNm2;
                @JsonProperty("b_nm")     private String bNm;
                @JsonProperty("corp_no")  private String corpNo;
                @JsonProperty("b_sector") private String bSector;
                @JsonProperty("b_type")   private String bType;
                @JsonProperty("b_adr")    private String bAdr;
            }

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Status {
                @JsonProperty("b_no")               private String bNo;
                @JsonProperty("b_stt")              private String bStt;
                @JsonProperty("b_stt_cd")           private String bSttCd;
                @JsonProperty("tax_type")           private String taxType;
                @JsonProperty("tax_type_cd")        private String taxTypeCd;
                @JsonProperty("end_dt")             private String endDt;
                @JsonProperty("utcc_yn")            private String utccYn;
                @JsonProperty("tax_type_change_dt") private String taxTypeChangeDt;
                @JsonProperty("invoice_apply_dt")   private String invoiceApplyDt;
                @JsonProperty("rbf_tax_type")       private String rbfTaxType;
                @JsonProperty("rbf_tax_type_cd")    private String rbfTaxTypeCd;
            }
        }
    }
}

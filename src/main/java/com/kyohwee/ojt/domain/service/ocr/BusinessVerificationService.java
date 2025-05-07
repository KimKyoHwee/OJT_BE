package com.kyohwee.ojt.domain.service.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kyohwee.ojt.domain.dto.BusinessDocument;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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

        Map<String,Object> payload = new HashMap<>();
        Map<String,String> biz = new HashMap<>();
        biz.put("b_no", doc.getBusinessNumber());
        biz.put("start_dt", doc.getStartDate());
        biz.put("p_nm", doc.getOwnerName());
        payload.put("businesses", Collections.singletonList(biz));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<ValidateResponse> resp = restTemplate.postForEntity(url, request, ValidateResponse.class);

        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody()!=null && !resp.getBody().getData().isEmpty()) {
            ValidateResponse.Result r = resp.getBody().getData().get(0);
            doc.setVerificationStatus(r.getValidYn());
            doc.setVerificationMessage(r.getValidMsg());
            return r.getValidYn();
        } else {
            throw new RuntimeException("진위확인 API 오류: HTTP " + resp.getStatusCode());
        }
    }

    /**
     * 배치 코드 쪽에서 사용하기 위한 alias 메소드
     */
    public String checkBusiness(BusinessDocument doc) {
        return verify(doc);
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidateResponse {
        private List<Result> data;
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Result {
            private String validYn;    // 진위여부 코드
            private String validMsg;   // 응답 메시지
        }
    }
}

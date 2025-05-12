package com.kyohwee.ojt.domain.service.ocr;

import com.kyohwee.ojt.domain.dto.BusinessDocument;
import com.kyohwee.ojt.domain.dto.ValidateResponse;
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

    /** 공공데이터 포털 진위확인 엔드포인트 (application.yml) */
    @Value("${openapi.business.validate-url}")
    private String validateUrl;

    /** 발급받은 서비스 키 (application.yml) */
    @Value("${openapi.business.service-key}")
    private String serviceKey;

    /**
     * 사업자등록증 진위확인용 페이로드 설계
     */
    private Map<String, String> makeBiz(String bNo, String startDt, String pNm) {
        Map<String, String> biz = new HashMap<>();
        biz.put("b_no",     bNo);
        biz.put("start_dt", startDt);
        biz.put("p_nm",     pNm);
        biz.put("p_nm2",    "");  // 공동대표자 없는 경우 빈 문자열
        biz.put("b_nm",     "");
        biz.put("corp_no",  "");
        biz.put("b_sector", "");
        biz.put("b_type",   "");
        biz.put("b_adr",    "");
        return biz;
    }

    /**
     * 진위확인 API 호출 및 응답 처리
     */
    public ValidateResponse.BusinessData verify(BusinessDocument doc) {
        String url = validateUrl + "?serviceKey=" + serviceKey + "&returnType=JSON";

        // 1) 숫자만 남겨서 포맷 정리
        String bNo     = doc.getBusinessNumber().replaceAll("\\D+", "");
        String startDt = doc.getStartDate().replaceAll("\\D+", "");

        // 2) 페이로드 준비
        Map<String,String> biz = makeBiz(bNo, startDt, doc.getOwnerName());

        Map<String,Object> payload = Map.of("businesses", List.of(biz));
        log.info("Verify payload: {}", payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> request = new HttpEntity<>(payload, headers);

        // 3) 호출
        ResponseEntity<ValidateResponse> resp =
                restTemplate.postForEntity(url, request, ValidateResponse.class);

        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("진위확인 API 오류: HTTP " + resp.getStatusCode());
        }

        ValidateResponse body = resp.getBody();

        // 에러 응답 처리
        if (!"OK".equals(body.getStatusCode())) {
            log.error("진위확인 API 에러 status_code={}", body.getStatusCode());
            return null;
        }

        // 정상 응답이지만 데이터가 없으면 예외
        if (body.getData() == null || body.getData().isEmpty()) {
            throw new RuntimeException("진위확인 결과 없음");
        }

        // 첫 번째 결과를 꺼내서 DTO에 담아 반환
        ValidateResponse.BusinessData entry = body.getData().get(0);
        doc.setVerificationStatus(entry.getValid());
        doc.setVerificationMessage(entry.getValidMsg());
        return entry;
    }

    /**
     * 배치 코드용 alias
     */
    public ValidateResponse.BusinessData checkBusiness(BusinessDocument doc) {
        return verify(doc);
    }

}

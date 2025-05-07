package com.kyohwee.ojt.domain.service.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kyohwee.ojt.domain.dto.ClovaOcrResponseDto;
import com.kyohwee.ojt.domain.service.ocr.ClovaOcrService.OcrRequest.Image;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClovaOcrService {

    private final RestTemplate restTemplate;

    /** application.yml 에 설정된 OCR 엔드포인트 (예: https://naveropenapi.apigw.ntruss.com/vision-ocr/v1/ocr) */
    @Value("${clova.ocr.url}")
    private String ocrUrl;

    /** API Key Secret */
    @Value("${clova.ocr.client-secret}")
    private String clientSecret;

    /**
     * 외부 이미지 URL 한 개를 OCR API에 보내고, 전체 응답을 DTO로 반환한다.
     *
     * @param imageUrl 이미지가 접근 가능한 public URL
     * @return OCR API가 돌려준 full response
     */
    public ClovaOcrResponseDto extractText(String imageUrl) {
        // 1) 헤더 준비
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        // 2) 요청 바디 준비
        Image img = new Image();
        img.setUrl(imageUrl);
        img.setFormat(detectFormat(imageUrl));   // ex) "jpg", "png"

        OcrRequest req = new OcrRequest();
        req.setRequestId(UUID.randomUUID().toString());
        req.setTimestamp(System.currentTimeMillis());
        req.setVersion("V2");
        req.setImages(Collections.singletonList(img));

        HttpEntity<OcrRequest> httpEntity = new HttpEntity<>(req, headers);

        // 3) API 호출
        ResponseEntity<ClovaOcrResponseDto> resp =
                restTemplate.exchange(ocrUrl, HttpMethod.POST, httpEntity, ClovaOcrResponseDto.class);

        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("Clova OCR API 호출 실패: HTTP " + resp.getStatusCode());
        }

        return resp.getBody();
    }

    /** URL 끝 확장자에서 간단히 포맷을 추출 (jpg/png 등) */
    private String detectFormat(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".png")) return "png";
        if (lower.endsWith(".gif")) return "gif";
        // 기본은 jpeg
        return "jpg";
    }

    // --- request DTO ---
    @Data
    public static class OcrRequest {
        private String requestId;
        private long timestamp;
        private String version;           // "V1" 또는 "V2"
        private List<Image> images;

        @Data
        public static class Image {
            private String format;        // jpg, png, gif
            private String url;           // public image URL
            private String name;          // optional
        }
    }
}

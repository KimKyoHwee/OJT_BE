package com.kyohwee.ojt.domain.service.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kyohwee.ojt.domain.dto.ClovaOcrResponseDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Clova Document OCR v2 서비스 구현
 */
@Service
@RequiredArgsConstructor
public class ClovaOcrService {

    private final RestTemplate restTemplate;

    /** Document OCR v2 엔드포인트 URL (PDF/문서 지원) */
    @Value("${clova.ocr.document-url}")
    private String documentOcrUrl;

    /** API Key ID (X-NCP-APIGW-API-KEY-ID) */
    @Value("${clova.ocr.client-id}")
    private String clientId;

    /** API Key Secret (X-OCR-SECRET) */
    @Value("${clova.ocr.client-secret}")
    private String clientSecret;

    /**
     * S3 URL에서 이미지를 다운로드 받아 Base64로 인코딩
     *
     * @param imageUrl S3 등에서 접근 가능한 public URL
     * @return Base64 인코딩 문자열
     */
    public String downloadImageAsBase64(String imageUrl) {
        ResponseEntity<byte[]> resp = restTemplate.getForEntity(imageUrl, byte[].class);
        byte[] imageBytes = resp.getBody();
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalStateException("이미지 다운로드 실패: " + imageUrl);
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Document OCR v2 API에 요청하여 전체 응답을 DTO로 반환
     *
     * @param imageUrl S3 등에서 접근 가능한 public URL
     * @return Clova OCR 응답 DTO
     */
    public ClovaOcrResponseDto extractText(String imageUrl) {
        // 1) 이미지 다운로드 및 Base64 인코딩
        String base64 = downloadImageAsBase64(imageUrl);

        // 2) 헤더 설정 (X-OCR-SECRET)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-OCR-SECRET", clientSecret);

        // 3) 요청 바디 생성
        OcrRequest.Image img = new OcrRequest.Image();
        img.setFormat(detectFormat(imageUrl));
        img.setName("img-" + UUID.randomUUID());
        img.setData(base64);

        OcrRequest req = new OcrRequest();
        req.setRequestId(UUID.randomUUID().toString());
        req.setTimestamp(System.currentTimeMillis());
        req.setVersion("V2");
        req.setImages(Collections.singletonList(img));

        HttpEntity<OcrRequest> entity = new HttpEntity<>(req, headers);

        // 4) Document OCR v2 호출
        ResponseEntity<ClovaOcrResponseDto> resp = restTemplate.exchange(
                documentOcrUrl,
                HttpMethod.POST,
                entity,
                ClovaOcrResponseDto.class
        );
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            throw new RuntimeException("Clova Document OCR API 호출 실패: HTTP " + resp.getStatusCode());
        }
        return resp.getBody();
    }

    /** URL 끝 확장자에서 포맷 추출 (jpg, png, pdf 등) */
    private String detectFormat(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".png"))  return "png";
        if (lower.endsWith(".gif"))  return "gif";
        if (lower.endsWith(".jpeg")) return "jpeg";
        if (lower.endsWith(".jpg"))  return "jpeg";
        if (lower.endsWith(".pdf"))  return "pdf";
        return "jpeg";
    }

    // --- request DTO ---
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OcrRequest {
        private String requestId;
        private long timestamp;
        private String version;
        private List<Image> images;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Image {
            private String format;
            private String name;
            private String data;
        }
    }
}

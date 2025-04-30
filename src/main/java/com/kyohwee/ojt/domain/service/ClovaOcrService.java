package com.kyohwee.ojt.domain.service;

import com.kyohwee.ojt.domain.dto.ClovaOcrResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ClovaOcrService {

    private final RestTemplate restTemplate;

    // application.yml (or .properties)에 아래 값들을 설정해주세요.
    @Value("${clova.ocr.url}")
    private String ocrUrl; // ex) https://naveropenapi.apigw.ntruss.com/vision-ocr/v1/ocr

    @Value("${clova.ocr.client-id}")
    private String clientId;

    @Value("${clova.ocr.client-secret}")
    private String clientSecret;

    /**
     * 주어진 이미지 바이트 배열을 Clova OCR에 보내어 텍스트를 추출합니다.
     *
     * @param imageBytes JPEG/PNG 등 이미지 바이트 배열
     * @return OCR 결과를 담은 DTO
     */
    public ClovaOcrResponseDto extractText(byte[] imageBytes) {
        // 1) 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        // 2) multipart body 구성
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // ByteArrayResource를 이용해 MultipartFile처럼 보냄
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "image.jpg"; // 클라이언트에서 받을 파라미터 이름과 파일명
            }
        };
        body.add("image", imageResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 3) API 호출
        ResponseEntity<ClovaOcrResponseDto> response = restTemplate.exchange(
                ocrUrl,
                HttpMethod.POST,
                requestEntity,
                ClovaOcrResponseDto.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Clova OCR API 호출 실패: " + response.getStatusCode());
        }

        return response.getBody();
    }
}
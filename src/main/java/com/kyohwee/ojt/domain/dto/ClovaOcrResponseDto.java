package com.kyohwee.ojt.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ClovaOcrResponseDto {
    private String requestId;
    private long timestamp;
    private List<ImageResult> images;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageResult {
        private String format;
        private int width;
        private int height;
        private List<Field> fields;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Field {
            private String inferText;           // 인식된 텍스트
            private List<Integer> boundingPoly; // 좌표 정보 [x1,y1,x2,y2,...]
            // 필요하다면 confidence, lineBreak 등도 추가 가능
        }
    }
}

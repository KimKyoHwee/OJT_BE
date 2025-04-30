package com.kyohwee.ojt.domain.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ClovaOcrResponseDto {
    private String version;
    private String requestId;
    private long timestamp;
    private List<OcrImage> images;

    @Data
    public static class OcrImage {
        private String name;
        private String inferResult;
        private List<OcrField> fields;
    }

    @Data
    public static class OcrField {
        private String inferText;
        private List<List<Integer>> lineBorder;
        private List<List<Integer>> words;
        private Float confidence;
    }
}

package com.kyohwee.ojt.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Clova Document OCR v2 응답 DTO (사업자등록증 포함)
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ClovaOcrResponseDto {
    private String version;
    private String requestId;
    private long timestamp;
    private List<ImageResponse> images;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageResponse {
        private String uid;
        private String name;
        private String inferResult;
        private String message;
        private BizLicense bizLicense;
        private ValidationResult validationResult;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BizLicense {
        private Meta meta;
        private Result result;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private String estimatedLanguage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<Field> companyName;
        private List<Field> repName;
        private List<Field> registerNumber;
        private List<Field> openDate;
        private List<Field> issuanceDate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Field {
        private String text;
        private Formatted formatted;
        private List<BoundingPoly> boundingPolys;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Formatted {
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoundingPoly {
        private List<Vertex> vertices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vertex {
        private double x;
        private double y;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationResult {
        private String name;
        private String status;
        private String message;
    }
}

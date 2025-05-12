package com.kyohwee.ojt.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 공공데이터포털 “사업자등록증 진위확인(validate)” API 응답 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateResponse {

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

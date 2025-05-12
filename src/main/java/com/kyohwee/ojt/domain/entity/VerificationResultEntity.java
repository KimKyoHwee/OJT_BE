package com.kyohwee.ojt.domain.entity;

import com.kyohwee.ojt.domain.dto.ValidateResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verification_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사업자등록번호 */
    @Column(name = "b_no", length = 20)
    private String bNo;

    /** 사업자 상태 (계속사업자, 폐업 등) */
    @Column(name = "b_stt", length = 50)
    private String bStt;

    /** 상태 코드 (01=계속, 02=휴업, 03=폐업) */
    @Column(name = "b_stt_cd", length = 5)
    private String bSttCd;

    /** 과세 유형 (예: 부가가치세 일반과세자) */
    @Column(name = "tax_type", length = 100)
    private String taxType;

    /** 과세 유형 코드 */
    @Column(name = "tax_type_cd", length = 5)
    private String taxTypeCd;

    /** 폐업일자 or 최종일자 */
    @Column(name = "end_dt", length = 20)
    private String endDt;

    /** 세금계산서 발행 여부 (Y/N) */
    @Column(name = "utcc_yn", length = 2)
    private String utccYn;

    /** 과세 유형 변경일자 */
    @Column(name = "tax_type_change_dt", length = 20)
    private String taxTypeChangeDt;

    /** 세금계산서 적용일자 */
    @Column(name = "invoice_apply_dt", length = 20)
    private String invoiceApplyDt;

    /** 과거 과세 유형 (리포맷을 위해) */
    @Column(name = "rbf_tax_type", length = 100)
    private String rbfTaxType;

    /** 과거 과세 유형 코드 */
    @Column(name = "rbf_tax_type_cd", length = 5)
    private String rbfTaxTypeCd;

    public static VerificationResultEntity from(ValidateResponse.BusinessData data) {
        ValidateResponse.BusinessData.Status s = data.getStatus();
        if (s == null) {
            throw new IllegalArgumentException("BusinessData.status가 비어있습니다.");
        }
        return VerificationResultEntity.builder()
                .bNo(s.getBNo())
                .bStt(s.getBStt())
                .bSttCd(s.getBSttCd())
                .taxType(s.getTaxType())
                .taxTypeCd(s.getTaxTypeCd())
                .endDt(s.getEndDt())
                .utccYn(s.getUtccYn())
                .taxTypeChangeDt(s.getTaxTypeChangeDt())
                .invoiceApplyDt(s.getInvoiceApplyDt())
                .rbfTaxType(s.getRbfTaxType())
                .rbfTaxTypeCd(s.getRbfTaxTypeCd())
                .build();
    }
}

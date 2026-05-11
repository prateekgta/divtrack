package io.divtrack.dividend.application.dto;

import io.divtrack.dividend.domain.model.BillMapping;
import java.math.BigDecimal;

public record BillMappingDto(String id, String ticker, String billName, BigDecimal billAmount) {
    public static BillMappingDto from(BillMapping bm, String ticker) {
        return new BillMappingDto(bm.getId(), ticker, bm.getBillName(), bm.getBillAmount());
    }
}

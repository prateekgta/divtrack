package io.divtrack.portfolio.domain.service;

import io.divtrack.common.BusinessException;

public class HoldingNotFoundException extends BusinessException {
    public HoldingNotFoundException(String id) {
        super("HOLDING_NOT_FOUND", "Holding not found: " + id, 404);
    }
}

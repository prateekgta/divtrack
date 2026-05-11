package io.divtrack.portfolio.domain.service;

import io.divtrack.common.BusinessException;

public class PlanLimitExceededException extends BusinessException {
    public PlanLimitExceededException(String message) {
        super("PLAN_LIMIT", message, 403);
    }
}

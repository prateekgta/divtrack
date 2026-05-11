package io.divtrack.identity.application.dto;

public record UserDto(String id, String email, String name, String plan, java.math.BigDecimal monthlyExpenses) {
    public static UserDto from(io.divtrack.identity.domain.model.User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName(), user.getPlan(), user.getMonthlyExpenses());
    }
}

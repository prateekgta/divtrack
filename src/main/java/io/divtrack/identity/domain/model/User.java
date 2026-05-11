package io.divtrack.identity.domain.model;

import io.divtrack.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String plan = "FREE";

    @Column(name = "monthly_expenses")
    private java.math.BigDecimal monthlyExpenses;

    public User() {}

    public User(String email, String passwordHash, String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.plan = "FREE";
    }

    public boolean isPro() { return "PRO".equals(plan); }
    public void upgradeToPro() { this.plan = "PRO"; }

    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getPlan() { return plan; }
    public java.math.BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(java.math.BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }
}

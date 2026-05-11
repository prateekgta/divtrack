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

    @Column(name = "security_question_1")
    private String securityQuestion1;

    @Column(name = "security_answer_1")
    private String securityAnswer1;

    @Column(name = "security_question_2")
    private String securityQuestion2;

    @Column(name = "security_answer_2")
    private String securityAnswer2;

    @Column(name = "reset_token_hash", length = 64)
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private java.time.OffsetDateTime resetTokenExpiresAt;

    public User() {}

    public User(String email, String passwordHash, String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.plan = "FREE";
    }

    public boolean hasSecurityQuestions() {
        return securityQuestion1 != null && !securityQuestion1.isBlank();
    }

    public boolean verifySecurityAnswer(int num, String answer) {
        if (answer == null) return false;
        String stored = num == 1 ? securityAnswer1 : securityAnswer2;
        return stored != null && stored.equalsIgnoreCase(answer.trim());
    }

    public boolean isResetTokenValid(String hash) {
        return hash != null && hash.equals(resetTokenHash)
                && resetTokenExpiresAt != null
                && java.time.OffsetDateTime.now().isBefore(resetTokenExpiresAt);
    }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setResetToken(String hash, java.time.OffsetDateTime expiresAt) {
        this.resetTokenHash = hash;
        this.resetTokenExpiresAt = expiresAt;
    }
    public void clearResetToken() {
        this.resetTokenHash = null;
        this.resetTokenExpiresAt = null;
    }

    public boolean isPro() { return "PRO".equals(plan); }
    public void upgradeToPro() { this.plan = "PRO"; }

    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getPlan() { return plan; }
    public java.math.BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(java.math.BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }
    public String getSecurityQuestion1() { return securityQuestion1; }
    public void setSecurityQuestion1(String q) { this.securityQuestion1 = q; }
    public void setSecurityAnswer1(String a) { this.securityAnswer1 = a; }
    public String getSecurityQuestion2() { return securityQuestion2; }
    public void setSecurityQuestion2(String q) { this.securityQuestion2 = q; }
    public void setSecurityAnswer2(String a) { this.securityAnswer2 = a; }
}

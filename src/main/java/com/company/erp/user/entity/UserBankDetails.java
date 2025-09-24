package com.company.erp.user.entity;

import com.company.erp.common.entity.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_bank_details")
public class UserBankDetails extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 100)
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Size(max = 50)
    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Size(max = 34)
    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "beneficiary_address", columnDefinition = "TEXT")
    private String beneficiaryAddress;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    // Constructors
    public UserBankDetails() {}

    public UserBankDetails(User user) {
        this.user = user;
    }

    // Utility methods
    public void markAsVerified() {
        this.verified = true;
        this.verificationDate = LocalDateTime.now();
    }

    public void markAsUnverified() {
        this.verified = false;
        this.verificationDate = null;
    }

    public boolean isComplete() {
        return bankName != null && !bankName.trim().isEmpty() &&
                accountNumber != null && !accountNumber.trim().isEmpty() &&
                beneficiaryAddress != null && !beneficiaryAddress.trim().isEmpty();
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBeneficiaryAddress() {
        return beneficiaryAddress;
    }

    public void setBeneficiaryAddress(String beneficiaryAddress) {
        this.beneficiaryAddress = beneficiaryAddress;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }

    @Override
    public String toString() {
        return "UserBankDetails{" +
                "id=" + getId() +
                ", bankName='" + bankName + '\'' +
                ", accountNumber='" + (accountNumber != null ? "***" + accountNumber.substring(Math.max(0, accountNumber.length() - 4)) : null) + '\'' +
                ", verified=" + verified +
                '}';
    }
}
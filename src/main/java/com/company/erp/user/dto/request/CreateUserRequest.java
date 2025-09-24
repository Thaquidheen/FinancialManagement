package com.company.erp.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

// Create User Request DTO
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*]).*$",
            message = "Password must contain at least one number and one special character")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 20, message = "Iqama ID must not exceed 20 characters")
    private String iqamaId;

    @Size(max = 20, message = "National ID must not exceed 20 characters")
    private String nationalId;

    @Size(max = 20, message = "Passport number must not exceed 20 characters")
    private String passportNumber;

    @Size(max = 50, message = "Department must not exceed 50 characters")
    private String department;

    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;

    private LocalDate hireDate;

    private Long managerId;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    @Valid
    private BankDetailsRequest bankDetails;

    // Constructors
    public CreateUserRequest() {}

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIqamaId() {
        return iqamaId;
    }

    public void setIqamaId(String iqamaId) {
        this.iqamaId = iqamaId;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public BankDetailsRequest getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetailsRequest bankDetails) {
        this.bankDetails = bankDetails;
    }

    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", roles=" + roles +
                '}';
    }

    // Nested Bank Details Request
    public static class BankDetailsRequest {

        @Size(max = 100, message = "Bank name must not exceed 100 characters")
        private String bankName;

        @Size(max = 50, message = "Account number must not exceed 50 characters")
        private String accountNumber;

        @Size(max = 34, message = "IBAN must not exceed 34 characters")
        private String iban;

        private String beneficiaryAddress;

        public BankDetailsRequest() {}

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
    }
}




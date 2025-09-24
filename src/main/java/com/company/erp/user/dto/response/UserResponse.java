package com.company.erp.user.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String iqamaId;
    private String nationalId;
    private String passportNumber;
    private String department;
    private String position;
    private LocalDate hireDate;
    private Long managerId;
    private String managerName;
    private Set<String> roles;
    private Boolean active;
    private Boolean accountLocked;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdDate;
    private BankDetailsResponse bankDetails;

    // Constructors
    public UserResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public BankDetailsResponse getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetailsResponse bankDetails) {
        this.bankDetails = bankDetails;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", roles=" + roles +
                ", active=" + active +
                '}';
    }

    // Nested Bank Details Response
    public static class BankDetailsResponse {

        private String bankName;
        private String accountNumber;
        private String iban;
        private String beneficiaryAddress;
        private Boolean verified;

        public BankDetailsResponse() {}

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
    }
}

// User Summary Response DTO (for lists and dropdowns)
class UserSummaryResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String department;
    private String position;
    private Set<String> roles;
    private Boolean active;

    public UserSummaryResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

// User Statistics Response DTO
class UserStatisticsResponse {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long projectManagers;
    private long accountManagers;
    private long employees;
    private long lockedAccounts;
    private DepartmentStatistics departmentStatistics;

    public UserStatisticsResponse() {}

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getProjectManagers() {
        return projectManagers;
    }

    public void setProjectManagers(long projectManagers) {
        this.projectManagers = projectManagers;
    }

    public long getAccountManagers() {
        return accountManagers;
    }

    public void setAccountManagers(long accountManagers) {
        this.accountManagers = accountManagers;
    }

    public long getEmployees() {
        return employees;
    }

    public void setEmployees(long employees) {
        this.employees = employees;
    }

    public long getLockedAccounts() {
        return lockedAccounts;
    }

    public void setLockedAccounts(long lockedAccounts) {
        this.lockedAccounts = lockedAccounts;
    }

    public DepartmentStatistics getDepartmentStatistics() {
        return departmentStatistics;
    }

    public void setDepartmentStatistics(DepartmentStatistics departmentStatistics) {
        this.departmentStatistics = departmentStatistics;
    }

    // Nested Department Statistics
    public static class DepartmentStatistics {
        private java.util.Map<String, Long> departmentCounts;

        public DepartmentStatistics() {}

        public java.util.Map<String, Long> getDepartmentCounts() {
            return departmentCounts;
        }

        public void setDepartmentCounts(java.util.Map<String, Long> departmentCounts) {
            this.departmentCounts = departmentCounts;
        }
    }
}
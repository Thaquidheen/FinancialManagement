package com.company.erp.user.service;

import com.company.erp.common.exception.BusinessException;
import com.company.erp.common.exception.DuplicateResourceException;
import com.company.erp.common.exception.ResourceNotFoundException;
import com.company.erp.user.dto.request.CreateUserRequest;
import com.company.erp.user.dto.request.UpdateUserRequest;
import com.company.erp.user.dto.response.RoleResponse;
import com.company.erp.user.dto.response.UserResponse;
import com.company.erp.user.entity.Role;
import com.company.erp.user.entity.User;
import com.company.erp.user.entity.UserBankDetails;
import com.company.erp.user.repository.RoleRepository;
import com.company.erp.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create new user
     */
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Creating new user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIqamaId(request.getIqamaId());
        user.setNationalId(request.getNationalId());
        user.setPassportNumber(request.getPassportNumber());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setHireDate(request.getHireDate());

        // Set manager if provided
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getManagerId()));
            user.setManager(manager);
        }

        // Add roles
        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByNameAndActiveTrue(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        // Create bank details if provided
        if (request.getBankDetails() != null) {
            UserBankDetails bankDetails = new UserBankDetails();
            bankDetails.setBankName(request.getBankDetails().getBankName());
            bankDetails.setAccountNumber(request.getBankDetails().getAccountNumber());
            bankDetails.setIban(request.getBankDetails().getIban());
            bankDetails.setBeneficiaryAddress(request.getBankDetails().getBeneficiaryAddress());
            user.setBankDetails(bankDetails);
        }

        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getUsername());

        return convertToUserResponse(savedUser);
    }

    /**
     * Update existing user
     */
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check username uniqueness if changed
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check email uniqueness if changed
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Update user fields
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIqamaId(request.getIqamaId());
        user.setNationalId(request.getNationalId());
        user.setPassportNumber(request.getPassportNumber());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setHireDate(request.getHireDate());

        // Update manager if provided
        if (request.getManagerId() != null) {
            if (!request.getManagerId().equals(userId)) { // Prevent self-assignment
                User manager = userRepository.findById(request.getManagerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getManagerId()));
                user.setManager(manager);
            }
        } else {
            user.setManager(null);
        }

        User savedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", savedUser.getUsername());

        return convertToUserResponse(savedUser);
    }

    /**
     * Update user roles
     */
    public UserResponse updateUserRoles(Long userId, Set<String> roleNames) {
        logger.info("Updating roles for user ID: {}", userId);

        User user = userRepository.findByIdWithRolesAndBankDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Clear existing roles
        user.getRoles().clear();

        // Add new roles
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByNameAndActiveTrue(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
            newRoles.add(role);
        }
        user.setRoles(newRoles);

        User savedUser = userRepository.save(user);
        logger.info("Roles updated successfully for user: {}", savedUser.getUsername());

        return convertToUserResponse(savedUser);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdWithRolesAndBankDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllActiveRoles() {
        logger.debug("Fetching all active roles");
        return roleRepository.findByActiveTrue();
    }

    /**
     * Get all active roles with full details
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllActiveRolesDetailed() {
        logger.debug("Fetching all active roles with details");
        List<Role> roles = roleRepository.findByActiveTrue();

        return roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Role entity to RoleResponse DTO (if you need it)
     */
    private RoleResponse convertToRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setDisplayName(role.getName().replace("_", " "));
        response.setDescription(role.getDescription());
        response.setActive(role.getActive());
        response.setCreatedDate(role.getCreatedDate());
        return response;
    }


    /**
     * Find User entity by ID (for internal services needing entity access)
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Find all active users (entity list)
     */
    @Transactional(readOnly = true)
    public List<User> findAllActive() {
        return userRepository.findByActiveTrue();
    }

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * Search users by criteria
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String fullName, String department, Boolean active, Pageable pageable) {
        Page<User> users = userRepository.findBySearchCriteria(fullName, department, active, pageable);
        return users.map(this::convertToUserResponse);
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String roleName) {
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get project managers
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getProjectManagers() {
        List<User> projectManagers = userRepository.findActiveProjectManagers();
        return projectManagers.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get account managers
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAccountManagers() {
        List<User> accountManagers = userRepository.findActiveAccountManagers();
        return accountManagers.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(false);
        userRepository.save(user);

        logger.info("User deactivated: {}", user.getUsername());
    }

    /**
     * Activate user
     */
    public void activateUser(Long userId) {
        logger.info("Activating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(true);
        user.setAccountLocked(false); // Also unlock if needed
        userRepository.save(user);

        logger.info("User activated: {}", user.getUsername());
    }

    /**
     * Delete user (soft delete)
     */
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if user has assigned projects
        // This will be implemented in project service

        user.setActive(false);
        userRepository.save(user);

        logger.info("User deleted (soft): {}", user.getUsername());
    }

    /**
     * Update user bank details
     */
    public UserResponse updateBankDetails(Long userId, UserBankDetails bankDetailsRequest) {
        logger.info("Updating bank details for user ID: {}", userId);

        User user = userRepository.findByIdWithRolesAndBankDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getBankDetails() == null) {
            user.setBankDetails(new UserBankDetails(user));
        }

        UserBankDetails bankDetails = user.getBankDetails();
        bankDetails.setBankName(bankDetailsRequest.getBankName());
        bankDetails.setAccountNumber(bankDetailsRequest.getAccountNumber());
        bankDetails.setIban(bankDetailsRequest.getIban());
        bankDetails.setBeneficiaryAddress(bankDetailsRequest.getBeneficiaryAddress());

        User savedUser = userRepository.save(user);
        logger.info("Bank details updated for user: {}", savedUser.getUsername());

        return convertToUserResponse(savedUser);
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.countActiveUsers();
        long projectManagers = userRepository.countUsersByRole("PROJECT_MANAGER");
        long accountManagers = userRepository.countUsersByRole("ACCOUNT_MANAGER");
        long employees = userRepository.countUsersByRole("EMPLOYEE");

        List<Object[]> departmentCounts = userRepository.countUsersByDepartment();

        return new UserStatistics(totalUsers, projectManagers, accountManagers, employees, departmentCounts);
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setIqamaId(user.getIqamaId());
        response.setNationalId(user.getNationalId());
        response.setPassportNumber(user.getPassportNumber());
        response.setDepartment(user.getDepartment());
        response.setPosition(user.getPosition());
        response.setHireDate(user.getHireDate());
        response.setActive(user.getActive());
        response.setAccountLocked(user.getAccountLocked());
        response.setLastLoginDate(user.getLastLoginDate());
        response.setCreatedDate(user.getCreatedDate());

        if (user.getManager() != null) {
            response.setManagerId(user.getManager().getId());
            response.setManagerName(user.getManager().getFullName());
        }

        response.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        if (user.getBankDetails() != null) {
            UserResponse.BankDetailsResponse bankDetails = new UserResponse.BankDetailsResponse();
            bankDetails.setBankName(user.getBankDetails().getBankName());
            bankDetails.setAccountNumber(user.getBankDetails().getAccountNumber());
            bankDetails.setIban(user.getBankDetails().getIban());
            bankDetails.setBeneficiaryAddress(user.getBankDetails().getBeneficiaryAddress());
            bankDetails.setVerified(user.getBankDetails().getVerified());
            response.setBankDetails(bankDetails);
        }

        return response;
    }

    // Inner class for statistics
    public static class UserStatistics {
        private long totalUsers;
        private long projectManagers;
        private long accountManagers;
        private long employees;
        private List<Object[]> departmentCounts;

        public UserStatistics(long totalUsers, long projectManagers, long accountManagers,
                              long employees, List<Object[]> departmentCounts) {
            this.totalUsers = totalUsers;
            this.projectManagers = projectManagers;
            this.accountManagers = accountManagers;
            this.employees = employees;
            this.departmentCounts = departmentCounts;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getProjectManagers() { return projectManagers; }
        public long getAccountManagers() { return accountManagers; }
        public long getEmployees() { return employees; }
        public List<Object[]> getDepartmentCounts() { return departmentCounts; }
    }
}
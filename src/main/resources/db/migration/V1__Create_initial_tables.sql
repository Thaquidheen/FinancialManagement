-- V1__Create_initial_tables.sql
-- Initial database schema for ERP system

-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       phone_number VARCHAR(20),
                       iqama_id VARCHAR(20),
                       national_id VARCHAR(20),
                       passport_number VARCHAR(20),
                       department VARCHAR(50),
                       position VARCHAR(50),
                       manager_id BIGINT,
                       hire_date DATE,
                       active BOOLEAN NOT NULL DEFAULT true,
                       account_locked BOOLEAN NOT NULL DEFAULT false,
                       password_expired BOOLEAN NOT NULL DEFAULT false,
                       failed_login_attempts INTEGER DEFAULT 0,
                       last_login_date TIMESTAMP,
                       created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(50) DEFAULT 'system',
                       last_modified_by VARCHAR(50) DEFAULT 'system',
                       FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- Roles table
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description VARCHAR(255),
                       active BOOLEAN NOT NULL DEFAULT true,
                       created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by VARCHAR(50) DEFAULT 'system',
                       last_modified_by VARCHAR(50) DEFAULT 'system'
);

-- User roles junction table
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            assigned_by VARCHAR(50) DEFAULT 'system',
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- User bank details table
CREATE TABLE user_bank_details (
                                   id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL UNIQUE,
                                   bank_name VARCHAR(100),
                                   account_number VARCHAR(50),
                                   iban VARCHAR(34),
                                   beneficiary_address TEXT,
                                   verified BOOLEAN NOT NULL DEFAULT false,
                                   verification_date TIMESTAMP,
                                   created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   created_by VARCHAR(50) DEFAULT 'system',
                                   last_modified_by VARCHAR(50) DEFAULT 'system',
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Projects table
CREATE TABLE projects (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          location VARCHAR(200),
                          allocated_budget DECIMAL(15,2) NOT NULL DEFAULT 0,
                          spent_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                          remaining_budget DECIMAL(15,2) NOT NULL DEFAULT 0,
                          currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          start_date DATE,
                          end_date DATE,
                          completion_percentage DECIMAL(5,2) DEFAULT 0,
                          manager_id BIGINT,
                          active BOOLEAN NOT NULL DEFAULT true,
                          created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          created_by VARCHAR(50) DEFAULT 'system',
                          last_modified_by VARCHAR(50) DEFAULT 'system',
                          FOREIGN KEY (manager_id) REFERENCES users(id),
                          CHECK (status IN ('ACTIVE', 'COMPLETED', 'ON_HOLD', 'CANCELLED')),
                          CHECK (allocated_budget >= 0),
                          CHECK (spent_amount >= 0),
                          CHECK (completion_percentage >= 0 AND completion_percentage <= 100)
);

-- Quotations table
CREATE TABLE quotations (
                            id BIGSERIAL PRIMARY KEY,
                            project_id BIGINT NOT NULL,
                            created_by BIGINT NOT NULL,
                            total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                            currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
                            status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
                            description TEXT,
                            submission_notes TEXT,
                            submitted_date TIMESTAMP,
                            approved_date TIMESTAMP,
                            approved_by BIGINT,
                            rejection_reason TEXT,
                            active BOOLEAN NOT NULL DEFAULT true,
                            created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            created_by_name VARCHAR(50) DEFAULT 'system',
                            last_modified_by VARCHAR(50) DEFAULT 'system',
                            FOREIGN KEY (project_id) REFERENCES projects(id),
                            FOREIGN KEY (created_by) REFERENCES users(id),
                            FOREIGN KEY (approved_by) REFERENCES users(id),
                            CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'PAYMENT_FILE_GENERATED', 'SENT_TO_BANK', 'PAID')),
                            CHECK (total_amount >= 0)
);

-- Quotation items table
CREATE TABLE quotation_items (
                                 id BIGSERIAL PRIMARY KEY,
                                 quotation_id BIGINT NOT NULL,
                                 description TEXT NOT NULL,
                                 amount DECIMAL(15,2) NOT NULL,
                                 currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
                                 category VARCHAR(50),
                                 account_head VARCHAR(100),
                                 item_date DATE,
                                 vendor_name VARCHAR(200),
                                 vendor_contact VARCHAR(100),
                                 item_order INTEGER DEFAULT 0,
                                 active BOOLEAN NOT NULL DEFAULT true,
                                 created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 created_by VARCHAR(50) DEFAULT 'system',
                                 last_modified_by VARCHAR(50) DEFAULT 'system',
                                 FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE,
                                 CHECK (amount > 0)
);

-- Approvals table
CREATE TABLE approvals (
                           id BIGSERIAL PRIMARY KEY,
                           quotation_id BIGINT NOT NULL,
                           approver_id BIGINT NOT NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                           comments TEXT,
                           approval_date TIMESTAMP,
                           level_order INTEGER DEFAULT 1,
                           active BOOLEAN NOT NULL DEFAULT true,
                           created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           created_by VARCHAR(50) DEFAULT 'system',
                           last_modified_by VARCHAR(50) DEFAULT 'system',
                           FOREIGN KEY (quotation_id) REFERENCES quotations(id) ON DELETE CASCADE,
                           FOREIGN KEY (approver_id) REFERENCES users(id),
                           CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CHANGES_REQUESTED'))
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_projects_manager_id ON projects(manager_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_quotations_project_id ON quotations(project_id);
CREATE INDEX idx_quotations_created_by ON quotations(created_by);
CREATE INDEX idx_quotations_status ON quotations(status);
CREATE INDEX idx_quotation_items_quotation_id ON quotation_items(quotation_id);
CREATE INDEX idx_approvals_quotation_id ON approvals(quotation_id);
CREATE INDEX idx_approvals_approver_id ON approvals(approver_id);
CREATE INDEX idx_approvals_status ON approvals(status);

-- Insert default roles
INSERT INTO roles (name, description) VALUES
                                          ('SUPER_ADMIN', 'Super Administrator with full system access'),
                                          ('PROJECT_MANAGER', 'Project Manager with project and quotation management rights'),
                                          ('ACCOUNT_MANAGER', 'Account Manager with approval and payment processing rights'),
                                          ('EMPLOYEE', 'Regular employee with basic system access');

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, full_name, active) VALUES
    ('admin', 'admin@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM0w1I00LFO6Cg7OWtQa', 'System Administrator', true);

-- Assign SUPER_ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'));
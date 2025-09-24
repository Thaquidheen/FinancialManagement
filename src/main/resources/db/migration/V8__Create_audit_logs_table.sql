-- V8__Create_audit_logs_table.sql
-- Create audit logs table for Phase 7 audit trail functionality

CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                            action_type VARCHAR(50) NOT NULL,
                            entity_type VARCHAR(50) NOT NULL,
                            entity_id BIGINT,
                            old_values TEXT,
                            new_values TEXT,
                            description TEXT,
                            ip_address VARCHAR(45),
                            user_agent TEXT,
                            session_id VARCHAR(100),
                            created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            severity VARCHAR(20) DEFAULT 'LOW',
                            category VARCHAR(50) DEFAULT 'GENERAL',
                            project_id BIGINT REFERENCES projects(id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_created_date ON audit_logs(created_date);
CREATE INDEX idx_audit_logs_severity ON audit_logs(severity);
CREATE INDEX idx_audit_logs_category ON audit_logs(category);
CREATE INDEX idx_audit_logs_project_id ON audit_logs(project_id);

-- Composite index for common filter combinations
CREATE INDEX idx_audit_logs_filters ON audit_logs(project_id, action_type, created_date);
CREATE INDEX idx_audit_logs_user_date ON audit_logs(user_id, created_date);

-- Add comments for documentation
COMMENT ON TABLE audit_logs IS 'Audit trail for all system activities';
COMMENT ON COLUMN audit_logs.action_type IS 'Type of action performed (CREATE, UPDATE, DELETE, APPROVE, etc.)';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity affected (PROJECT, QUOTATION, BUDGET_TRACKING, etc.)';
COMMENT ON COLUMN audit_logs.old_values IS 'JSON representation of entity state before change';
COMMENT ON COLUMN audit_logs.new_values IS 'JSON representation of entity state after change';
COMMENT ON COLUMN audit_logs.severity IS 'Severity level: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN audit_logs.category IS 'Category: FINANCIAL, SECURITY, OPERATIONAL, GENERAL';
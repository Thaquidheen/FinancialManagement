-- V9__Enhanced_notifications_system.sql
-- Create enhanced notification system tables for Phase 8

-- Base notifications table (created if missing so ALTER below succeeds)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    sent BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Update notifications table with enhanced fields
ALTER TABLE IF EXISTS notifications
    ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'MEDIUM',
    ADD COLUMN IF NOT EXISTS channel VARCHAR(20) DEFAULT 'IN_APP',
    ADD COLUMN IF NOT EXISTS scheduled_time TIMESTAMP,
    ADD COLUMN IF NOT EXISTS action_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS retry_count INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS error_message TEXT;

-- Ensure notification_templates exists for seed data below
CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    email_subject VARCHAR(255),
    email_body TEXT,
    sms_template VARCHAR(500),
    in_app_template TEXT,
    active BOOLEAN DEFAULT TRUE,
    language VARCHAR(5) DEFAULT 'en'
);

-- Create notification template data table
CREATE TABLE IF NOT EXISTS notification_template_data (
                                                          notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    data_key VARCHAR(100) NOT NULL,
    data_value TEXT,
    PRIMARY KEY (notification_id, data_key)
    );

-- Create notification preferences table
CREATE TABLE IF NOT EXISTS notification_preferences (
                                                        id BIGSERIAL PRIMARY KEY,
                                                        user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    email_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT FALSE,
    daily_summary_enabled BOOLEAN DEFAULT TRUE,
    weekly_summary_enabled BOOLEAN DEFAULT TRUE,
    do_not_disturb_enabled BOOLEAN DEFAULT FALSE,
    do_not_disturb_start TIME,
    do_not_disturb_end TIME,
    language VARCHAR(5) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'Asia/Riyadh',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create user notification types junction table
CREATE TABLE IF NOT EXISTS user_notification_types (
                                                       preference_id BIGINT NOT NULL REFERENCES notification_preferences(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (preference_id, notification_type)
    );

-- Create user notification channels junction table
CREATE TABLE IF NOT EXISTS user_notification_channels (
                                                          preference_id BIGINT NOT NULL REFERENCES notification_preferences(id) ON DELETE CASCADE,
    notification_channel VARCHAR(20) NOT NULL,
    PRIMARY KEY (preference_id, notification_channel)
    );

-- Create SMS logs table
CREATE TABLE IF NOT EXISTS sms_logs (
                                        id BIGSERIAL PRIMARY KEY,
                                        phone_number VARCHAR(20) NOT NULL,
    message TEXT,
    provider VARCHAR(20),
    status VARCHAR(20),
    error_message TEXT,
    provider_response TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cost DECIMAL(10,4),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Enhanced document management
-- Create minimal documents table if missing to support alterations and indexes below
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    category VARCHAR(50),
    checksum VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE IF EXISTS documents
    ADD COLUMN IF NOT EXISTS access_count INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_accessed_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS checksum VARCHAR(64),
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by_id BIGINT REFERENCES users(id);

-- Create document tags table
CREATE TABLE IF NOT EXISTS document_tags (
                                             id BIGSERIAL PRIMARY KEY,
                                             name VARCHAR(100) NOT NULL UNIQUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create document-tags junction table
CREATE TABLE IF NOT EXISTS document_tag_assignments (
                                                        document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES document_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (document_id, tag_id)
    );

-- Ensure document_metadata base table exists
CREATE TABLE IF NOT EXISTS document_metadata (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL UNIQUE REFERENCES documents(id) ON DELETE CASCADE,
    extracted_text TEXT,
    indexed BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Update document_metadata table
ALTER TABLE IF EXISTS document_metadata
    ADD COLUMN IF NOT EXISTS extracted_text TEXT,
    ADD COLUMN IF NOT EXISTS indexed BOOLEAN DEFAULT FALSE;

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, read) WHERE read = FALSE;
CREATE INDEX IF NOT EXISTS idx_notifications_type_priority ON notifications(type, priority);
CREATE INDEX IF NOT EXISTS idx_notifications_scheduled ON notifications(scheduled_time) WHERE scheduled_time IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_sms_logs_phone_status ON sms_logs(phone_number, status);
CREATE INDEX IF NOT EXISTS idx_sms_logs_sent_at ON sms_logs(sent_at);

CREATE INDEX IF NOT EXISTS idx_documents_project_active ON documents(project_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_documents_category_active ON documents(category) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_documents_checksum ON documents(checksum);

CREATE INDEX IF NOT EXISTS idx_document_metadata_indexed ON document_metadata(indexed);

INSERT INTO notification_templates (type, name, title, email_subject, email_body, sms_template, in_app_template, active, language)
VALUES
    ('QUOTATION_SUBMITTED', 'Quotation Submitted', 'New Quotation Submitted',
     'New Quotation Submitted - $${projectName}',
     'A new quotation has been submitted for project $${projectName} by $${submittedBy}. Please review and approve.',
     'New quotation submitted for $${projectName}. Please review.',
     'New quotation submitted for $${projectName} by $${submittedBy}',
     TRUE, 'en'),

    ('BUDGET_EXCEEDED', 'Budget Exceeded Alert', 'Budget Exceeded',
     'URGENT: Budget Exceeded - $${projectName}',
     'Project $${projectName} has exceeded its allocated budget. Current utilization: $${utilizationPercentage}%',
     'URGENT: $${projectName} over budget ($${utilizationPercentage}%)',
     'Project $${projectName} has exceeded budget - $${utilizationPercentage}%',
     TRUE, 'en'),

    ('PAYMENT_COMPLETED', 'Payment Completed', 'Payment Processed',
     'Payment Completed - $${projectName}',
     'Payment of $${amount} for project $${projectName} has been completed on $${paymentDate}.',
     'Payment of $${amount} completed for $${projectName}',
     'Payment completed: $${amount} for $${projectName}',
     TRUE, 'en')
    ON CONFLICT (type) DO NOTHING;
-- Add comments for documentation
COMMENT ON TABLE notification_preferences IS 'User notification preferences and settings';
COMMENT ON TABLE sms_logs IS 'SMS delivery logs and tracking';
COMMENT ON TABLE document_tags IS 'Tags for document categorization and search';
COMMENT ON COLUMN documents.checksum IS 'SHA-256 checksum for file integrity verification';
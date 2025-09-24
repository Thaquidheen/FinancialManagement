-- V4__Create_quotation_tables.sql
-- Create quotation system tables

-- Quotations table
CREATE TABLE IF NOT EXISTS quotations (
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
CREATE TABLE IF NOT EXISTS quotation_items (
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

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_quotations_project_id ON quotations(project_id);
CREATE INDEX IF NOT EXISTS idx_quotations_created_by ON quotations(created_by);
CREATE INDEX IF NOT EXISTS idx_quotations_status ON quotations(status);
CREATE INDEX IF NOT EXISTS idx_quotations_created_date ON quotations(created_date);
CREATE INDEX IF NOT EXISTS idx_quotation_items_quotation_id ON quotation_items(quotation_id);
CREATE INDEX IF NOT EXISTS idx_quotation_items_item_order ON quotation_items(item_order);
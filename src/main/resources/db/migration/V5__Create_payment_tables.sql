-- V5__Create_payment_tables.sql
-- Create payment processing system tables

-- Payment batches table
CREATE TABLE payment_batches (
                                 id BIGSERIAL PRIMARY KEY,
                                 batch_number VARCHAR(50) NOT NULL UNIQUE,
                                 bank_name VARCHAR(100),
                                 total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                                 currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
                                 payment_count INTEGER NOT NULL DEFAULT 0,
                                 status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
                                 created_by BIGINT NOT NULL,
                                 file_name VARCHAR(200),
                                 file_path VARCHAR(500),
                                 generated_date TIMESTAMP,
                                 downloaded_date TIMESTAMP,
                                 sent_to_bank_date TIMESTAMP,
                                 bank_reference VARCHAR(100),
                                 processing_notes TEXT,
                                 active BOOLEAN NOT NULL DEFAULT true,
                                 created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 created_by_name VARCHAR(50) DEFAULT 'system',
                                 last_modified_by VARCHAR(50) DEFAULT 'system',

                                 FOREIGN KEY (created_by) REFERENCES users(id),
                                 CHECK (status IN ('DRAFT', 'FILE_GENERATED', 'SENT_TO_BANK', 'COMPLETED', 'CANCELLED')),
                                 CHECK (total_amount >= 0),
                                 CHECK (payment_count >= 0)
);

-- Payments table
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          quotation_id BIGINT NOT NULL,
                          payee_id BIGINT NOT NULL,
                          batch_id BIGINT,
                          amount DECIMAL(15,2) NOT NULL,
                          currency VARCHAR(3) NOT NULL DEFAULT 'SAR',
                          status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                          bank_name VARCHAR(100),
                          account_number VARCHAR(50),
                          iban VARCHAR(34),
                          beneficiary_address TEXT,
                          bank_reference VARCHAR(100),
                          payment_date TIMESTAMP,
                          failure_reason TEXT,
                          retry_count INTEGER NOT NULL DEFAULT 0,
                          comments TEXT,
                          active BOOLEAN NOT NULL DEFAULT true,
                          created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          created_by_name VARCHAR(50) DEFAULT 'system',
                          last_modified_by VARCHAR(50) DEFAULT 'system',

                          FOREIGN KEY (quotation_id) REFERENCES quotations(id),
                          FOREIGN KEY (payee_id) REFERENCES users(id),
                          FOREIGN KEY (batch_id) REFERENCES payment_batches(id),

                          CHECK (status IN ('PENDING', 'READY_FOR_PAYMENT', 'FILE_GENERATED', 'PROCESSING', 'SENT_TO_BANK', 'PAID', 'FAILED', 'CANCELLED', 'ON_HOLD')),
                          CHECK (amount > 0),
                          CHECK (retry_count >= 0)
);

-- Bank files table (for audit trail)
CREATE TABLE bank_files (
                            id BIGSERIAL PRIMARY KEY,
                            batch_id BIGINT NOT NULL,
                            file_name VARCHAR(200) NOT NULL,
                            file_path VARCHAR(500),
                            file_size BIGINT,
                            content_type VARCHAR(100),
                            bank_name VARCHAR(100),
                            generated_by BIGINT NOT NULL,
                            download_count INTEGER NOT NULL DEFAULT 0,
                            last_downloaded_date TIMESTAMP,
                            active BOOLEAN NOT NULL DEFAULT true,
                            created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            created_by VARCHAR(50) DEFAULT 'system',
                            last_modified_by VARCHAR(50) DEFAULT 'system',

                            FOREIGN KEY (batch_id) REFERENCES payment_batches(id) ON DELETE CASCADE,
                            FOREIGN KEY (generated_by) REFERENCES users(id),
                            CHECK (file_size >= 0),
                            CHECK (download_count >= 0)
);

-- Create indexes for better performance
CREATE INDEX idx_payment_batches_status ON payment_batches(status);
CREATE INDEX idx_payment_batches_bank_name ON payment_batches(bank_name);
CREATE INDEX idx_payment_batches_created_by ON payment_batches(created_by);
CREATE INDEX idx_payment_batches_created_date ON payment_batches(created_date);

CREATE INDEX idx_payments_quotation_id ON payments(quotation_id);
CREATE INDEX idx_payments_payee_id ON payments(payee_id);
CREATE INDEX idx_payments_batch_id ON payments(batch_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_bank_name ON payments(bank_name);
CREATE INDEX idx_payments_payment_date ON payments(payment_date);
CREATE INDEX idx_payments_created_date ON payments(created_date);

CREATE INDEX idx_bank_files_batch_id ON bank_files(batch_id);
CREATE INDEX idx_bank_files_generated_by ON bank_files(generated_by);
CREATE INDEX idx_bank_files_bank_name ON bank_files(bank_name);
CREATE INDEX idx_bank_files_created_date ON bank_files(created_date);
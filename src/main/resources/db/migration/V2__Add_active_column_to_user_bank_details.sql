-- V2__Add_active_column_to_user_bank_details.sql
-- Add missing active column to user_bank_details table

ALTER TABLE user_bank_details
ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;

-- V3__Create_project_assignments_table.sql
-- Add project_assignments table for project-user role assignments

CREATE TABLE project_assignments (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    active BOOLEAN NOT NULL DEFAULT true,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) DEFAULT 'system',
    last_modified_by VARCHAR(50) DEFAULT 'system',

    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT unique_project_user_role UNIQUE (project_id, user_id, role),
    CHECK (role IN ('MANAGER', 'MEMBER', 'STAKEHOLDER'))
);

-- Create indexes for better performance
CREATE INDEX idx_project_assignments_project_id ON project_assignments(project_id);
CREATE INDEX idx_project_assignments_user_id ON project_assignments(user_id);
CREATE INDEX idx_project_assignments_role ON project_assignments(role);
CREATE INDEX idx_project_assignments_active ON project_assignments(active);
CREATE INDEX idx_project_assignments_is_active ON project_assignments(is_active);

package com.company.erp.document.service;

import com.company.erp.document.entity.Document;
import com.company.erp.user.entity.User;
import org.springframework.stereotype.Service;

/**
 * Service for document security and access control
 */
@Service
public class DocumentSecurityService {

    /**
     * Check if user can access a document
     */
    public boolean canUserAccessDocument(User user, Document document) {
        // Super admin and account managers can access all documents
        if (user.hasRole("SUPER_ADMIN") || user.hasRole("ACCOUNT_MANAGER")) {
            return true;
        }
        
        // Project managers can access documents from their projects or documents they uploaded
        if (user.hasRole("PROJECT_MANAGER")) {
            return document.getUploadedBy().getId().equals(user.getId()) ||
                   isUserAssignedToProject(user.getId(), document.getProjectId());
        }
        
        // Regular users can only access their own documents
        return document.getUploadedBy().getId().equals(user.getId());
    }

    /**
     * Check if user can delete a document
     */
    public boolean canUserDeleteDocument(User user, Document document) {
        // Super admin can delete all documents
        if (user.hasRole("SUPER_ADMIN")) {
            return true;
        }
        
        // Account managers and project managers can delete their own documents
        if (user.hasRole("ACCOUNT_MANAGER") || user.hasRole("PROJECT_MANAGER")) {
            return document.getUploadedBy().getId().equals(user.getId());
        }
        
        // Regular users can only delete their own documents
        return document.getUploadedBy().getId().equals(user.getId());
    }

    /**
     * Check if user can edit a document
     */
    public boolean canUserEditDocument(User user, Document document) {
        return canUserDeleteDocument(user, document);
    }

    /**
     * Check if user is assigned to a project
     * This is a placeholder implementation
     */
    private boolean isUserAssignedToProject(Long userId, Long projectId) {
        // In a real implementation, this would check the project assignments
        // For now, return true as a placeholder
        return true;
    }
}

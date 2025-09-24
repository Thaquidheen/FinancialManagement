// =============================================================================
// EmailTemplateService.java - Template Management Service
// =============================================================================

package com.company.erp.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class EmailTemplateService {

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.notification.email.template-path:classpath:/templates/email/}")
    private String templateBasePath;

    /**
     * Process template with variables
     */
    public String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();

        // Add all provided variables
        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        // Add common template variables
        addCommonVariables(context);

        return templateEngine.process("email/" + templateName, context);
    }

    /**
     * Add common variables to all email templates
     */
    private void addCommonVariables(Context context) {
        context.setVariable("companyName", "Your Company Name");
        context.setVariable("companyLogo", "https://erp.company.com/images/logo.png");
        context.setVariable("supportEmail", "support@company.com");
        context.setVariable("systemUrl", "https://erp.company.com");
        context.setVariable("unsubscribeUrl", "https://erp.company.com/unsubscribe");
        context.setVariable("currentYear", java.time.Year.now().getValue());
    }
}


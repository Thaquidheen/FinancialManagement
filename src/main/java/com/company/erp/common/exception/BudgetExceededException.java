
package com.company.erp.common.exception;


// Budget Exceeded Exception
public class BudgetExceededException extends BusinessException {
    public BudgetExceededException(String projectName, double requestedAmount, double availableBudget) {
        super("BUDGET_EXCEEDED",
                String.format("Budget exceeded for project '%s'. Requested: SAR %.2f, Available: SAR %.2f",
                        projectName, requestedAmount, availableBudget));
    }
}
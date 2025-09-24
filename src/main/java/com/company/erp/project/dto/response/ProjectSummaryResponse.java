import java.math.BigDecimal;

public class ProjectSummaryResponse {

    private Long id;
    private String name;
    private String location;
    private String status;
    private BigDecimal allocatedBudget;
    private BigDecimal spentAmount;
    private String managerName;
    private Boolean overBudget;

    public ProjectSummaryResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public void setAllocatedBudget(BigDecimal allocatedBudget) {
        this.allocatedBudget = allocatedBudget;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Boolean getOverBudget() {
        return overBudget;
    }

    public void setOverBudget(Boolean overBudget) {
        this.overBudget = overBudget;
    }
}
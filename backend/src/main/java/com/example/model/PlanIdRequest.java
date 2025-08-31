package com.example.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for planId-based operations
 * 
 * Supports both single planId and batch planIds operations
 */
public class PlanIdRequest {
    
    @JsonProperty("planId")
    private Long planId;
    
    @JsonProperty("planIds")
    private List<Long> planIds;
    
    // Constructors
    public PlanIdRequest() {}
    
    public PlanIdRequest(Long planId) {
        this.planId = planId;
    }
    
    public PlanIdRequest(List<Long> planIds) {
        this.planIds = planIds;
    }
    
    // Getters and setters
    public Long getPlanId() {
        return planId;
    }
    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    public List<Long> getPlanIds() {
        return planIds;
    }
    
    public void setPlanIds(List<Long> planIds) {
        this.planIds = planIds;
    }
    
    /**
     * Check if this is a single planId request
     */
    public boolean isSinglePlanId() {
        return planId != null;
    }
    
    /**
     * Check if this is a batch planIds request
     */
    public boolean isBatchPlanIds() {
        return planIds != null && !planIds.isEmpty();
    }
    
    /**
     * Validate the request
     */
    public boolean isValid() {
        return isSinglePlanId() || isBatchPlanIds();
    }
    
    /**
     * Get the total number of planIds in this request
     */
    public int getTotalPlanIds() {
        if (isSinglePlanId()) {
            return 1;
        } else if (isBatchPlanIds()) {
            return planIds.size();
        } else {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        if (isSinglePlanId()) {
            return "PlanIdRequest{planId=" + planId + "}";
        } else if (isBatchPlanIds()) {
            return "PlanIdRequest{planIds=" + planIds.size() + " items}";
        } else {
            return "PlanIdRequest{empty}";
        }
    }
}

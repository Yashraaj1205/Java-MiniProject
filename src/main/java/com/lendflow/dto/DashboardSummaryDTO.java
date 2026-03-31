package com.lendflow.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardSummaryDTO {
    private BigDecimal totalDisbursed;
    private long activeBorrowers;
    private String collectionRate;
    private long overdueLoans;
    
    // Analytics Collections
    private List<Long> statusCounts;
    private List<String> chartMonths;
    private List<BigDecimal> monthlyDisbursements;
}

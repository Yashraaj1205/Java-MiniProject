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
    
    private BigDecimal totalInterestEarned;
    private BigDecimal totalLateFees;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    
    
    private List<Long> statusCounts;
    private List<String> chartMonths;
    private List<BigDecimal> monthlyDisbursements;
    private List<BigDecimal> monthlyCollections;
    private List<BigDecimal> monthlyExpenses;
}


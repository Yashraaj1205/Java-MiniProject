package com.lendflow.service;

import com.lendflow.dto.DashboardSummaryDTO;
import com.lendflow.entity.Loan;
import java.util.List;

public interface IDashboardService {
    DashboardSummaryDTO getSummary();
    List<Loan> getRecentLoans();
}


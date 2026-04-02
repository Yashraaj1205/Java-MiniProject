package com.lendflow.service;

import com.lendflow.dto.DashboardSummaryDTO;
import com.lendflow.entity.Loan;
import com.lendflow.entity.Payment;
import com.lendflow.repository.BorrowerRepository;
import com.lendflow.repository.ExpenseRepository;
import com.lendflow.repository.LoanRepository;
import com.lendflow.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public DashboardSummaryDTO getSummary() {
        List<Loan> allLoans = loanRepository.findAll();
        List<Payment> allPayments = paymentRepository.findAll();
        
        BigDecimal totalDisbursed = allLoans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalInterestEarned = allPayments.stream()
                .map(p -> p.getInterestComponent() == null ? BigDecimal.ZERO : p.getInterestComponent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLateFees = allPayments.stream()
                .map(p -> p.getLateFee() == null ? BigDecimal.ZERO : p.getLateFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = allPayments.stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeBorrowers = borrowerRepository.count();
        
        long overdueLoans = loanRepository.countByStatus("Overdue");

        String collectionRate = totalDisbursed.compareTo(BigDecimal.ZERO) == 0 ? "0.0%" :
                totalCollected.multiply(BigDecimal.valueOf(100)).divide(totalDisbursed, 1, RoundingMode.HALF_UP) + "%";

        long completedCount = loanRepository.countByStatus("Completed");
        long activeCount = loanRepository.countByStatus("Active");
                
        List<Long> statusCounts = Arrays.asList(activeCount, overdueLoans, completedCount);

        List<String> chartMonths = new ArrayList<>();
        List<BigDecimal> monthlyDisbursements = new ArrayList<>();
        List<BigDecimal> monthlyCollections = new ArrayList<>();
        List<BigDecimal> monthlyExpensesList = new ArrayList<>();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

        BigDecimal totalExpensesAcc = BigDecimal.ZERO;

        for(int i = 5; i >= 0; i--) {
            LocalDate monthTarget = now.minusMonths(i);
            LocalDate start = monthTarget.withDayOfMonth(1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            
            chartMonths.add(monthTarget.format(formatter));
            
            BigDecimal disMonth = loanRepository.findLoansIssuedBetween(start, end).stream()
                                    .map(Loan::getPrincipalAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyDisbursements.add(disMonth);

            BigDecimal colMonth = paymentRepository.findByPaymentDateBetween(start, end).stream()
                                    .map(Payment::getAmountPaid)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyCollections.add(colMonth);

            BigDecimal expMonth = expenseRepository.sumAmountBetween(start, end);
            if (expMonth == null) expMonth = BigDecimal.ZERO;
            monthlyExpensesList.add(expMonth);

            totalExpensesAcc = totalExpensesAcc.add(expMonth);
        }

        BigDecimal netProfit = totalInterestEarned.add(totalLateFees).subtract(totalExpensesAcc);

        return DashboardSummaryDTO.builder()
                .totalDisbursed(totalDisbursed)
                .activeBorrowers(activeBorrowers)
                .collectionRate(collectionRate)
                .overdueLoans(overdueLoans)
                .totalInterestEarned(totalInterestEarned)
                .totalLateFees(totalLateFees)
                .totalExpenses(totalExpensesAcc)
                .netProfit(netProfit)
                .statusCounts(statusCounts)
                .chartMonths(chartMonths)
                .monthlyDisbursements(monthlyDisbursements)
                .monthlyCollections(monthlyCollections)
                .monthlyExpenses(monthlyExpensesList)
                .build();
    }

    @Override
    public List<Loan> getRecentLoans() {
        return loanRepository.findTop5ByOrderByIssueDateDesc();
    }
}


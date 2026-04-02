package com.lendflow.service;

import com.lendflow.entity.Borrower;
import com.lendflow.entity.Loan;
import com.lendflow.entity.Payment;
import com.lendflow.entity.EMISchedule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ILoanService {
    Loan createLoan(Loan newLoan, Borrower borrower);
    Payment recordPayment(Long loanId, BigDecimal amountPaid, String paymentMethod, String notes, BigDecimal lateFeePaid);
    void markOverdueLoans();
    List<Loan> getAllLoans();
    Loan getLoanById(Long id);
    List<EMISchedule> getApproachingPayments(LocalDate date);
    List<EMISchedule> getOverduePayments();
}


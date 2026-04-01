package com.lendflow.service;

import com.lendflow.entity.*;
import com.lendflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final EMIScheduleRepository emiScheduleRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Transactional
    public Loan createLoan(Loan newLoan, Borrower borrower) {
        newLoan.setBorrower(borrower);
        newLoan.setLoanReference("LN-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        newLoan.setStatus("Active");

        if (newLoan.getIssueDate() == null) {
            newLoan.setIssueDate(LocalDate.now());
        }
        
        // Ensure defaults if missing
        if (newLoan.getInterestRate() == null) newLoan.setInterestRate(0.0);
        
        if (newLoan.getEndDate() == null) {
            newLoan.setEndDate(newLoan.getIssueDate().plusMonths(1)); // Fallback
        }
        
        // Calculate Total Payable based on exact days (logic in Loan entity)
        BigDecimal totalPayable = newLoan.getTotalPayable();
        newLoan.setEmiAmount(totalPayable); // Since it's lump sum, EMI amount is total payable
        
        // Set due date directly to end date
        newLoan.setNextDueDate(newLoan.getEndDate());
        newLoan.setRepaymentType("Lump Sum");

        Loan savedLoan = loanRepository.save(newLoan);
        generateLumpSumSchedule(savedLoan, totalPayable);
        
        try {
            notificationService.sendLoanIssuedNotification(savedLoan);
        } catch (Exception e) {
            // Log silently so loan creation doesn't fail if notification fails
            e.printStackTrace();
        }
        
        return savedLoan;
    }

    private void generateLumpSumSchedule(Loan loan, BigDecimal totalPayable) {
        BigDecimal principalTotal = loan.getPrincipalAmount();
        BigDecimal interestTotal = totalPayable.subtract(principalTotal);

        EMISchedule schedule = EMISchedule.builder()
                .loan(loan)
                .installmentNumber(1)
                .dueDate(loan.getEndDate())
                .emiAmount(totalPayable)
                .principalComponent(principalTotal)
                .interestComponent(interestTotal)
                .status("Pending")
                .build();
        
        emiScheduleRepository.save(schedule);
    }

    @Transactional
    public Payment recordPayment(Long loanId, BigDecimal amountPaid, String paymentMethod, String notes, BigDecimal lateFeePaid) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new IllegalArgumentException("Invalid Loan ID"));
        
        if (lateFeePaid == null) lateFeePaid = BigDecimal.ZERO;

        // Determine which schedules to pay off based on amount
        List<EMISchedule> pendingSchedules = emiScheduleRepository.findByLoanIdAndStatus(loanId, "Pending");
        pendingSchedules.addAll(emiScheduleRepository.findByLoanIdAndStatus(loanId, "Overdue"));
        pendingSchedules.addAll(emiScheduleRepository.findByLoanIdAndStatus(loanId, "Partial"));
        
        // Sort by installment number
        pendingSchedules.sort((s1, s2) -> s1.getInstallmentNumber().compareTo(s2.getInstallmentNumber()));

        BigDecimal remainingAmount = amountPaid;
        BigDecimal appliedPrincipal = BigDecimal.ZERO;
        BigDecimal appliedInterest = BigDecimal.ZERO;

        for (EMISchedule schedule : pendingSchedules) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) break;
            
            BigDecimal amountNeededForEmi = schedule.getEmiAmount().subtract(schedule.getPaidAmount());
            
            if (remainingAmount.compareTo(amountNeededForEmi) >= 0) {
                // Fully pay this EMI
                schedule.setPaidAmount(schedule.getEmiAmount());
                schedule.setStatus("Paid");
                schedule.setPaidDate(LocalDate.now());
                
                appliedPrincipal = appliedPrincipal.add(schedule.getPrincipalComponent());
                appliedInterest = appliedInterest.add(schedule.getInterestComponent());
                
                remainingAmount = remainingAmount.subtract(amountNeededForEmi);
            } else {
                // Partially pay this EMI
                schedule.setPaidAmount(schedule.getPaidAmount().add(remainingAmount));
                schedule.setStatus("Partial");
                schedule.setPaidDate(LocalDate.now());
                
                // Simplified split for partial payment: pay interest first, then principal
                BigDecimal tempInterestNeeded = schedule.getInterestComponent();
                BigDecimal interestToApply = remainingAmount.min(tempInterestNeeded);
                appliedInterest = appliedInterest.add(interestToApply);
                
                BigDecimal tempPrincipalToApply = remainingAmount.subtract(interestToApply);
                appliedPrincipal = appliedPrincipal.add(tempPrincipalToApply);
                
                remainingAmount = BigDecimal.ZERO;
            }
        }
        
        emiScheduleRepository.saveAll(pendingSchedules);

        Payment payment = Payment.builder()
                .loan(loan)
                .amountPaid(amountPaid)
                .principalComponent(appliedPrincipal)
                .interestComponent(appliedInterest)
                .lateFee(lateFeePaid)
                .paymentDate(LocalDate.now())
                .paymentMethod(paymentMethod)
                .notes(notes)
                .build();
                
        paymentRepository.save(payment);

        updateLoanStatus(loan);
        return payment;
    }

    private void updateLoanStatus(Loan loan) {
        // Calculate new next due date
        List<EMISchedule> pending = emiScheduleRepository.findByLoanIdAndStatus(loan.getId(), "Pending");
        pending.addAll(emiScheduleRepository.findByLoanIdAndStatus(loan.getId(), "Partial"));
        pending.addAll(emiScheduleRepository.findByLoanIdAndStatus(loan.getId(), "Overdue"));
        
        if (pending.isEmpty()) {
            loan.setStatus("Completed");
            loan.setNextDueDate(LocalDate.now()); // Or keep last date
        } else {
            pending.sort((s1, s2) -> s1.getInstallmentNumber().compareTo(s2.getInstallmentNumber()));
            LocalDate earliestDue = pending.get(0).getDueDate();
            loan.setNextDueDate(earliestDue);
            
            if (earliestDue.isBefore(LocalDate.now())) {
                loan.setStatus("Overdue");
                updateAccruedLateFees(loan, pending);
            } else {
                loan.setStatus("Active");
            }
        }
        loanRepository.save(loan);
    }
    
    private void updateAccruedLateFees(Loan loan, List<EMISchedule> pendingAndOverdue) {
        if (loan.getLateFeePerDay() == null || loan.getLateFeePerDay().compareTo(BigDecimal.ZERO) == 0) return;
        
        LocalDate today = LocalDate.now();
        
        for (EMISchedule schedule : pendingAndOverdue) {
             if (schedule.getDueDate().isBefore(today)) {
                 long daysLate = java.time.temporal.ChronoUnit.DAYS.between(schedule.getDueDate(), today);
                 BigDecimal accruedLateFee = loan.getLateFeePerDay().multiply(BigDecimal.valueOf(daysLate));
                 if (loan.getLateFeeFlat() != null) {
                     accruedLateFee = accruedLateFee.add(loan.getLateFeeFlat());
                 }
                 schedule.setLateFee(accruedLateFee);
                 schedule.setStatus("Overdue");
             }
        }
        emiScheduleRepository.saveAll(pendingAndOverdue);
    }
    
    @Transactional
    public void markOverdueLoans() {
        // Find loans that should be overdue
        List<Loan> loans = loanRepository.findOverdueLoans(LocalDate.now());
        for (Loan l : loans) {
            l.setStatus("Overdue");
            List<EMISchedule> pending = emiScheduleRepository.findByLoanIdAndStatus(l.getId(), "Pending");
            pending.addAll(emiScheduleRepository.findByLoanIdAndStatus(l.getId(), "Partial"));
            updateAccruedLateFees(l, pending);
            loanRepository.save(l);
        }
    }
}

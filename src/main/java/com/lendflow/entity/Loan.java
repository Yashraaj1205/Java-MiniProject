package com.lendflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String loanReference; // e.g., LN-004
    
    @Column(nullable = false)
    private BigDecimal principalAmount;
    
    @Column(nullable = false)
    private BigDecimal emiAmount; // Monthly payment amount

    @Builder.Default
    private Double interestRate = 0.0; // Annual interest rate %

    @Builder.Default
    private Integer tenureMonths = 12; // Repayment tenure in months

    @Column(nullable = false)
    @Builder.Default
    private String repaymentType = "EMI"; // EMI, Lump Sum, Weekly, Custom

    @Builder.Default
    private BigDecimal lateFeePerDay = BigDecimal.ZERO; // Penalty per day late

    @Builder.Default
    private BigDecimal lateFeeFlat = BigDecimal.ZERO; // One-time flat late fee per missed EMI

    private String purpose; // What the loan is for (Stock Purchase, Equipment, etc.)

    @Column(length = 500)
    private String notes; // Free-text notes
    
    @Column(nullable = false)
    private LocalDate issueDate;

    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private LocalDate endDate; // User selected maturity date
    
    @Column(nullable = false)
    private LocalDate nextDueDate; // Next due date
    
    @Column(nullable = false)
    private String status; // Active, Overdue, Completed, Settled
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("paymentDate ASC")
    private List<Payment> payments;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dueDate ASC")
    private List<EMISchedule> emiSchedules;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("actionDate DESC")
    private List<RecoveryAction> recoveryActions;

    /**
     * Calculate total payable using exact days:
     * Total = Principal + (Principal × Rate/100 × Days/365)
     */
    public BigDecimal getTotalPayable() {
        BigDecimal total = principalAmount;
        if (interestRate != null && interestRate > 0 && issueDate != null && endDate != null) {
            long actualDays = java.time.temporal.ChronoUnit.DAYS.between(issueDate, endDate);
            if (actualDays <= 0) actualDays = 1; // Minimum 1 day of interest just in case it's same day

            BigDecimal interest = principalAmount
                    .multiply(BigDecimal.valueOf(interestRate))
                    .multiply(BigDecimal.valueOf(actualDays))
                    .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
            total = total.add(interest);
        }
        return total;
    }

    /**
     * Calculate total interest amount
     */
    public BigDecimal getTotalInterest() {
        return getTotalPayable().subtract(principalAmount);
    }

    /**
     * Outstanding balance = Total Payable - Total Paid
     */
    public BigDecimal getOutstandingBalance() {
        BigDecimal paid = getTotalPaid();
        return getTotalPayable().subtract(paid);
    }

    /**
     * Total amount paid across all payments
     */
    public BigDecimal getTotalPaid() {
        if (payments == null || payments.isEmpty()) return BigDecimal.ZERO;
        return payments.stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Total late fees accrued across all payments
     */
    public BigDecimal getTotalLateFees() {
        if (payments == null || payments.isEmpty()) return BigDecimal.ZERO;
        return payments.stream()
                .map(p -> p.getLateFee() == null ? BigDecimal.ZERO : p.getLateFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get completion percentage
     */
    public int getCompletionPercent() {
        BigDecimal total = getTotalPayable();
        if (total.compareTo(BigDecimal.ZERO) == 0) return 100;
        BigDecimal paid = getTotalPaid();
        return paid.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * Get number of paid installments
     */
    public long getPaidInstallments() {
        if (emiSchedules == null) return 0;
        return emiSchedules.stream()
                .filter(e -> "Paid".equalsIgnoreCase(e.getStatus()))
                .count();
    }

    /**
     * Get number of overdue installments
     */
    public long getOverdueInstallments() {
        if (emiSchedules == null) return 0;
        return emiSchedules.stream()
                .filter(e -> "Overdue".equalsIgnoreCase(e.getStatus()))
                .count();
    }
}

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
@EqualsAndHashCode(callSuper = true)
public class Loan extends BaseEntity {
    
    @Column(unique = true, nullable = false)
    private String loanReference;

    
    @Column(nullable = false)
    private BigDecimal principalAmount;
    
    @Column(nullable = false)
    private BigDecimal emiAmount; 

    @Builder.Default
    private Double interestRate = 0.0; 

    @Builder.Default
    private Integer tenureMonths = 12; 

    @Column(nullable = false)
    @Builder.Default
    private String repaymentType = "EMI"; 

    @Builder.Default
    private BigDecimal lateFeePerDay = BigDecimal.ZERO; 

    @Builder.Default
    private BigDecimal lateFeeFlat = BigDecimal.ZERO; 

    private String purpose; 

    @Column(length = 500)
    private String notes; 
    
    @Column(nullable = false)
    private LocalDate issueDate;

    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private LocalDate endDate; 
    
    @Column(nullable = false)
    private LocalDate nextDueDate; 
    
    @Column(nullable = false)
    private String status; 
    
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

    
    public BigDecimal getTotalPayable() {
        BigDecimal total = principalAmount;
        if (interestRate != null && interestRate > 0 && issueDate != null && endDate != null) {
            long actualDays = java.time.temporal.ChronoUnit.DAYS.between(issueDate, endDate);
            if (actualDays <= 0) actualDays = 1; 

            BigDecimal interest = principalAmount
                    .multiply(BigDecimal.valueOf(interestRate))
                    .multiply(BigDecimal.valueOf(actualDays))
                    .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
            total = total.add(interest);
        }
        return total;
    }

    
    public BigDecimal getTotalInterest() {
        return getTotalPayable().subtract(principalAmount);
    }

    
    public BigDecimal getOutstandingBalance() {
        BigDecimal paid = getTotalPaid();
        return getTotalPayable().subtract(paid);
    }

    
    public BigDecimal getTotalPaid() {
        if (payments == null || payments.isEmpty()) return BigDecimal.ZERO;
        return payments.stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    
    public BigDecimal getTotalLateFees() {
        if (payments == null || payments.isEmpty()) return BigDecimal.ZERO;
        return payments.stream()
                .map(p -> p.getLateFee() == null ? BigDecimal.ZERO : p.getLateFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    
    public int getCompletionPercent() {
        BigDecimal total = getTotalPayable();
        if (total.compareTo(BigDecimal.ZERO) == 0) return 100;
        BigDecimal paid = getTotalPaid();
        return paid.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    
    public long getPaidInstallments() {
        if (emiSchedules == null) return 0;
        return emiSchedules.stream()
                .filter(e -> "Paid".equalsIgnoreCase(e.getStatus()))
                .count();
    }

    
    public long getOverdueInstallments() {
        if (emiSchedules == null) return 0;
        return emiSchedules.stream()
                .filter(e -> "Overdue".equalsIgnoreCase(e.getStatus()))
                .count();
    }
}


package com.lendflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "borrowers", uniqueConstraints = {@UniqueConstraint(name = "uk_borrowers_phone", columnNames = "phone")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Borrower extends BaseEntity {
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be exactly 10 digits")
    private String phone;
    
    private String address;
    private String email;
    private String businessName;

    @Column(nullable = false)
    @Builder.Default
    private String businessType = "Retail"; 

    private String identityProof; 

    @Column(nullable = false)
    @Builder.Default
    private LocalDate dateAdded = LocalDate.now();
    
    @Column(nullable = false)
    @Builder.Default
    private String riskProfile = "Low"; 
    
    @OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Loan> loans;

    public BigDecimal getTotalPendingAmount() {
        if (loans == null || loans.isEmpty()) return BigDecimal.ZERO;
        return loans.stream()
                .filter(l -> !"Completed".equalsIgnoreCase(l.getStatus()))
                .map(Loan::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalBorrowed() {
        if (loans == null || loans.isEmpty()) return BigDecimal.ZERO;
        return loans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPaid() {
        if (loans == null || loans.isEmpty()) return BigDecimal.ZERO;
        return loans.stream()
                .flatMap(l -> l.getPayments() == null ? java.util.stream.Stream.empty() : l.getPayments().stream())
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getActiveLoansCount() {
        if (loans == null) return 0;
        return loans.stream()
                .filter(l -> !"Completed".equalsIgnoreCase(l.getStatus()))
                .count();
    }

    public long getCompletedLoansCount() {
        if (loans == null) return 0;
        return loans.stream()
                .filter(l -> "Completed".equalsIgnoreCase(l.getStatus()))
                .count();
    }
}

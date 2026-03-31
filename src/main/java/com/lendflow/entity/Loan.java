package com.lendflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;
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
    
    private Double interestRate;
    
    @Column(nullable = false)
    private LocalDate issueDate;
    
    @Column(nullable = false)
    private LocalDate nextDueDate; // Next due date
    
    @Column(nullable = false)
    private String status; // Active, Overdue, Completed
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Payment> payments;

    public BigDecimal getOutstandingBalance() {
        BigDecimal paid = (payments == null || payments.isEmpty()) ? BigDecimal.ZERO : 
                            payments.stream().map(Payment::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = principalAmount;
        if (interestRate != null && interestRate > 0) {
            BigDecimal interest = total.multiply(BigDecimal.valueOf(interestRate)).divide(BigDecimal.valueOf(100));
            total = total.add(interest);
        }
        return total.subtract(paid);
    }
}

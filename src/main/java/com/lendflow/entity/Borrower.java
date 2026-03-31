package com.lendflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.math.BigDecimal;
@Entity
@Table(name = "borrowers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String phone;
    
    private String address;
    
    @Column(nullable = false)
    @Builder.Default
    private String riskProfile = "Low"; // Low, Medium, High
    
    @OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Loan> loans;

    public BigDecimal getTotalPendingAmount() {
        if (loans == null || loans.isEmpty()) return BigDecimal.ZERO;
        return loans.stream()
                .filter(l -> !"Completed".equalsIgnoreCase(l.getStatus()))
                .map(Loan::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

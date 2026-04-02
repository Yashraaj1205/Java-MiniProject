package com.lendflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {


    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Builder.Default
    private BigDecimal principalComponent = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal interestComponent = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    @Builder.Default
    private String paymentMethod = "Cash"; 

    @Column(length = 500)
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
}


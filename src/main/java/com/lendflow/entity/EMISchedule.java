package com.lendflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "emi_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class EMISchedule extends BaseEntity {


    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private BigDecimal emiAmount;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal principalComponent = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal interestComponent = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private String status = "Pending"; 

    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    private LocalDate paidDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
}


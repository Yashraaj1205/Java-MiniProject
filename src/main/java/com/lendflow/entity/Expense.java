package com.lendflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Expense extends BaseEntity {

    @Column(nullable = false)
    private String category; 

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false)
    @Builder.Default
    private String paymentMethod = "Cash"; 
}

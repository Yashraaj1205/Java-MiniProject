package com.lendflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "recovery_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class RecoveryAction extends BaseEntity {


    @Column(nullable = false)
    private String actionType; 

    @Column(nullable = false)
    private LocalDate actionDate;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private String outcome = "Pending"; 

    private LocalDate followUpDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
}


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
public class RecoveryAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actionType; // Phone Call, Visit, WhatsApp, SMS, Legal Notice, Settlement Offer

    @Column(nullable = false)
    private LocalDate actionDate;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private String outcome = "Pending"; // Successful, Partial, No Response, Promise to Pay, Pending

    private LocalDate followUpDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
}

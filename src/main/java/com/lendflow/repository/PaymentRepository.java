package com.lendflow.repository;

import com.lendflow.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByLoanId(Long loanId);
    List<Payment> findByLoanIdOrderByPaymentDateDesc(Long loanId);
    List<Payment> findByPaymentDateBetween(LocalDate start, LocalDate end);
}


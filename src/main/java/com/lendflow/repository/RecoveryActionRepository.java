package com.lendflow.repository;

import com.lendflow.entity.RecoveryAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecoveryActionRepository extends JpaRepository<RecoveryAction, Long> {
    List<RecoveryAction> findByLoanIdOrderByActionDateDesc(Long loanId);
    List<RecoveryAction> findByFollowUpDateBeforeAndOutcomeNot(LocalDate date, String outcome);
    List<RecoveryAction> findByOutcome(String outcome);
}

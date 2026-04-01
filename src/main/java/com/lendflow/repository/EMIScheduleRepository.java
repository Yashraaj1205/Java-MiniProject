package com.lendflow.repository;

import com.lendflow.entity.EMISchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EMIScheduleRepository extends JpaRepository<EMISchedule, Long> {
    List<EMISchedule> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
    List<EMISchedule> findByLoanIdAndStatus(Long loanId, String status);
    List<EMISchedule> findByStatusAndDueDateBefore(String status, LocalDate date);
}

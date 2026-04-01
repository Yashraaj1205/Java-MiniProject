package com.lendflow.repository;

import com.lendflow.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatus(String status);
    List<Loan> findByStatusIn(List<String> statuses);
    List<Loan> findTop5ByOrderByIssueDateDesc();
    List<Loan> findByBorrowerId(Long borrowerId);
    long countByStatus(String status);

    @Query("SELECT l FROM Loan l WHERE l.status <> 'Completed' AND l.nextDueDate <= :date")
    List<Loan> findOverdueLoans(@Param("date") LocalDate date);

    @Query("SELECT l FROM Loan l WHERE l.status <> 'Completed' AND l.nextDueDate BETWEEN :start AND :end")
    List<Loan> findLoansDueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT l FROM Loan l WHERE l.issueDate BETWEEN :start AND :end")
    List<Loan> findLoansIssuedBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}

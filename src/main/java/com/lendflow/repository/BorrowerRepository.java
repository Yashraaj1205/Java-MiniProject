package com.lendflow.repository;

import com.lendflow.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    List<Borrower> findByNameContainingIgnoreCase(String name);
    Optional<Borrower> findByPhone(String phone);
    List<Borrower> findByRiskProfile(String riskProfile);
    List<Borrower> findByBusinessType(String businessType);
    List<Borrower> findAllByOrderByDateAddedDesc();
}


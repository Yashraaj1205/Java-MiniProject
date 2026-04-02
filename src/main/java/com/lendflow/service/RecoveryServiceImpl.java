package com.lendflow.service;

import com.lendflow.entity.Loan;
import com.lendflow.entity.RecoveryAction;
import com.lendflow.exception.ResourceNotFoundException;
import com.lendflow.repository.LoanRepository;
import com.lendflow.repository.RecoveryActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecoveryServiceImpl implements IRecoveryService {

    private final RecoveryActionRepository recoveryActionRepository;
    private final LoanRepository loanRepository;

    @Override
    public RecoveryAction addRecoveryAction(Long loanId, RecoveryAction action) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "ID", loanId));
        
        action.setLoan(loan);
        if (action.getActionDate() == null) {
            action.setActionDate(LocalDate.now());
        }
        
        return recoveryActionRepository.save(action);
    }

    @Override
    public List<RecoveryAction> getRecoveryActionsForLoan(Long loanId) {
        return recoveryActionRepository.findByLoanIdOrderByActionDateDesc(loanId);
    }

    @Override
    public List<RecoveryAction> getPendingFollowUps() {
        return recoveryActionRepository.findByFollowUpDateBeforeAndOutcomeNot(LocalDate.now().plusDays(1), "Successful");
    }
}


package com.lendflow.service;

import com.lendflow.entity.RecoveryAction;
import java.util.List;

public interface IRecoveryService {
    RecoveryAction addRecoveryAction(Long loanId, RecoveryAction action);
    List<RecoveryAction> getRecoveryActionsForLoan(Long loanId);
    List<RecoveryAction> getPendingFollowUps();
}


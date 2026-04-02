package com.lendflow.controller;

import com.lendflow.service.ILoanService;
import com.lendflow.service.IRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ILoanService loanService;
    private final IRecoveryService recoveryService;

    @ModelAttribute("notificationCount")
    public long getNotificationCount() {
        long approaching = loanService.getApproachingPayments(LocalDate.now().plusDays(2)).size();
        long overdue = loanService.getOverduePayments().size();
        long followups = recoveryService.getPendingFollowUps().size();
        
        return approaching + overdue + followups;
    }
}

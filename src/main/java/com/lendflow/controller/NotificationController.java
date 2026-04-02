package com.lendflow.controller;

import com.lendflow.entity.EMISchedule;
import com.lendflow.entity.RecoveryAction;
import com.lendflow.service.ILoanService;
import com.lendflow.service.IRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final ILoanService loanService;
    private final IRecoveryService recoveryService;

    @GetMapping
    public String viewNotifications(Model model) {
        List<EMISchedule> approaching = loanService.getApproachingPayments(LocalDate.now().plusDays(2));
        List<EMISchedule> overdue = loanService.getOverduePayments();
        List<RecoveryAction> followUps = recoveryService.getPendingFollowUps();

        model.addAttribute("approaching", approaching);
        model.addAttribute("overdue", overdue);
        model.addAttribute("followUps", followUps);
        
        return "notifications";
    }
}


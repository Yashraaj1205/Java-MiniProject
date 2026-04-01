package com.lendflow.controller;

import com.lendflow.entity.EMISchedule;
import com.lendflow.entity.RecoveryAction;
import com.lendflow.repository.EMIScheduleRepository;
import com.lendflow.service.RecoveryService;
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

    private final EMIScheduleRepository emiScheduleRepository;
    private final RecoveryService recoveryService;

    @GetMapping
    public String viewNotifications(Model model) {
        // Approaching EMIs (due tomorrow or today)
        List<EMISchedule> approaching = emiScheduleRepository.findByStatusAndDueDateBefore("Pending", LocalDate.now().plusDays(2));
        
        // Overdue EMIs
        List<EMISchedule> overdue = emiScheduleRepository.findAll().stream().filter(e -> "Overdue".equals(e.getStatus())).toList();

        // Pending Follow-Ups
        List<RecoveryAction> followUps = recoveryService.getPendingFollowUps();

        model.addAttribute("approaching", approaching);
        model.addAttribute("overdue", overdue);
        model.addAttribute("followUps", followUps);
        
        return "notifications";
    }
}

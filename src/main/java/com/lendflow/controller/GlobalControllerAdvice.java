package com.lendflow.controller;

import com.lendflow.repository.EMIScheduleRepository;
import com.lendflow.service.RecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final EMIScheduleRepository emiScheduleRepository;
    private final RecoveryService recoveryService;

    @ModelAttribute("notificationCount")
    public long getNotificationCount() {
        long approaching = emiScheduleRepository.findByStatusAndDueDateBefore("Pending", LocalDate.now().plusDays(2)).size();
        long overdue = emiScheduleRepository.findAll().stream().filter(e -> "Overdue".equals(e.getStatus())).count();
        long followups = recoveryService.getPendingFollowUps().size();
        
        return approaching + overdue + followups;
    }
}

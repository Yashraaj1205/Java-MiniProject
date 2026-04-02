package com.lendflow.service;

import com.lendflow.entity.Loan;
import com.lendflow.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final LoanRepository loanRepository;
    private final INotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(readOnly = true)
    public void sendDueReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        List<Loan> activeLoans = loanRepository.findAll().stream()
                .filter(loan -> "Active".equalsIgnoreCase(loan.getStatus()) || "Partial".equalsIgnoreCase(loan.getStatus()))
                .filter(loan -> loan.getNextDueDate() != null && loan.getNextDueDate().isEqual(tomorrow))
                .toList();

        for (Loan loan : activeLoans) {
            try {
                notificationService.sendDueReminderNotification(loan);
            } catch (Exception e) {
                log.error("Failed to send reminder for loan {}: {}", loan.getLoanReference(), e.getMessage());
            }
        }
    }
}


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
    private final NotificationService notificationService;

    // Run every day at 09:00 AM
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(readOnly = true)
    public void sendDueReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Checking for loans due tomorrow ({}) to send SMS reminders...", tomorrow);

        // Fetch all active loans with next due date exactly tomorrow
        // We can just filter the full list or use a custom query. 
        // For simplicity and assuming normal data volume, we'll fetch all active and filter.
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
        
        log.info("Finished sending {} due reminders.", activeLoans.size());
    }
}

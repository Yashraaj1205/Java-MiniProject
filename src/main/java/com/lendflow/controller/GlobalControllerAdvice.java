package com.lendflow.controller;

import com.lendflow.entity.Loan;
import com.lendflow.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final LoanRepository loanRepository;

    @ModelAttribute("notificationCount")
    public long getNotificationCount() {
        return loanRepository.findAll().stream()
                .filter(l -> !"Completed".equalsIgnoreCase(l.getStatus()))
                .filter(l -> ChronoUnit.DAYS.between(LocalDate.now(), l.getNextDueDate()) <= 1)
                .count();
    }
}

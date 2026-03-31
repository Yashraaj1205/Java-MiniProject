package com.lendflow.controller;

import com.lendflow.entity.Loan;
import com.lendflow.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final LoanRepository loanRepository;

    @GetMapping
    public String viewNotifications(Model model) {
        List<Loan> activeLoans = loanRepository.findAll().stream()
                .filter(l -> !"Completed".equalsIgnoreCase(l.getStatus()))
                .collect(Collectors.toList());

        List<Loan> approachingDeadlines = activeLoans.stream()
                .filter(l -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), l.getNextDueDate());
                    return days >= 0 && days <= 1; // Due strictly today or tomorrow
                })
                .collect(Collectors.toList());

        List<Loan> overdueLoans = activeLoans.stream()
                .filter(l -> ChronoUnit.DAYS.between(LocalDate.now(), l.getNextDueDate()) < 0)
                .collect(Collectors.toList());

        model.addAttribute("approaching", approachingDeadlines);
        model.addAttribute("overdue", overdueLoans);
        
        return "notifications";
    }
}

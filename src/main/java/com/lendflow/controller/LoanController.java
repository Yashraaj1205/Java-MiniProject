package com.lendflow.controller;

import com.lendflow.entity.Loan;
import com.lendflow.service.ILoanService;
import com.lendflow.service.IBorrowerService;
import com.lendflow.entity.Borrower;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.math.BigDecimal;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final ILoanService loanService;
    private final IBorrowerService borrowerService;

    @GetMapping
    public String listLoans(Model model) {
        model.addAttribute("loans", loanService.getAllLoans());
        model.addAttribute("borrowers", borrowerService.getAllBorrowers());
        model.addAttribute("newLoan", new Loan());
        return "loans";
    }

    @GetMapping("/{id}")
    public String loanDetails(@PathVariable Long id, Model model) {
        Loan loan = loanService.getLoanById(id);
        model.addAttribute("loan", loan);
        return "loan-detail";
    }

    @PostMapping("/add")
    public String addLoan(
            @ModelAttribute Loan newLoan, 
            @RequestParam Long borrowerId, 
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate issueDate) {
            
        Borrower borrower = borrowerService.getBorrowerById(borrowerId);
        if (issueDate != null) newLoan.setIssueDate(issueDate);
        loanService.createLoan(newLoan, borrower);
        return "redirect:/loans";
    }

    @PostMapping("/pay")
    public String recordPayment(
            @RequestParam Long loanId, 
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "Cash") String paymentMethod,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) BigDecimal lateFeePaid) {
        
        loanService.recordPayment(loanId, amount, paymentMethod, notes, lateFeePaid);
        return "redirect:/loans/" + loanId;
    }
}


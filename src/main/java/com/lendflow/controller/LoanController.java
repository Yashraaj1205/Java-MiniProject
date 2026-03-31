package com.lendflow.controller;

import com.lendflow.entity.Borrower;
import com.lendflow.entity.Loan;
import com.lendflow.repository.BorrowerRepository;
import com.lendflow.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;

    @GetMapping
    public String listLoans(Model model) {
        model.addAttribute("loans", loanRepository.findAll());
        model.addAttribute("borrowers", borrowerRepository.findAll());
        model.addAttribute("newLoan", new Loan());
        return "loans";
    }

    @PostMapping("/add")
    public String addLoan(@ModelAttribute Loan newLoan, @RequestParam Long borrowerId) {
        Borrower borrower = borrowerRepository.findById(borrowerId).orElseThrow(() -> new IllegalArgumentException("Invalid borrower Id:" + borrowerId));
        newLoan.setBorrower(borrower);
        newLoan.setLoanReference("LN-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        newLoan.setStatus("Active");
        
        if(newLoan.getIssueDate() == null) {
            newLoan.setIssueDate(LocalDate.now());
        }
        if(newLoan.getInterestRate() == null) {
            newLoan.setInterestRate(0.0);
        }
        
        // Auto-calculate Expected EMI based on Principal + Interest over a standard 12-month period
        java.math.BigDecimal principal = newLoan.getPrincipalAmount() == null ? java.math.BigDecimal.ZERO : newLoan.getPrincipalAmount();
        double rate = newLoan.getInterestRate() / 100.0;
        java.math.BigDecimal totalAmount = principal.add(principal.multiply(java.math.BigDecimal.valueOf(rate)));
        newLoan.setEmiAmount(totalAmount.divide(java.math.BigDecimal.valueOf(12), java.math.RoundingMode.HALF_UP));
        
        loanRepository.save(newLoan);
        return "redirect:/loans";
    }

    @PostMapping("/pay")
    public String recordPayment(@RequestParam Long loanId, @RequestParam java.math.BigDecimal amount) {
        Loan loan = loanRepository.findById(loanId).orElseThrow();
        
        com.lendflow.entity.Payment payment = com.lendflow.entity.Payment.builder()
                .loan(loan)
                .amountPaid(amount)
                .paymentDate(LocalDate.now())
                .build();
                
        if(loan.getPayments() == null) loan.setPayments(new java.util.ArrayList<>());
        loan.getPayments().add(payment);
        
        if(loan.getOutstandingBalance().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            loan.setStatus("Completed");
        }
        
        loanRepository.save(loan);
        return "redirect:/loans";
    }
}

package com.lendflow.controller;

import com.lendflow.entity.Borrower;
import com.lendflow.service.IBorrowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final IBorrowerService borrowerService;

    @GetMapping
    public String listBorrowers(Model model) {
        model.addAttribute("borrowers", borrowerService.getAllBorrowers());
        model.addAttribute("newBorrower", new Borrower());
        return "borrowers";
    }

    @GetMapping("/{id}")
    public String borrowerDetails(@PathVariable Long id, Model model) {
        Borrower borrower = borrowerService.getBorrowerById(id);
        model.addAttribute("borrower", borrower);
        return "borrower-detail";
    }

    @PostMapping("/add")
    public String addBorrower(Borrower borrower) {
        borrowerService.saveBorrower(borrower);
        return "redirect:/borrowers";
    }
}


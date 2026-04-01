package com.lendflow.controller;

import com.lendflow.entity.Borrower;
import com.lendflow.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerRepository borrowerRepository;

    @GetMapping
    public String listBorrowers(Model model) {
        model.addAttribute("borrowers", borrowerRepository.findAllByOrderByDateAddedDesc());
        model.addAttribute("newBorrower", new Borrower());
        return "borrowers";
    }

    @GetMapping("/{id}")
    public String borrowerDetails(@PathVariable Long id, Model model) {
        Borrower borrower = borrowerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid borrower ID"));
        model.addAttribute("borrower", borrower);
        return "borrower-detail";
    }

    @PostMapping("/add")
    public String addBorrower(Borrower borrower) {
        borrowerRepository.save(borrower);
        return "redirect:/borrowers";
    }
}

package com.lendflow.controller;

import com.lendflow.entity.Borrower;
import com.lendflow.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerRepository borrowerRepository;

    @GetMapping
    public String listBorrowers(Model model) {
        model.addAttribute("borrowers", borrowerRepository.findAll());
        model.addAttribute("newBorrower", new Borrower());
        return "borrowers";
    }

    @PostMapping("/add")
    public String addBorrower(Borrower borrower) {
        borrowerRepository.save(borrower);
        return "redirect:/borrowers";
    }
}

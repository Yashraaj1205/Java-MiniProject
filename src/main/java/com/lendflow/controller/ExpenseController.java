package com.lendflow.controller;

import com.lendflow.entity.Expense;
import com.lendflow.service.IExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final IExpenseService expenseService;

    @GetMapping
    public String listExpenses(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("expenses", expenseService.getExpensesForMonth(now.getYear(), now.getMonthValue()));
        model.addAttribute("totalExpenses", expenseService.getTotalExpensesForMonth(now.getYear(), now.getMonthValue()));
        model.addAttribute("categoryBreakdown", expenseService.getExpenseCategoryBreakdown(now.getYear(), now.getMonthValue()));
        model.addAttribute("newExpense", new Expense());
        model.addAttribute("currentMonth", now.getMonth().name());
        return "expenses";
    }

    @PostMapping("/add")
    public String addExpense(@ModelAttribute Expense expense) {
        expenseService.saveExpense(expense);
        return "redirect:/expenses";
    }
}


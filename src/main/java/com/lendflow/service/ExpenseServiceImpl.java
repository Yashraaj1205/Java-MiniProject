package com.lendflow.service;

import com.lendflow.entity.Expense;
import com.lendflow.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements IExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public Expense saveExpense(Expense expense) {
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
        }
        return expenseRepository.save(expense);
    }

    @Override
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByExpenseDateDesc();
    }

    @Override
    public List<Expense> getExpensesForMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(start, end);
    }

    @Override
    public BigDecimal getTotalExpensesForMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal total = expenseRepository.sumAmountBetween(start, end);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Map<String, BigDecimal> getExpenseCategoryBreakdown(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Object[]> results = expenseRepository.sumByCategoryBetween(start, end);
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }
}


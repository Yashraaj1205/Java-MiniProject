package com.lendflow.service;

import com.lendflow.entity.Expense;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IExpenseService {
    Expense saveExpense(Expense expense);
    List<Expense> getAllExpenses();
    List<Expense> getExpensesForMonth(int year, int month);
    BigDecimal getTotalExpensesForMonth(int year, int month);
    Map<String, BigDecimal> getExpenseCategoryBreakdown(int year, int month);
}


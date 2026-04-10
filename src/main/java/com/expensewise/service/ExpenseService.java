package com.expensewise.service;

import com.expensewise.domain.entity.Category;
import com.expensewise.domain.entity.Expense;
import com.expensewise.domain.repository.CategoryRepository;
import com.expensewise.domain.repository.ExpenseRepository;
import com.expensewise.dto.expense.ExpenseRequest;
import com.expensewise.dto.expense.ExpenseResponse;
import com.expensewise.exception.ResourceNotFoundException;
import com.expensewise.security.SecurityUserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityUserContext securityUserContext;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            SecurityUserContext securityUserContext
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.securityUserContext = securityUserContext;
    }

    public Page<ExpenseResponse> getAllExpenses(
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    ) {
        Long userId = securityUserContext.getCurrentUserId();
        return expenseRepository.findAllWithFilters(userId, categoryId, startDate, endDate, minAmount, maxAmount, pageable)
                .map(this::toResponse);
    }

    public ExpenseResponse getExpenseById(Long id) {
        Long userId = securityUserContext.getCurrentUserId();
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        return toResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        var user = securityUserContext.getCurrentUser();
        Category category = categoryRepository.findByIdAndUserIdOrDefault(request.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(request.amount())
                .description(request.description())
                .expenseDate(request.expenseDate())
                .receiptUrl(request.receiptUrl())
                .build();

        expense = expenseRepository.save(expense);
        return toResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        var user = securityUserContext.getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        Category category = categoryRepository.findByIdAndUserIdOrDefault(request.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        expense.setCategory(category);
        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setExpenseDate(request.expenseDate());
        expense.setReceiptUrl(request.receiptUrl());

        expense = expenseRepository.save(expense);
        return toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Long userId = securityUserContext.getCurrentUserId();
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        expenseRepository.delete(expense);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getCategory().getId(),
                expense.getCategory().getName(),
                expense.getCategory().getIcon(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getReceiptUrl(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}

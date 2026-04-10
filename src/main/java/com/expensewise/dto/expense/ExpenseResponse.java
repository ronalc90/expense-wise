package com.expensewise.dto.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
    Long id,
    Long categoryId,
    String categoryName,
    String categoryIcon,
    BigDecimal amount,
    String description,
    LocalDate expenseDate,
    String receiptUrl,
    Instant createdAt,
    Instant updatedAt
) {}

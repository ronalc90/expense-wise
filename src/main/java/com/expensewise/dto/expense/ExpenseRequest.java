package com.expensewise.dto.expense;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
    @NotNull(message = "Category ID is required")
    Long categoryId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount format is invalid")
    BigDecimal amount,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @NotNull(message = "Expense date is required")
    LocalDate expenseDate,

    @Size(max = 500, message = "Receipt URL must not exceed 500 characters")
    String receiptUrl
) {}

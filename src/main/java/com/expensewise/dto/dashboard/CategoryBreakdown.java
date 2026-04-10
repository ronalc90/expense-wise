package com.expensewise.dto.dashboard;

import java.math.BigDecimal;

public record CategoryBreakdown(
    Long categoryId,
    String categoryName,
    String categoryIcon,
    BigDecimal totalAmount,
    long transactionCount,
    BigDecimal percentage
) {}

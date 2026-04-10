package com.expensewise.dto.dashboard;

import java.math.BigDecimal;

public record MonthlyTrend(
    int year,
    int month,
    BigDecimal totalAmount,
    long transactionCount
) {}

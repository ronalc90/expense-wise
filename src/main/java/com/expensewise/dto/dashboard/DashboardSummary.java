package com.expensewise.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummary(
    BigDecimal totalExpenses,
    long transactionCount,
    BigDecimal averageExpense,
    BigDecimal highestExpense
) {}

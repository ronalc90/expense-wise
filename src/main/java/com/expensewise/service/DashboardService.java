package com.expensewise.service;

import com.expensewise.domain.repository.ExpenseRepository;
import com.expensewise.dto.dashboard.CategoryBreakdown;
import com.expensewise.dto.dashboard.DashboardSummary;
import com.expensewise.dto.dashboard.MonthlyTrend;
import com.expensewise.security.SecurityUserContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final SecurityUserContext securityUserContext;

    public DashboardService(ExpenseRepository expenseRepository, SecurityUserContext securityUserContext) {
        this.expenseRepository = expenseRepository;
        this.securityUserContext = securityUserContext;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Double d) return BigDecimal.valueOf(d);
        if (value instanceof Long l) return BigDecimal.valueOf(l);
        if (value instanceof Integer i) return BigDecimal.valueOf(i);
        return new BigDecimal(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer i) return i;
        if (value instanceof Long l) return l.intValue();
        return Integer.parseInt(value.toString());
    }

    public DashboardSummary getSummary(LocalDate startDate, LocalDate endDate) {
        Long userId = securityUserContext.getCurrentUserId();

        BigDecimal total = toBigDecimal(expenseRepository.sumByUserIdAndDateRange(userId, startDate, endDate));
        long count = expenseRepository.countByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal highest = toBigDecimal(expenseRepository.maxAmountByUserIdAndDateRange(userId, startDate, endDate));

        BigDecimal average = count > 0
                ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new DashboardSummary(total, count, average, highest);
    }

    public List<CategoryBreakdown> getCategoryBreakdown(LocalDate startDate, LocalDate endDate) {
        Long userId = securityUserContext.getCurrentUserId();

        List<Object[]> results = expenseRepository.sumByCategoryAndDateRange(userId, startDate, endDate);
        BigDecimal grandTotal = results.stream()
                .map(row -> toBigDecimal(row[3]))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream()
                .map(row -> {
                    BigDecimal amount = toBigDecimal(row[3]);
                    BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? amount.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new CategoryBreakdown(
                            toLong(row[0]),
                            (String) row[1],
                            (String) row[2],
                            amount,
                            toLong(row[4]),
                            percentage
                    );
                })
                .toList();
    }

    public List<MonthlyTrend> getMonthlyTrend(LocalDate startDate, LocalDate endDate) {
        Long userId = securityUserContext.getCurrentUserId();

        return expenseRepository.monthlyTrend(userId, startDate, endDate).stream()
                .map(row -> new MonthlyTrend(
                        toInteger(row[0]),
                        toInteger(row[1]),
                        toBigDecimal(row[2]),
                        toLong(row[3])
                ))
                .toList();
    }
}

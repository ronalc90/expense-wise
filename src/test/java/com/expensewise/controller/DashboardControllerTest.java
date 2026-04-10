package com.expensewise.controller;

import com.expensewise.domain.entity.Category;
import com.expensewise.domain.entity.Expense;
import com.expensewise.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardControllerTest extends BaseIntegrationTest {

    @BeforeEach
    void setUpDashboardData() {
        Category food = categoryRepository.save(Category.builder()
                .name("Food").icon("utensils").user(testUser).isDefault(false).build());
        Category transport = categoryRepository.save(Category.builder()
                .name("Transport").icon("car").user(testUser).isDefault(false).build());

        expenseRepository.save(Expense.builder()
                .user(testUser).category(food).amount(new BigDecimal("50.00"))
                .description("Groceries").expenseDate(LocalDate.of(2024, 1, 10)).build());
        expenseRepository.save(Expense.builder()
                .user(testUser).category(food).amount(new BigDecimal("30.00"))
                .description("Restaurant").expenseDate(LocalDate.of(2024, 1, 15)).build());
        expenseRepository.save(Expense.builder()
                .user(testUser).category(transport).amount(new BigDecimal("20.00"))
                .description("Uber").expenseDate(LocalDate.of(2024, 1, 20)).build());
    }

    @Test
    @DisplayName("Should return dashboard summary")
    void getSummary_Success() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + testToken)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses", is(100.00)))
                .andExpect(jsonPath("$.transactionCount", is(3)))
                .andExpect(jsonPath("$.highestExpense", is(50.00)));
    }

    @Test
    @DisplayName("Should return category breakdown")
    void getCategoryBreakdown_Success() throws Exception {
        mockMvc.perform(get("/api/dashboard/by-category")
                        .header("Authorization", "Bearer " + testToken)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].categoryName", is("Food")))
                .andExpect(jsonPath("$[0].totalAmount", is(80.00)));
    }

    @Test
    @DisplayName("Should return monthly trend")
    void getMonthlyTrend_Success() throws Exception {
        mockMvc.perform(get("/api/dashboard/monthly-trend")
                        .header("Authorization", "Bearer " + testToken)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].year", is(2024)))
                .andExpect(jsonPath("$[0].month", is(1)))
                .andExpect(jsonPath("$[0].totalAmount", is(100.00)));
    }

    @Test
    @DisplayName("Should return empty summary for date range with no expenses")
    void getSummary_EmptyRange() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + testToken)
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses", is(0)))
                .andExpect(jsonPath("$.transactionCount", is(0)));
    }
}

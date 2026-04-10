package com.expensewise.controller;

import com.expensewise.domain.entity.Category;
import com.expensewise.domain.entity.Expense;
import com.expensewise.dto.expense.ExpenseRequest;
import com.expensewise.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExpenseControllerTest extends BaseIntegrationTest {

    private Category testCategory;
    private Expense testExpense;

    @BeforeEach
    void setUpExpenses() {
        testCategory = Category.builder()
                .name("Food")
                .icon("utensils")
                .user(testUser)
                .isDefault(false)
                .build();
        testCategory = categoryRepository.save(testCategory);

        testExpense = Expense.builder()
                .user(testUser)
                .category(testCategory)
                .amount(new BigDecimal("25.50"))
                .description("Lunch")
                .expenseDate(LocalDate.of(2024, 1, 15))
                .build();
        testExpense = expenseRepository.save(testExpense);
    }

    @Test
    @DisplayName("Should create a new expense")
    void createExpense_Success() throws Exception {
        var request = new ExpenseRequest(
                testCategory.getId(),
                new BigDecimal("42.00"),
                "Dinner",
                LocalDate.of(2024, 1, 16),
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(42.00)))
                .andExpect(jsonPath("$.description", is("Dinner")))
                .andExpect(jsonPath("$.categoryName", is("Food")));
    }

    @Test
    @DisplayName("Should get expense by ID")
    void getExpenseById_Success() throws Exception {
        mockMvc.perform(get("/api/expenses/{id}", testExpense.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testExpense.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(25.50)))
                .andExpect(jsonPath("$.description", is("Lunch")));
    }

    @Test
    @DisplayName("Should list expenses with pagination")
    void getAllExpenses_Success() throws Exception {
        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + testToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Should update an expense")
    void updateExpense_Success() throws Exception {
        var request = new ExpenseRequest(
                testCategory.getId(),
                new BigDecimal("30.00"),
                "Updated Lunch",
                LocalDate.of(2024, 1, 15),
                null
        );

        mockMvc.perform(put("/api/expenses/{id}", testExpense.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(30.00)))
                .andExpect(jsonPath("$.description", is("Updated Lunch")));
    }

    @Test
    @DisplayName("Should delete an expense")
    void deleteExpense_Success() throws Exception {
        mockMvc.perform(delete("/api/expenses/{id}", testExpense.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses/{id}", testExpense.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should fail to create expense without authentication")
    void createExpense_Unauthorized() throws Exception {
        var request = new ExpenseRequest(
                testCategory.getId(),
                new BigDecimal("42.00"),
                "Dinner",
                LocalDate.of(2024, 1, 16),
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to create expense with invalid amount")
    void createExpense_InvalidAmount() throws Exception {
        var request = new ExpenseRequest(
                testCategory.getId(),
                new BigDecimal("-10.00"),
                "Invalid",
                LocalDate.of(2024, 1, 16),
                null
        );

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 for non-existent expense")
    void getExpenseById_NotFound() throws Exception {
        mockMvc.perform(get("/api/expenses/{id}", 99999L)
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNotFound());
    }
}

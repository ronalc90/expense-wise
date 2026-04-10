package com.expensewise.service;

import com.expensewise.domain.entity.Category;
import com.expensewise.domain.entity.Expense;
import com.expensewise.domain.entity.User;
import com.expensewise.domain.repository.CategoryRepository;
import com.expensewise.domain.repository.ExpenseRepository;
import com.expensewise.dto.expense.ExpenseRequest;
import com.expensewise.dto.expense.ExpenseResponse;
import com.expensewise.exception.ResourceNotFoundException;
import com.expensewise.security.SecurityUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SecurityUserContext securityUserContext;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Category testCategory;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").name("Test").build();
        testCategory = Category.builder().id(1L).name("Food").icon("utensils").user(testUser).build();
        testExpense = Expense.builder()
                .id(1L)
                .user(testUser)
                .category(testCategory)
                .amount(new BigDecimal("25.50"))
                .description("Lunch")
                .expenseDate(LocalDate.of(2024, 1, 15))
                .build();
    }

    @Test
    @DisplayName("Should create an expense successfully")
    void createExpense_Success() {
        var request = new ExpenseRequest(1L, new BigDecimal("25.50"), "Lunch", LocalDate.of(2024, 1, 15), null);

        when(securityUserContext.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUserIdOrDefault(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        ExpenseResponse response = expenseService.createExpense(request);

        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(response.description()).isEqualTo("Lunch");
        assertThat(response.categoryName()).isEqualTo("Food");

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void createExpense_CategoryNotFound() {
        var request = new ExpenseRequest(999L, new BigDecimal("25.50"), "Lunch", LocalDate.of(2024, 1, 15), null);

        when(securityUserContext.getCurrentUser()).thenReturn(testUser);
        when(categoryRepository.findByIdAndUserIdOrDefault(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    @DisplayName("Should get expense by ID")
    void getExpenseById_Success() {
        when(securityUserContext.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testExpense));

        ExpenseResponse response = expenseService.getExpenseById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    @DisplayName("Should throw exception when expense not found")
    void getExpenseById_NotFound() {
        when(securityUserContext.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense");
    }

    @Test
    @DisplayName("Should update an expense successfully")
    void updateExpense_Success() {
        var request = new ExpenseRequest(1L, new BigDecimal("30.00"), "Updated", LocalDate.of(2024, 1, 15), null);
        var updatedExpense = Expense.builder()
                .id(1L).user(testUser).category(testCategory)
                .amount(new BigDecimal("30.00")).description("Updated")
                .expenseDate(LocalDate.of(2024, 1, 15)).build();

        when(securityUserContext.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(categoryRepository.findByIdAndUserIdOrDefault(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);

        ExpenseResponse response = expenseService.updateExpense(1L, request);

        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(response.description()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Should delete an expense successfully")
    void deleteExpense_Success() {
        when(securityUserContext.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testExpense));

        expenseService.deleteExpense(1L);

        verify(expenseRepository).delete(testExpense);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent expense")
    void deleteExpense_NotFound() {
        when(securityUserContext.getCurrentUserId()).thenReturn(1L);
        when(expenseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

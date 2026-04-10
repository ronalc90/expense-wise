package com.expensewise.domain.repository;

import com.expensewise.domain.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.id = :id AND e.user.id = :userId")
    Optional<Expense> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
        SELECT e FROM Expense e
        JOIN FETCH e.category
        WHERE e.user.id = :userId
        AND (:categoryId IS NULL OR e.category.id = :categoryId)
        AND (:startDate IS NULL OR e.expenseDate >= :startDate)
        AND (:endDate IS NULL OR e.expenseDate <= :endDate)
        AND (:minAmount IS NULL OR e.amount >= :minAmount)
        AND (:maxAmount IS NULL OR e.amount <= :maxAmount)
        """)
    Page<Expense> findAllWithFilters(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount,
        Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    long countByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(MAX(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal maxAmountByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT e.category.id, e.category.name, e.category.icon, COALESCE(SUM(e.amount), 0), COUNT(e)
        FROM Expense e
        WHERE e.user.id = :userId
        AND e.expenseDate BETWEEN :startDate AND :endDate
        GROUP BY e.category.id, e.category.name, e.category.icon
        ORDER BY SUM(e.amount) DESC
        """)
    List<Object[]> sumByCategoryAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT YEAR(e.expenseDate), MONTH(e.expenseDate), COALESCE(SUM(e.amount), 0), COUNT(e)
        FROM Expense e
        WHERE e.user.id = :userId
        AND e.expenseDate BETWEEN :startDate AND :endDate
        GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate)
        ORDER BY YEAR(e.expenseDate), MONTH(e.expenseDate)
        """)
    List<Object[]> monthlyTrend(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findAllByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

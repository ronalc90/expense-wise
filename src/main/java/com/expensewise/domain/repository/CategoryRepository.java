package com.expensewise.domain.repository;

import com.expensewise.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.isDefault = true ORDER BY c.isDefault DESC, c.name ASC")
    List<Category> findAllByUserIdOrDefault(@Param("userId") Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user.id = :userId OR c.isDefault = true)")
    Optional<Category> findByIdAndUserIdOrDefault(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByNameAndUserId(String name, Long userId);
}

package com.expensewise.controller;

import com.expensewise.domain.entity.Category;
import com.expensewise.dto.category.CategoryRequest;
import com.expensewise.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerTest extends BaseIntegrationTest {

    private Category userCategory;
    private Category defaultCategory;

    @BeforeEach
    void setUpCategories() {
        defaultCategory = Category.builder()
                .name("Food & Dining")
                .icon("utensils")
                .isDefault(true)
                .build();
        defaultCategory = categoryRepository.save(defaultCategory);

        userCategory = Category.builder()
                .name("My Custom Category")
                .icon("star")
                .user(testUser)
                .isDefault(false)
                .build();
        userCategory = categoryRepository.save(userCategory);
    }

    @Test
    @DisplayName("Should list all categories including defaults")
    void getAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].isDefault", is(true)));
    }

    @Test
    @DisplayName("Should get category by ID")
    void getCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", userCategory.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("My Custom Category")));
    }

    @Test
    @DisplayName("Should create a new category")
    void createCategory_Success() throws Exception {
        var request = new CategoryRequest("Travel Expenses", "plane");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Travel Expenses")))
                .andExpect(jsonPath("$.icon", is("plane")))
                .andExpect(jsonPath("$.isDefault", is(false)));
    }

    @Test
    @DisplayName("Should fail to create category with duplicate name")
    void createCategory_DuplicateName() throws Exception {
        var request = new CategoryRequest("My Custom Category", "star");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should update a user category")
    void updateCategory_Success() throws Exception {
        var request = new CategoryRequest("Updated Category", "edit");

        mockMvc.perform(put("/api/categories/{id}", userCategory.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Category")));
    }

    @Test
    @DisplayName("Should fail to update a default category")
    void updateCategory_DefaultFails() throws Exception {
        var request = new CategoryRequest("Renamed Default", "edit");

        mockMvc.perform(put("/api/categories/{id}", defaultCategory.getId())
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete a user category")
    void deleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", userCategory.getId())
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should fail without authentication")
    void getAllCategories_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }
}

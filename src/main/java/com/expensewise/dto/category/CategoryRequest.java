package com.expensewise.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    String name,

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    String icon
) {}

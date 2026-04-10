package com.expensewise.dto.category;

import java.time.Instant;

public record CategoryResponse(
    Long id,
    String name,
    String icon,
    boolean isDefault,
    Instant createdAt
) {}

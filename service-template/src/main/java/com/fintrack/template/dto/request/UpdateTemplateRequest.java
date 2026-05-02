package com.fintrack.template.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for updating an existing template item.
 *
 * <p>All fields are optional — only non-null fields will be applied.
 * Rename to {@code Update<YourDomain>Request}.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
public class UpdateTemplateRequest {

    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}


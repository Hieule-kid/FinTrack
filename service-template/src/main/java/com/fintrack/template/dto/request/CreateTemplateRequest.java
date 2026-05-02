package com.fintrack.template.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for creating a new template item.
 *
 * <p>Rename this class to {@code Create<YourDomain>Request} and replace
 * the fields with your domain's required fields.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
public class CreateTemplateRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}


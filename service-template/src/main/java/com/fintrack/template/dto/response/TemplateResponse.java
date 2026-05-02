package com.fintrack.template.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a template item.
 *
 * <p>This is the public-facing view of {@code TemplateEntity}.
 * Rename to {@code <YourDomain>Response} and add domain-specific fields.
 *
 * <p>Never expose sensitive internal fields in response DTOs.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateResponse {

    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}


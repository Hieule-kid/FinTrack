package com.fintrack.auth.dto.request;

import com.fintrack.auth.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for the {@code PATCH /api/v1/users/currency} endpoint.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
public class UpdateCurrencyRequest {

    @Schema(description = "Preferred currency for the user", example = "USD")
    @NotNull(message = "Currency is required")
    private Currency currency;
}

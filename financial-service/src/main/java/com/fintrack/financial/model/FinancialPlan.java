package com.fintrack.financial.model;

import com.fintrack.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a financial savings plan.
 *
 * <p>Allows users to set financial goals with target amounts and deadlines.
 * Tracks progress through associated {@link Milestone} entities.
 *
 * <p><b>⚠️ Soft Link to Auth Service:</b> {@code userId} is a VARCHAR UUID that references
 * the User in auth-service. There is NO physical Foreign Key across services.
 *
 * <p>Table: {@code financial_plans}
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "financial_plans")
public class FinancialPlan extends BaseEntity {

    /**
     * ID of the user who created this plan.
     * ⚠️ Soft Link: VARCHAR UUID referencing User in auth-service (no physical FK).
     * Every query must filter by this to ensure multi-tenancy.
     */
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false, length = 36, updatable = false)
    private String userId;

    /**
     * Human-readable name for this financial plan (e.g., "House Down Payment", "Vacation Fund").
     */
    @NotBlank(message = "Plan name cannot be blank")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Optional description providing context or purpose for this plan.
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Target amount in the base currency (e.g., USD).
     * Must be positive.
     */
    @NotNull(message = "Target amount cannot be null")
    @Min(value = 1, message = "Target amount must be greater than 0")
    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    /**
     * Current savings accumulated towards this goal.
     * Starts at zero and increases as milestones are achieved.
     * Must not exceed targetAmount.
     */
    @Builder.Default
    @Column(name = "current_savings", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentSavings = BigDecimal.ZERO;

    /**
     * Target date (deadline) for achieving this goal.
     * Stored in UTC for consistency across timezones.
     */
    @NotNull(message = "Target date cannot be null")
    @Column(name = "target_date", nullable = false)
    private ZonedDateTime targetDate;

    /**
     * Associated milestones (sub-goals) that make up this plan.
     * Cascade delete ensures orphaned milestones are removed when the plan is deleted.
     * Uses HashSet to ensure milestones with the same ID are not duplicated.
     */
    @Builder.Default
    @OneToMany(mappedBy = "financialPlan", orphanRemoval = true)
    private Set<Milestone> milestones = new HashSet<>();

    /**
     * Calculate the progress percentage (0-100).
     *
     * @return progress as a percentage rounded to 2 decimal places
     */
    public BigDecimal calculateProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentSavings.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100));
    }

    /**
     * Check if this plan has been completed.
     *
     * @return true if currentSavings >= targetAmount
     */
    public boolean isCompleted() {
        return currentSavings.compareTo(targetAmount) >= 0;
    }
}



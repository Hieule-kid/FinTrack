package com.fintrack.financial.model;

import com.fintrack.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

/**
 * JPA entity representing a milestone within a {@link FinancialPlan}.
 *
 * <p>Milestones break down larger savings goals into smaller, achievable targets.
 * When all milestones are complete, the parent plan is considered complete.
 *
 * <p>Table: {@code milestones}
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
@Table(name = "milestones")
public class Milestone extends BaseEntity {

    /**
     * The parent {@link FinancialPlan} this milestone belongs to.
     * Physical Foreign Key — enforces referential integrity at the database level.
     */
    @NotNull(message = "Financial plan cannot be null")
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "financial_plan_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_milestones_financial_plan_id")
    )
    private FinancialPlan financialPlan;

    /**
     * Name or description of this milestone (e.g., "Initial Deposit", "Second Quarter Goal").
     */
    @NotBlank(message = "Milestone name cannot be blank")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Target amount for this milestone in the base currency.
     * Must be positive and typically less than or equal to the parent plan's target amount.
     */
    @NotNull(message = "Target amount cannot be null")
    @Min(value = 1, message = "Target amount must be greater than 0")
    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    /**
     * Flag indicating whether this milestone has been achieved.
     * When true, the milestone is considered complete.
     */
    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    /**
     * Target date (deadline) for achieving this milestone.
     * Stored in UTC for consistency across timezones.
     * Typically before the parent plan's target date.
     */
    @NotNull(message = "Target date cannot be null")
    @Column(name = "target_date", nullable = false)
    private ZonedDateTime targetDate;
}



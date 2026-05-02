package com.fintrack.template.model;

import com.fintrack.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Template JPA entity — rename to your domain entity (e.g. {@code Transaction}, {@code Budget}).
 *
 * <p>Extends {@link BaseEntity} to inherit:
 * <ul>
 *   <li>{@code id} — UUID primary key</li>
 *   <li>{@code createdAt}, {@code updatedAt} — auto-audited timestamps</li>
 *   <li>{@code createdBy}, {@code updatedBy} — auto-audited user IDs</li>
 *   <li>{@code deleted} — soft-delete flag</li>
 * </ul>
 *
 * <p>Table: change {@code "template_items"} to your table name (snake_case, plural).
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "template_items")
public class TemplateEntity extends BaseEntity {

    // ── Add your domain fields below ─────────────────────────────────────────

    /** Example field — replace with your domain data. */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Example field — replace with your domain data. */
    @Column(name = "description", length = 500)
    private String description;
}

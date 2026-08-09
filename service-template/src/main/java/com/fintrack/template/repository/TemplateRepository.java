package com.fintrack.template.repository;

import com.fintrack.template.model.TemplateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link TemplateEntity}.
 *
 * <p>Spring Data JPA generates the SQL at runtime from method names.
 * Extend with custom {@code @Query} methods as your domain grows.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, String> {

}

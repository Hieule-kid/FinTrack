package com.fintrack.template.service.impl;

import com.fintrack.core.dto.PageResponse;
import com.fintrack.core.exception.AppException;
import com.fintrack.core.exception.ErrorCode;
import com.fintrack.template.dto.request.CreateTemplateRequest;
import com.fintrack.template.dto.request.UpdateTemplateRequest;
import com.fintrack.template.dto.response.TemplateResponse;
import com.fintrack.template.model.TemplateEntity;
import com.fintrack.template.repository.TemplateRepository;
import com.fintrack.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link TemplateService}.
 *
 * <p>Demonstrates the standard FinTrack service layer pattern:
 * <ol>
 *   <li>Map inbound DTO → entity</li>
 *   <li>Apply business rules / validations</li>
 *   <li>Persist via repository</li>
 *   <li>Map entity → response DTO and return</li>
 * </ol>
 *
 * <p>For production services, consider replacing the manual mapping with
 * MapStruct for cleaner, type-safe conversions.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateResponse create(CreateTemplateRequest request) {
        TemplateEntity entity = TemplateEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        TemplateEntity saved = templateRepository.save(entity);
        log.debug("Created template item: id={}", saved.getId());

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateResponse findById(String id) {
        return templateRepository.findById(id)
                // Only return non-deleted documents
                .filter(entity -> !entity.isDeleted())
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Template item not found with id: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<TemplateResponse> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TemplateEntity> entityPage = templateRepository.findByDeletedFalse(pageable);

        return PageResponse.of(entityPage.map(this::toResponse));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Applies partial updates — only non-null fields in the request are changed.
     */
    @Override
    public TemplateResponse update(String id, UpdateTemplateRequest request) {
        TemplateEntity entity = templateRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Template item not found with id: " + id));

        // Partial update — only overwrite if the field is provided
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }

        TemplateEntity updated = templateRepository.save(entity);
        log.debug("Updated template item: id={}", updated.getId());

        return toResponse(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE (soft)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Sets {@code deleted = true} — document is NOT physically removed.
     */
    @Override
    public void delete(String id) {
        TemplateEntity entity = templateRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Template item not found with id: " + id));

        entity.setDeleted(true);
        templateRepository.save(entity);
        log.info("Soft-deleted template item: id={}", id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps a {@link TemplateEntity} to a {@link TemplateResponse} DTO.
     *
     * <p>In a real service, replace this with a MapStruct mapper interface.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    private TemplateResponse toResponse(TemplateEntity entity) {
        return TemplateResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}


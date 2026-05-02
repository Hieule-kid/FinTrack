package com.fintrack.template.service;

import com.fintrack.core.base.BaseService;
import com.fintrack.template.dto.request.CreateTemplateRequest;
import com.fintrack.template.dto.request.UpdateTemplateRequest;
import com.fintrack.template.dto.response.TemplateResponse;

/**
 * Service contract for template CRUD operations.
 *
 * <p>Extends {@link BaseService} to inherit the standard CRUD method signatures.
 * Add domain-specific methods here as needed.
 *
 * <p>Rename to {@code <YourDomain>Service} when creating a new service.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
public interface TemplateService extends BaseService<
        CreateTemplateRequest,
        UpdateTemplateRequest,
        TemplateResponse> {

    // Add domain-specific methods here.
    // Example:
    // List<TemplateResponse> findByName(String name);
}


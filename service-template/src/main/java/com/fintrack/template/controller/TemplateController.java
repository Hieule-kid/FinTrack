package com.fintrack.template.controller;

import com.fintrack.core.base.BaseController;
import com.fintrack.template.dto.request.CreateTemplateRequest;
import com.fintrack.template.dto.request.UpdateTemplateRequest;
import com.fintrack.template.dto.response.TemplateResponse;
import com.fintrack.template.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for template CRUD operations.
 *
 * <p>Inherits all standard CRUD endpoints from {@link BaseController}:
 * <pre>
 *   POST   /api/v1/templates        → create
 *   GET    /api/v1/templates/{id}   → findById
 *   PUT    /api/v1/templates/{id}   → update
 *   DELETE /api/v1/templates/{id}   → delete (soft)
 *   GET    /api/v1/templates        → findAll (paginated)
 * </pre>
 *
 * <p>Add domain-specific endpoints by defining additional {@code @GetMapping}
 * or {@code @PostMapping} methods directly in this class.
 *
 * @author FinTrack Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/templates")
@Tag(name = "Template", description = "CRUD operations for template items — rename when creating a new service")
@SecurityRequirement(name = "Bearer Authentication")
public class TemplateController extends BaseController<
        CreateTemplateRequest,
        UpdateTemplateRequest,
        TemplateResponse> {

    /**
     * Constructor — injects the service and passes it to {@link BaseController}.
     *
     * @param templateService the template domain service
     */
    public TemplateController(TemplateService templateService) {
        super(templateService);
    }

    // Add custom endpoints here.
    // Example:
    //
    // @Operation(summary = "Search by name")
    // @GetMapping("/search")
    // public ResponseEntity<ApiResponse<List<TemplateResponse>>> search(@RequestParam String name) {
    //     ...
    // }
}

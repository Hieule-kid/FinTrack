package com.fintrack.core.base;

import com.fintrack.core.dto.ApiResponse;
import com.fintrack.core.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Abstract base REST controller that wires standard CRUD endpoints
 * to a {@link BaseService} implementation.
 *
 * <p>Each concrete controller must:
 * <ol>
 *   <li>Extend this class with the appropriate generic types</li>
 *   <li>Annotate the class with {@code @RestController} and {@code @RequestMapping}</li>
 *   <li>Inject the service via the constructor (handled by {@code @RequiredArgsConstructor})</li>
 * </ol>
 *
 * <p>Exposed endpoints (relative to the controller's {@code @RequestMapping}):
 * <pre>
 *   POST   /          → create
 *   GET    /{id}      → findById
 *   PUT    /{id}      → update
 *   DELETE /{id}      → delete (soft)
 *   GET    /          → findAll (paginated)
 * </pre>
 *
 * <p>Usage:
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/v1/transactions")
 * public class TransactionController extends BaseController<CreateTransactionRequest,
 *                                                            UpdateTransactionRequest,
 *                                                            TransactionResponse> {
 *     public TransactionController(TransactionService service) {
 *         super(service);
 *     }
 * }
 * }</pre>
 *
 * @param <C> create request DTO type
 * @param <U> update request DTO type
 * @param <R> response DTO type
 * @author FinTrack Team
 * @since 1.0.0
 */
@RequiredArgsConstructor
public abstract class BaseController<C, U, R> {

    /** The domain service that handles all business logic. */
    private final BaseService<C, U, R> service;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new resource.
     *
     * @param createRequest validated create payload
     * @return {@code 201 Created} with the created resource
     */
    @PostMapping
    public ResponseEntity<ApiResponse<R>> create(@Valid @RequestBody C createRequest) {
        R result = service.create(createRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Resource created successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a single resource by ID.
     *
     * @param id the resource's MongoDB ObjectId
     * @return {@code 200 OK} with the resource
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<R>> findById(@PathVariable String id) {
        R result = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Returns a paginated, sorted list of resources.
     *
     * @param page    zero-based page index (default: 0)
     * @param size    items per page (default: 10)
     * @param sortBy  sort field (default: {@code "createdAt"})
     * @param sortDir sort direction {@code "asc"} or {@code "desc"} (default: {@code "desc"})
     * @return {@code 200 OK} with pagination metadata and content list
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<R>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<R> result = service.findAll(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates an existing resource.
     *
     * @param id            the resource's MongoDB ObjectId
     * @param updateRequest validated update payload
     * @return {@code 200 OK} with the updated resource
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<R>> update(
            @PathVariable String id,
            @Valid @RequestBody U updateRequest) {
        R result = service.update(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(result, "Resource updated successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE (soft)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Soft-deletes a resource (sets {@code deleted = true}).
     *
     * @param id the resource's MongoDB ObjectId
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


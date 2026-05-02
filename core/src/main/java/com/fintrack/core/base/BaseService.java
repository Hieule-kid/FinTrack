package com.fintrack.core.base;

import com.fintrack.core.dto.PageResponse;

/**
 * Generic CRUD service contract for all FinTrack services.
 *
 * <p>All business service interfaces should extend this interface
 * to guarantee consistent CRUD behaviour across the platform.
 *
 * <p>Type parameters:
 * <ul>
 *   <li>{@code C} — Create DTO (inbound, validated request body)</li>
 *   <li>{@code U} — Update DTO (inbound, validated request body)</li>
 *   <li>{@code R} — Response DTO (outbound, safe view of the resource)</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * public interface TransactionService extends BaseService<CreateTransactionRequest,
 *                                                          UpdateTransactionRequest,
 *                                                          TransactionResponse> {
 *     // additional domain-specific methods here
 * }
 * }</pre>
 *
 * @param <C> create request DTO type
 * @param <U> update request DTO type
 * @param <R> response DTO type
 * @author FinTrack Team
 * @since 1.0.0
 */
public interface BaseService<C, U, R> {

    /**
     * Creates a new resource from the given request.
     *
     * @param createRequest the validated creation payload; must not be {@code null}
     * @return the created resource as a response DTO
     */
    R create(C createRequest);

    /**
     * Updates an existing resource identified by {@code id}.
     *
     * @param id            the MongoDB ObjectId string; must not be {@code null}
     * @param updateRequest the validated update payload; must not be {@code null}
     * @return the updated resource as a response DTO
     * @throws com.fintrack.core.exception.AppException if no resource with {@code id} exists
     */
    R update(String id, U updateRequest);

    /**
     * Retrieves a single resource by its ID.
     *
     * @param id the MongoDB ObjectId string; must not be {@code null}
     * @return the resource as a response DTO
     * @throws com.fintrack.core.exception.AppException if no resource with {@code id} exists
     */
    R findById(String id);

    /**
     * Returns a paginated list of non-deleted resources.
     *
     * @param page    zero-based page index (0..N)
     * @param size    number of items per page (1..100 recommended)
     * @param sortBy  field name to sort by (e.g. {@code "createdAt"})
     * @param sortDir sort direction, {@code "asc"} or {@code "desc"}
     * @return a {@link PageResponse} wrapping the list and pagination metadata
     */
    PageResponse<R> findAll(int page, int size, String sortBy, String sortDir);

    /**
     * Soft-deletes the resource identified by {@code id}.
     *
     * <p>Sets {@code deleted = true}; the document is NOT physically removed
     * from the database.
     *
     * @param id the MongoDB ObjectId string; must not be {@code null}
     * @throws com.fintrack.core.exception.AppException if no resource with {@code id} exists
     */
    void delete(String id);
}


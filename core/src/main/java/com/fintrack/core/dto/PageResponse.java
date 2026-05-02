package com.fintrack.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pagination response wrapper returned by all paginated list endpoints.
 *
 * <p>Wraps a {@link Page} result from Spring Data into a FinTrack-standard DTO
 * so the shape is decoupled from Spring internals:
 * <pre>{@code
 * {
 *   "content":       [ ... ],
 *   "pageNumber":    0,
 *   "pageSize":      10,
 *   "totalElements": 42,
 *   "totalPages":    5,
 *   "last":          false
 * }
 * }</pre>
 *
 * @param <T> the element type of the content list
 * @author FinTrack Team
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /** The list of items for the current page. */
    private List<T> content;

    /** Zero-based index of the current page. */
    private int pageNumber;

    /** Number of items requested per page. */
    private int pageSize;

    /** Total number of items across all pages. */
    private long totalElements;

    /** Total number of pages available. */
    private int totalPages;

    /** {@code true} if this is the last page. */
    private boolean last;

    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a {@link PageResponse} from a Spring Data {@link Page} object.
     *
     * @param page the Spring Data page containing content and metadata
     * @param <T>  element type
     * @return a fully populated {@link PageResponse}
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}


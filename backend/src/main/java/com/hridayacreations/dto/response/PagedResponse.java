package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Pagination-aware payload wrapper used as the {@code data} of an {@link ApiResponse} for list
 * endpoints. Decouples the API contract from Spring Data's {@code Page} implementation.
 *
 * @param <T> element type
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PagedResponse", description = "Paginated collection payload")
public class PagedResponse<T> {

    @Schema(description = "Items on the current page")
    private List<T> content;

    @Schema(description = "Zero-based page index", example = "0")
    private int page;

    @Schema(description = "Page size", example = "10")
    private int size;

    @Schema(description = "Total number of matching elements", example = "57")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "6")
    private int totalPages;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether the page has no elements", example = "false")
    private boolean empty;

    /**
     * Builds a {@link PagedResponse} from a Spring Data {@link Page} whose elements are already the
     * desired response type.
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Builds a {@link PagedResponse} from a {@link Page} of entities, mapping each element to a
     * response DTO via {@code mapper}.
     */
    public static <E, T> PagedResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return PagedResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .build();
    }
}

package com.hridayacreations.util;

import com.hridayacreations.constants.AppConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

/**
 * Builds a safe {@link Pageable} from raw request parameters, clamping the page size and defaulting
 * sort fields to protect against abusive or malformed pagination input.
 */
public final class PageableBuilder {

    private PageableBuilder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Pageable build(int page, int size, String sortBy, String sortDirection) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE)
                : Math.min(size, AppConstants.MAX_PAGE_SIZE);
        String sortField = StringUtils.hasText(sortBy) ? sortBy : AppConstants.DEFAULT_SORT_BY;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, sortField));
    }
}

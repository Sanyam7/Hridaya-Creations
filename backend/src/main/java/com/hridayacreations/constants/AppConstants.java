package com.hridayacreations.constants;

/**
 * Application-wide constant values for pagination, sorting and common defaults.
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    /* Pagination defaults */
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    /* System auditor used before authentication context exists (e.g. seeding) */
    public static final String SYSTEM_AUDITOR = "SYSTEM";

    /* Review constraints */
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
}

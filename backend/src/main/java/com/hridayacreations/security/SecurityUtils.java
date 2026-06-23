package com.hridayacreations.security;

import com.hridayacreations.constants.SecurityConstants;
import com.hridayacreations.exception.UnauthorizedException;
import com.hridayacreations.security.services.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Convenience helpers for reading the currently authenticated principal from the security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @return the authenticated {@link UserPrincipal}
     * @throws UnauthorizedException if no authenticated user is present
     */
    public static UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("No authenticated user found in the security context");
        }
        return principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentPrincipal().getEmail();
    }

    public static boolean isAdmin() {
        return getCurrentPrincipal().getAuthorities().stream()
                .anyMatch(authority -> SecurityConstants.AUTHORITY_ADMIN.equals(authority.getAuthority()));
    }
}

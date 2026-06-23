package com.hridayacreations.config;

import com.hridayacreations.constants.AppConstants;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Supplies the current principal (email) for JPA auditing of {@code createdBy}/{@code updatedBy}.
 * Falls back to {@link AppConstants#SYSTEM_AUDITOR} for unauthenticated contexts such as data
 * seeding or self-registration.
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(AppConstants.SYSTEM_AUDITOR);
        }
        return Optional.of(authentication.getName());
    }
}

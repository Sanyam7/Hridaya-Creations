package com.hridayacreations.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing (population of {@code createdAt}/{@code updatedAt}/
 * {@code createdBy}/{@code updatedBy}). Kept separate from the main application class so that
 * web-layer slice tests ({@code @WebMvcTest}) do not attempt to bootstrap the JPA infrastructure.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaAuditingConfig {
}

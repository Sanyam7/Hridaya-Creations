package com.hridayacreations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Hridaya Creations backend application.
 *
 * <p>Hridaya Creations is a personalized gifting e-commerce platform. This service exposes a
 * secured REST API for customer self-service (catalog browsing, cart, orders, reviews) and an
 * administrative surface for catalog, inventory, pricing and order management.</p>
 *
 * <p>JPA auditing is enabled in {@link com.hridayacreations.config.JpaAuditingConfig} (kept off this
 * class so web-layer slice tests do not bootstrap the persistence layer).</p>
 */
@EnableAsync
@SpringBootApplication
public class HridayaCreationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HridayaCreationsApplication.class, args);
    }
}

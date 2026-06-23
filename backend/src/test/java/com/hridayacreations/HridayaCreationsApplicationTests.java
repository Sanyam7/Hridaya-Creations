package com.hridayacreations;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that boots the full Spring application context (security, JPA, MapStruct mappers,
 * Cloudinary client, seeding) against an in-memory H2 database to verify wiring.
 */
@SpringBootTest
@ActiveProfiles("test")
class HridayaCreationsApplicationTests {

    @Test
    void contextLoads() {
        // Fails the build if any bean cannot be created or the schema cannot be generated.
    }
}

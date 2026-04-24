package com.app.ChromaDress;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("python.service.url", () -> "http://localhost:5000");
        registry.add("app.upload.dir", () -> "uploads/test/");
        registry.add("app.upload.base-url", () -> "http://localhost:8080/uploads/test/");
        registry.add("jwt.secret", () -> "QuestaEUnaChiaveSegretaMoltoLungaPerSuperareIControlliDiSicurezza2026!");
    }
}
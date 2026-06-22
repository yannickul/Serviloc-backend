package com.serviloc.categories;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
class ServiceCategoriesApplicationTests {

    @Test
    void contextLoads() {
        // Vérifie que le contexte démarre avec Postgres + Redis via Testcontainers
    }
}

package lab1.integration;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Базовий клас для інтеграційних тестів.
 *
 * @DataJpaTest піднімає лише частину контексту: JPA, репозиторії, Liquibase і DataSource
 * (без контролерів і веб-шару). Кожен тест виконується у транзакції, яка відкочується
 * після тесту, тому тести не впливають один на одного.
 *
 * Контейнер MySQL запускається один раз на всі тестові класи (singleton container).
 * Liquibase при старті контексту застосовує до нього всі міграції 001–009.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractMySqlIntegrationTest {

    // Користувач root потрібен через preConditions "runningAs: root" у міграціях 001–006
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("store_db")
            .withUsername("root")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}

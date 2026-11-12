# Cosmo Cats Intergalactic Marketplace — Lab 1

Spring Boot backend: API contract (OpenAPI), RFC 9457 error handling, DDD domain model,
CRUD for `Product` on in-memory mock data, pagination, Jakarta Bean Validation
with custom `@CosmicWordCheck`, RestClient integration with a WireMock stub.

## Run

```bash
docker compose up -d      # WireMock delivery stub on :8081 (bonus)
./gradlew bootRun         # API on :8080
```

- API contract: `src/main/resources/api-specs/product-api-spec.yml`
- Products: `GET/POST /api/v1/products`, `GET/PUT/DELETE /api/v1/products/{id}`
- Delivery (3rd party via RestClient): `GET /api/v1/delivery/{orderId}` (try `101` and `999`)

## Tests (Lab 2)

```bash
./gradlew clean build jacocoTestReport   # tests + coverage check (min 80% lines)
```

- Unit tests for every `ProductService` method, controller validation tests (positive and negative),
  RFC 9457 error handler tests, domain model tests.
- Bonus: `DeliveryClientWireMockTest` stubs the 3rd-party delivery service with WireMock.
- Coverage report: `build/reports/jacoco/test/html/index.html`
- CI: `.github/workflows/build.yml` runs on every pull request.

## Database (Lab 3)

MySQL 8 + Liquibase + Spring Data JPA.

```bash
export DB_PASSWORD=<your local MySQL root password>   # also DB_URL / DB_USERNAME if needed
./gradlew bootRun
```

- Schema (3NF): `Category`, `Product`, `Orders`, `OrderItem`, `Cart`, `CartItem` —
  `src/main/resources/db/changelog/` (versioned changesets 001–009).
  Ids: `AUTO_INCREMENT` (MySQL equivalent of a sequence), FK + CHECK constraints.
- Unique: product name within a category (`uq_product_name_category`), order natural id (`uq_Natural_Id`).
- Index: `idx_productName` on `Product.ProductName`.
- `@Transactional` services for Product, Category, Order; natural id `Order.naturalOrderId` (UUID).
- Projections + JPQL: `GET /api/v1/reports/top-products?limit=5` (best sellers, GROUP BY / ORDER BY),
  `GET /api/v1/reports/categories/{name}/in-stock` (WHERE / ORDER BY).
- Orders: `POST /api/v1/orders`, `GET /api/v1/orders/{naturalOrderId}`.
- Integration tests: `@DataJpaTest` + Testcontainers MySQL (`src/test/java/lab1/integration`), Docker required.

## Kafka (Lab 4.2)

```bash
docker compose up -d                 # Kafka (:29092), Schema Registry (:8087), WireMock (:8081)
./gradlew bootRun
./scripts/send-test-order.sh         # creates products and an order -> event order-placed
```

- `POST /api/v1/orders` with `{"items":[{"productId":1,"quantity":2}],"catsNames":["Luna","Comet"]}`
  saves the order (transaction), then `OrderProducer.sendOrder` publishes **order-placed** (Avro, key = natural id).
- Avro schema: `src/main/avro/OrderPlacedEvent.avsc`, registered in Schema Registry by `KafkaAvroSerializer`.
- `OrderConsumer.consumeOrder` (`@KafkaListener`) stores the event in `ConsumedOrders` via Spring Data JPA;
  redelivery of the same order is skipped (idempotent consumer, unique natural id).
- Tests: `@EmbeddedKafka` + mock Schema Registry (`src/test/java/lab1/kafka`).

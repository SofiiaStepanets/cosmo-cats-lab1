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

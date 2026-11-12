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

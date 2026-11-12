#!/bin/bash
# Створює два товари через API, потім замовлення.
# Перед запуском: docker compose up -d, застосунок запущено,
# категорію 'Cosmic Food' додано (scripts/test-data.sql).

BASE=http://localhost:8080/api/v1

echo "== Товар 1 =="
P1=$(curl -s -X POST $BASE/products -H "Content-Type: application/json" \
  -d '{"name":"Star Tuna","description":"Tuna from the Milky Way","price":25.50,"category":"Cosmic Food","inStock":true}')
echo "$P1"
ID1=$(echo "$P1" | sed -E 's/.*"id":([0-9]+).*/\1/')

echo "== Товар 2 =="
P2=$(curl -s -X POST $BASE/products -H "Content-Type: application/json" \
  -d '{"name":"Galaxy Milk","description":"Milk from the Andromeda farm","price":12.00,"category":"Cosmic Food","inStock":true}')
echo "$P2"
ID2=$(echo "$P2" | sed -E 's/.*"id":([0-9]+).*/\1/')

echo "== Замовлення (товари $ID1 x2, $ID2 x1) =="
curl -s -X POST $BASE/orders -H "Content-Type: application/json" \
  -d "{\"items\":[{\"productId\":$ID1,\"quantity\":2},{\"productId\":$ID2,\"quantity\":1}],\"catsNames\":[\"Luna\",\"Comet\"]}"
echo

echo "== Схеми в Schema Registry =="
curl -s http://localhost:8087/subjects
echo

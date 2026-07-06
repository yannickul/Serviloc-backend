#!/bin/bash

BASE_URL="http://localhost:8087/api/categories"
TOKEN="super-secret-token"

echo "=== 1. Liste des catégories ==="
curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" "$BASE_URL" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 2. Création d'une catégorie ==="
CREATE_RESPONSE=$(curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" -X POST "$BASE_URL" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"name":"Informatique","slug":"informatique"}')

echo "$CREATE_RESPONSE"

CATEGORY_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d: -f2)
echo "Catégorie créée avec ID=$CATEGORY_ID"
echo -e "\n"

echo "=== 3. Récupération de la catégorie (id=$CATEGORY_ID) ==="
curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" "$BASE_URL/$CATEGORY_ID" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 4. Suppression de la catégorie (id=$CATEGORY_ID) ==="
curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" -X DELETE "$BASE_URL/$CATEGORY_ID" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 5. Healthcheck interne ==="
curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" "http://localhost:8087/actuator/health"
echo -e "\n"

echo "=== Fin du test complet ==="

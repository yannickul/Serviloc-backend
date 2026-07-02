#!/bin/bash

BASE_URL="http://localhost:8088/api/files"
TOKEN="super-secret-token"
FILE="test.txt"

echo "=== 1. Upload du fichier ==="
curl -X POST "$BASE_URL/upload" \
     -H "Authorization: Bearer $TOKEN" \
     -F "file=@$FILE"
echo -e "\n"

echo "=== 2. Liste des fichiers ==="
curl "$BASE_URL/list?page=0&size=10" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 3. Métadonnées du fichier (id=1) ==="
curl "$BASE_URL/1" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 4. Téléchargement du fichier (id=1) ==="
curl -O -J "$BASE_URL/1/download" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 5. Suppression du fichier (id=1) ==="
curl -X DELETE "$BASE_URL/1" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 6. Healthcheck interne ==="
curl "$BASE_URL/internal/health" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== Fin du test complet ==="

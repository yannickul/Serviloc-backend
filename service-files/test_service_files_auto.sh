#!/bin/bash

BASE_URL="http://localhost:8088/api/files"
TOKEN="super-secret-token"
FILE="test.txt"

echo "=== 1. Upload du fichier ==="
UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/upload" \
     -H "Authorization: Bearer $TOKEN" \
     -F "file=@$FILE")

echo "$UPLOAD_RESPONSE"

# Extraire l'ID du fichier uploadé
FILE_ID=$(echo "$UPLOAD_RESPONSE" | grep -o '"id":[0-9]*' | cut -d: -f2)

echo "Fichier uploadé avec ID=$FILE_ID"
echo -e "\n"

echo "=== 2. Liste des fichiers ==="
curl -s "$BASE_URL/list?page=0&size=10" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 3. Métadonnées du fichier (id=$FILE_ID) ==="
curl -s "$BASE_URL/$FILE_ID" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 4. Téléchargement du fichier (id=$FILE_ID) ==="
curl -s -O -J "$BASE_URL/$FILE_ID/download" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 5. Suppression du fichier (id=$FILE_ID) ==="
curl -s -X DELETE "$BASE_URL/$FILE_ID" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== 6. Healthcheck interne ==="
curl -s "$BASE_URL/internal/health" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== Fin du test complet ==="

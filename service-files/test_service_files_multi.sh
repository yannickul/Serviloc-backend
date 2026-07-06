#!/bin/bash

BASE_URL="http://localhost:8088/api/files"
TOKEN="super-secret-token"
FILES=("test1.txt" "test2.txt" "test3.txt")

for FILE in "${FILES[@]}"; do
  echo "=== Upload du fichier $FILE ==="
  UPLOAD_RESPONSE=$(curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" -X POST "$BASE_URL/upload" \
       -H "Authorization: Bearer $TOKEN" \
       -F "file=@$FILE")

  echo "$UPLOAD_RESPONSE"

  FILE_ID=$(echo "$UPLOAD_RESPONSE" | grep -o '"id":[0-9]*' | cut -d: -f2)
  echo "Fichier $FILE uploadé avec ID=$FILE_ID"
  echo -e "\n"

  echo "=== Liste des fichiers ==="
  curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" "$BASE_URL/list?page=0&size=10" \
       -H "Authorization: Bearer $TOKEN"
  echo -e "\n"

  echo "=== Métadonnées du fichier (id=$FILE_ID) ==="
  curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" "$BASE_URL/$FILE_ID" \
       -H "Authorization: Bearer $TOKEN"
  echo -e "\n"

  echo "=== Téléchargement du fichier (id=$FILE_ID) ==="
  curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" -O -J "$BASE_URL/$FILE_ID/download" \
       -H "Authorization: Bearer $TOKEN"
  echo -e "\n"

  echo "=== Suppression du fichier (id=$FILE_ID) ==="
  curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" -X DELETE "$BASE_URL/$FILE_ID" \
       -H "Authorization: Bearer $TOKEN"
  echo -e "\n"
done

echo "=== Healthcheck interne ==="
curl -s -w "\nHTTP %{http_code} | Temps %{time_total}s\n" "$BASE_URL/internal/health" \
     -H "Authorization: Bearer $TOKEN"
echo -e "\n"

echo "=== Fin du test complet ==="

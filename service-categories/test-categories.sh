#!/bin/bash

BASE_URL="http://localhost:8087/categories"

echo "🔹 Test 1 : Lister toutes les catégories"
curl -s $BASE_URL | jq .

echo "🔹 Test 2 : Créer une nouvelle catégorie"
curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"id":null,"label":"Transport","iconKey":"car","color":"blue","demandCount":10,"percentageShare":15.5}' | jq .

echo "🔹 Test 3 : Lister après création"
curl -s $BASE_URL | jq .

echo "🔹 Test 4 : Récupérer la catégorie ID=1"
curl -s $BASE_URL/1 | jq .

echo "🔹 Test 5 : Mettre à jour la catégorie ID=1"
curl -s -X PUT $BASE_URL/1 \
  -H "Content-Type: application/json" \
  -d '{"id":1,"label":"Transport","iconKey":"bus","color":"green","demandCount":20,"percentageShare":25.0}' | jq .

echo "🔹 Test 6 : Supprimer la catégorie ID=1"
curl -s -X DELETE $BASE_URL/1

echo "🔹 Test 7 : Lister après suppression"
curl -s $BASE_URL | jq .

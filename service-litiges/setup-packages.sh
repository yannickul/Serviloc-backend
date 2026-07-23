#!/bin/bash
# =============================================================
# setup-packages.sh
# Crée toute la structure de packages DDD de service-litiges
# Usage : chmod +x setup-packages.sh && ./setup-packages.sh
# =============================================================

set -e

BASE="src/main/java/com/serviloc/litiges"
TEST_BASE="src/test/java/com/serviloc/litiges"

echo "📦 Création de la structure DDD pour service-litiges..."

# ── DOMAIN ──────────────────────────────────────────────────
mkdir -p "$BASE/domain/model"
mkdir -p "$BASE/domain/repository"
mkdir -p "$BASE/domain/event"
mkdir -p "$BASE/domain/exception"

# ── APPLICATION ─────────────────────────────────────────────
mkdir -p "$BASE/application/port/in"
mkdir -p "$BASE/application/port/out"
mkdir -p "$BASE/application/service"
mkdir -p "$BASE/application/dto/request"
mkdir -p "$BASE/application/dto/response"

# ── INFRASTRUCTURE ───────────────────────────────────────────
mkdir -p "$BASE/infrastructure/persistence"
mkdir -p "$BASE/infrastructure/messaging"
mkdir -p "$BASE/infrastructure/external"
mkdir -p "$BASE/infrastructure/external/dto"
mkdir -p "$BASE/infrastructure/config"

# ── ADAPTER ─────────────────────────────────────────────────
mkdir -p "$BASE/adapter/rest"

# ── RESSOURCES ──────────────────────────────────────────────
mkdir -p "src/main/resources"

# ── TESTS ────────────────────────────────────────────────────
mkdir -p "$TEST_BASE/domain"
mkdir -p "$TEST_BASE/application/service"
mkdir -p "$TEST_BASE/infrastructure"

# ── FICHIERS .gitkeep pour que Git tracke les dossiers vides ─
find "$BASE" -type d -empty -exec touch {}/.gitkeep \;
find "$TEST_BASE" -type d -empty -exec touch {}/.gitkeep \;

echo ""
echo "✅ Structure créée :"
find "$BASE" -type d | sort | sed 's|src/main/java/com/serviloc/litiges/||'

echo ""
echo "📋 Prochaine étape : copier les fichiers du Sprint 0 dans chaque package."
echo "   Lance ensuite : ./run-local.sh"
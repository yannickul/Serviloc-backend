# service-categories

Référentiel des catégories de services de ServiLoc — voir `API_CONTRACT.md` §4.16, §6, §8
et `ARCHITECTURE_MICROSERVICES_SERVILOC.md` §3.7.

Architecture : **Clean Architecture / Hexagonal** (domain / application / infrastructure / adapter).

```
com.serviloc.categories
├── domain/            # Aucune dépendance Spring. Entité riche ServiceCategory, CategoryId (VO), IconKey.
├── application/        # Cas d'usage, DTOs, CategoryService (transactions + cache).
├── infrastructure/      # JPA, Redis, RabbitMQ, sécurité interne.
└── adapter/rest/        # Contrôleurs REST (client / admin / internal), gestion d'erreurs.
```

## Prérequis

- Java 21, Maven 3.9+
- PostgreSQL, Redis, RabbitMQ (voir docker-compose global du projet)
- Eureka Server démarré avant ce service

## Lancer en local

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Le service attend :
- PostgreSQL sur `localhost:5447` (db `db_categories`)
- Redis sur `localhost:6379`
- RabbitMQ sur `localhost:5672`
- Eureka sur `localhost:8761`

Adapter ces valeurs dans `application-local.yml` si besoin.

## Lancer les tests

```bash
mvn test
```

## Endpoints

| Méthode | Path | Rôle | Cache |
|---|---|---|---|
| GET | `/client/categories` | client | Redis, TTL 1h |
| GET | `/admin/categories` | admin | — |
| POST | `/admin/categories` | admin | invalide le cache client |
| PUT | `/admin/categories/{id}` | admin | invalide le cache client |
| DELETE | `/admin/categories/{id}` | admin | invalide le cache client |
| GET | `/internal/categories` | Feign (Service Missions) | — |
| GET | `/internal/categories/{id}` | Feign (Service Missions, Utilisateurs) | — |
| GET | `/internal/categories/label/{label}` | Feign (Service Utilisateurs) | — |
| GET | `/internal/categories/stats` | Feign (Service Missions) | — |
| PUT | `/internal/categories/{id}/increment` | Feign / rejeu manuel | — |

Les endpoints `/internal/**` exigent le header `X-Internal-Token` (voir `serviloc.internal-token`
dans `application.yml`, à synchroniser avec les autres microservices).

## Événement RabbitMQ consommé

`demand.published` (exchange topic `serviloc.events`) → incrémente `demandCount` de la catégorie
concernée. Payload attendu : `{ demandId, location, categoryId, clientId }`.

Queue : `categories.demand.queue` (avec dead-letter queue `categories.demand.queue.dlq`).

## Cache Redis

- Nom du cache : `categories`
- Clé : `client-list`
- TTL : 1h (configuré globalement dans `application.yml` / `CacheConfig`)
- Invalidation : sur `POST`, `PUT`, `DELETE` de `/admin/categories/**`
- **Non invalidé** sur incrémentation de `demandCount` (staleness acceptée jusqu'à 1h,
  donnée statistique non critique)

## Données seed

6 catégories chargées au démarrage via `data.sql` (idempotent, `INSERT ... WHERE NOT EXISTS`) :
Plomberie, Électricité, Ménage, Serrurerie, Peinture, Jardinage.

## Vérifications à faire une fois le service démarré

```bash
# Eureka : le service doit apparaître comme SERVICE-CATEGORIES
curl http://localhost:8761/eureka/apps/SERVICE-CATEGORIES

# Seed
curl http://localhost:8087/client/categories

# Swagger UI
open http://localhost:8087/swagger-ui.html

# Cache Redis (après un premier GET /client/categories)
redis-cli KEYS "categories::*"
redis-cli TTL "categories::client-list"

# Invalidation cache sur update
curl -X PUT http://localhost:8087/admin/categories/cat_plomberie \
  -H "Content-Type: application/json" \
  -d '{"label":"Plomberie","iconKey":"wrench","color":"#c7d2fe"}'
redis-cli KEYS "categories::*"   # doit être vide juste après
```

## À intégrer côté équipe

- Ajouter `svc-categories` au docker-compose global (port 8087, DB port 5447, dépend de
  `eureka-server`, `svc-categories-db`, `redis`, `rabbitmq`).
- Configurer `serviloc.internal-token` identique dans tous les microservices appelants
  (`FeignInternalTokenConfig`, voir ARCHITECTURE §4.4).
- Service Missions doit publier `demand.published` avec un `categoryId` correspondant à un
  `cat_*` existant (sinon le message est loggé en warning et acquitté, pas de retry infini).

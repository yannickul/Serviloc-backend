# API Contract — Endpoints Internes `service-categories`

> Endpoints réservés aux appels inter-services (Feign). Jamais exposés par le Gateway.
> Authentification : header `X-Internal-Token` (valeur partagée, voir configuration
> `serviloc.internal-token` de chaque microservice). Toute requête sans ce header, ou avec
> une valeur incorrecte, reçoit `403 Forbidden`.

**Base URL interne** : `http://service-categories:8087` (nom du service dans le réseau Docker /
Eureka : `SERVICE-CATEGORIES`)

**Header requis sur tous les endpoints ci-dessous**

| Header | Valeur | Obligatoire |
|---|---|---|
| `X-Internal-Token` | Token machine partagé entre microservices | Oui |

**Format de réponse** — identique au contrat public (§3 de `API_CONTRACT.md`) :

Succès :
```json
{ "success": true, "data": { ... }, "meta": null }
```

Erreur :
```json
{ "success": false, "error": { "code": "NOT_FOUND", "message": "...", "field": "categoryId" } }
```

---

## Schéma `ServiceCategory`

```json
{
  "id": "cat_plomberie",
  "label": "Plomberie",
  "iconKey": "wrench",
  "description": "Réparation de fuites, installation sanitaire, dépannage plomberie",
  "color": "#dbeafe",
  "budgetRange": { "min": 5000, "max": 50000 },
  "demandCount": 47,
  "percentageShare": 34.0
}
```

| Champ | Type | Description |
|---|---|---|
| `id` | string | Identifiant stable, format `cat_<slug-du-label>` |
| `label` | string | Nom affiché de la catégorie |
| `iconKey` | string | `wrench` \| `bolt` \| `broom` \| `key` \| `brush` \| `plus` \| `leaf` |
| `description` | string | Description courte de la catégorie |
| `color` | string | Code couleur hexadécimal (ex: `#dbeafe`) |
| `budgetRange` | object | Fourchette budgétaire indicative en FCFA `{min, max}` |
| `demandCount` | number | Nombre cumulé de demandes publiées dans cette catégorie |
| `percentageShare` | number | Part de `demandCount` sur le total toutes catégories confondues (%, arrondi à 1 décimale) |

> ⚠️ Cette forme complète (avec `demandCount`/`percentageShare`) est celle des endpoints
> `/internal/**` ci-dessous. `GET /client/categories` (public, hors périmètre de ce document)
> expose une forme réduite sans ces deux champs statistiques — voir `README.md`.

---

### `GET /internal/categories`

Liste toutes les catégories, **sans cache** (toujours lu depuis PostgreSQL).

**Consommé par** : Service Missions

**Response 200**
```json
{
  "success": true,
  "data": [
    { "id": "cat_plomberie", "label": "Plomberie", "iconKey": "wrench", "description": "Réparation de fuites, installation sanitaire, dépannage plomberie", "color": "#dbeafe", "budgetRange": { "min": 5000, "max": 50000 }, "demandCount": 47, "percentageShare": 34.0 },
    { "id": "cat_electricite", "label": "Électricité", "iconKey": "bolt", "description": "Installation électrique, dépannage, mise aux normes", "color": "#fef9c3", "budgetRange": { "min": 5000, "max": 60000 }, "demandCount": 12, "percentageShare": 8.7 }
  ],
  "meta": null
}
```

---

### `GET /internal/categories/{id}`

Détail d'une catégorie par son identifiant.

**Consommé par** : Service Missions, Service Utilisateurs

**Paramètres de chemin**

| Paramètre | Type | Description |
|---|---|---|
| `id` | string | Identifiant de la catégorie (ex: `cat_plomberie`) |

**Response 200**
```json
{
  "success": true,
  "data": { "id": "cat_plomberie", "label": "Plomberie", "iconKey": "wrench", "description": "Réparation de fuites, installation sanitaire, dépannage plomberie", "color": "#dbeafe", "budgetRange": { "min": 5000, "max": 50000 }, "demandCount": 47, "percentageShare": 34.0 },
  "meta": null
}
```

**Response 404**
```json
{ "success": false, "error": { "code": "NOT_FOUND", "message": "Catégorie introuvable : cat_inconnue", "field": "categoryId" } }
```

---

### `GET /internal/categories/label/{label}`

Recherche une catégorie par son label (insensible à la casse).

**Consommé par** : Service Utilisateurs (validation prestataire lors de l'inscription/mise à jour de compétences)

**Paramètres de chemin**

| Paramètre | Type | Description |
|---|---|---|
| `label` | string | Label recherché (ex: `Plomberie`, comparaison insensible à la casse) |

**Response 200** — identique à `GET /internal/categories/{id}`

**Response 404** — identique, `field: "categoryId"`

---

### `GET /internal/categories/stats`

Statistiques par catégorie (`demandCount`, `percentageShare`). Structurellement identique à
`GET /internal/categories` — endpoint séparé pour un usage sémantique distinct côté appelant
(tableau de bord admin de Service Missions).

**Consommé par** : Service Missions (admin/stats)

**Response 200** — même forme que `GET /internal/categories`

---

### `PUT /internal/categories/{id}/increment`

Incrémente `demandCount` de la catégorie de 1 et recalcule implicitement `percentageShare`
(calculé à la volée à chaque lecture, jamais stocké).

**Déclenché normalement par** : le consumer RabbitMQ interne de `service-categories`
lui-même, sur l'événement `demand.published` (exchange `serviloc.events`, routing key
`demand.published`). Cet endpoint HTTP est conservé pour rejouer manuellement un
incrément (tests d'intégration, rattrapage après incident) — Service Missions n'a normalement
pas besoin de l'appeler directement puisqu'il publie déjà l'événement.

**Paramètres de chemin**

| Paramètre | Type | Description |
|---|---|---|
| `id` | string | Identifiant de la catégorie à incrémenter |

**Response 200**
```json
{
  "success": true,
  "data": { "categoryId": "cat_plomberie", "demandCount": 48 },
  "meta": null
}
```

**Response 404** — catégorie inconnue, `field: "categoryId"`

**Note sur l'idempotence** : chaque appel incrémente d'une unité ; un double appel (ex.
double publication du même événement) incrémente deux fois. Comportement accepté :
`demandCount`/`percentageShare` sont des indicateurs statistiques, pas une donnée
métier critique.

---

## Codes d'erreur

| Code | HTTP | Contexte |
|---|---|---|
| `ACCESS_DENIED` | 403 | `X-Internal-Token` manquant ou invalide |
| `NOT_FOUND` | 404 | Catégorie inexistante (par id ou label) |
| `VALIDATION_ERROR` | 400 | Paramètre de chemin malformé |
| `INTERNAL_ERROR` | 500 | Erreur non gérée côté service-categories |

---

## Événement RabbitMQ consommé (rappel, non-HTTP)

| Événement | Exchange | Routing key | Effet |
|---|---|---|---|
| `demand.published` | `serviloc.events` (topic) | `demand.published` | Incrémente `demandCount` de `payload.categoryId` |

Payload attendu (enveloppe standard ServiLoc) :
```json
{
  "eventId": "...",
  "eventType": "demand.published",
  "occurredAt": "...",
  "payload": { "demandId": "...", "location": { ... }, "categoryId": "cat_plomberie", "clientId": "..." }
}
```

Si `categoryId` ne correspond à aucune catégorie existante, le message est loggé en
warning et acquitté (pas de retry infini, pas de blocage de la queue).

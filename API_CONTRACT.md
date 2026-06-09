# API_CONTRACT.md — ServiLoc

> **Document fondateur — à maintenir par les deux équipes (frontend + backend)**
> Toute modification d'un endpoint, d'un format de réponse ou d'un schéma doit être notifiée 48h à l'avance avec un diff explicite dans ce fichier.
> Dernière mise à jour : Juin 2026 · Version 2.0

**Changelog v2.0 :**
- Ajout section 9 — Endpoints Agent Service Client (UC30-agent, UC31-agent, UC35–UC38)
- Ajout section 6.x — Chat/Conversations (UC5, UC16, UC22)
- Ajout `GET /client/providers/search` (UC13, UC15)
- Ajout `POST /client/missions/:id/litige` prestataire symétrique (UC12)
- Ajout `POST /provider/missions/:id/rate` (UC26)
- Ajout `PATCH /provider/profile` (UC18)
- Ajout `GET /admin/stats` (UC33)
- Ajout `GET|POST /admin/agents` et endpoints de gestion agents
- Mise à jour schémas : `Litige`, `ProviderReview`, `AgentProfile`, `Conversation`, `Message`
- Mise à jour JWT : `role` = `"agent"` (remplace `"service_client"`)

---

## Sommaire

1. [Configuration globale](#1-configuration-globale)
2. [Authentification](#2-authentification)
3. [Format des réponses](#3-format-des-réponses)
4. [Schémas des objets métier](#4-schémas-des-objets-métier)
5. [Endpoints — Auth](#5-endpoints--auth)
6. [Endpoints — Client](#6-endpoints--client)
7. [Endpoints — Prestataire](#7-endpoints--prestataire)
8. [Endpoints — Admin](#8-endpoints--admin)
9. [Endpoints — Agent Service Client](#9-endpoints--agent-service-client)
10. [Upload de fichiers](#10-upload-de-fichiers)
11. [Calendrier de livraison backend](#11-calendrier-de-livraison-backend)
12. [Données mock frontend (fallback)](#12-données-mock-frontend-fallback)
13. [Règles de coordination](#13-règles-de-coordination)

---

## 1. Configuration globale

```
URL de base           : https://api.serviloc.cm/v1
Content-Type          : application/json
Charset               : UTF-8
Devise                : XAF (Franc CFA — valeurs entières, pas de décimales)
Fuseau horaire        : Africa/Douala (UTC+1)
Format date           : ISO 8601 — "2026-05-21T09:32:00+01:00"
Format date courte    : "YYYY-MM-DD"
Langue des messages   : Français (fr-CM)
Pagination            : ?page=1&limit=20 (défaut: limit=20)
```

---

## 2. Authentification

### 2.1 Mécanisme

ServiLoc utilise **JWT Bearer Token** avec refresh token.

```
Authorization: Bearer <access_token>
```

| Token | Durée de validité | Stockage frontend |
|-------|-------------------|-------------------|
| `access_token` | 1 heure | `localStorage` (clé : `serviloc_access`) |
| `refresh_token` | 30 jours | `localStorage` (clé : `serviloc_refresh`) |

### 2.2 Payload JWT décodé

```json
{
  "sub": "usr_abc123",
  "role": "client",
  "phone": "+237695000000",
  "iat": 1748736000,
  "exp": 1748739600
}
```

| Champ | Valeurs possibles | Usage frontend |
|-------|-------------------|----------------|
| `sub` | UUID utilisateur | ID courant |
| `role` | `"client"` \| `"provider"` \| `"admin"` \| `"agent"` | Routing et affichage conditionnel |

> ⚠️ **v2.0** : `"service_client"` est remplacé par `"agent"` dans toute la codebase.

### 2.3 Rafraîchissement automatique

Le frontend appelle `/auth/refresh` automatiquement quand l'API retourne `401 UNAUTHORIZED`. Si le refresh échoue, l'utilisateur est redirigé vers `/login`.

```javascript
// src/services/authService.ts — logique de rafraîchissement
const response = await fetch('/v1/auth/refresh', {
  method: 'POST',
  body: JSON.stringify({ refreshToken: localStorage.getItem('serviloc_refresh') })
});
```

### 2.4 Rôles et accès

| Rôle | Espaces accessibles | Espace interdit |
|------|--------------------|-----------------| 
| `client` | `/client/**`, `/auth/**` | Tout le reste |
| `provider` | `/provider/**`, `/auth/**` | Tout le reste |
| `admin` | `/admin/**`, `/auth/**` | Tout le reste |
| `agent` | `/agent/**`, `/auth/**` | Tout le reste |

> Les préfixes sont contrôlés par le Gateway. Un token `role=agent` ne peut jamais atteindre `/admin/**`.

---

## 3. Format des réponses

### 3.1 Succès

```json
{
  "success": true,
  "data": { },
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 248,
    "totalPages": 13
  }
}
```

> `meta` est présent uniquement sur les endpoints paginés. Il est absent des réponses d'entité unique.

### 3.2 Erreur

**Format unique pour TOUTES les erreurs, tous les endpoints.**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_OTP",
    "message": "Code OTP erroné — 2 tentatives restantes avant blocage temporaire",
    "field": "otpCode"
  }
}
```

| Champ | Type | Présence | Description |
|-------|------|----------|-------------|
| `code` | `string` | Toujours | Code machine lisible côté frontend |
| `message` | `string` | Toujours | Message en français pour l'affichage |
| `field` | `string` | Optionnel | Champ concerné (validation de formulaire) |

### 3.3 Codes d'erreur standardisés

| Code HTTP | `error.code` | Signification |
|-----------|-------------|---------------|
| 400 | `VALIDATION_ERROR` | Données de formulaire invalides |
| 400 | `INVALID_OTP` | Code OTP incorrect |
| 400 | `OTP_EXPIRED` | Code OTP expiré (renvoyer) |
| 400 | `OTP_MAX_ATTEMPTS` | Trop de tentatives — compte bloqué |
| 401 | `UNAUTHORIZED` | Token absent ou invalide |
| 401 | `TOKEN_EXPIRED` | Access token expiré |
| 401 | `INVALID_CREDENTIALS` | Email/mot de passe incorrects |
| 403 | `FORBIDDEN` | Rôle insuffisant ou ressource non autorisée |
| 404 | `NOT_FOUND` | Ressource introuvable |
| 409 | `ALREADY_EXISTS` | Conflit (ex: email déjà utilisé) |
| 409 | `ALREADY_VALIDATED` | Ressource déjà dans cet état (ex: devis déjà accepté) |
| 422 | `UNPROCESSABLE` | Logique métier impossible (ex: mission déjà démarrée) |
| 429 | `RATE_LIMITED` | Trop de requêtes |
| 503 | `SERVICE_UNAVAILABLE` | Microservice downstream indisponible |
| 500 | `SERVER_ERROR` | Erreur serveur — contacter le backend |

---

## 4. Schémas des objets métier

> Ces schémas définissent la structure **exacte** des objets retournés par l'API. Le frontend ne doit pas inférer des champs non listés ici.

### 4.1 `User` (commun à tous les rôles)

```json
{
  "id": "usr_abc123",
  "role": "client",
  "firstName": "Madeleine",
  "lastName": "Kamdem",
  "fullName": "Madeleine Kamdem",
  "phone": "+237695123456",
  "email": "mk@email.cm",
  "avatarInitial": "M",
  "createdAt": "2026-03-15T10:00:00+01:00",
  "status": "active"
}
```

> `status` : `"active"` | `"suspended"` | `"pending_verification"`
> `role` : `"client"` | `"provider"` | `"admin"` | `"agent"`

### 4.2 `ClientProfile` (extension de User)

```json
{
  "id": "usr_abc123",
  "role": "client",
  "firstName": "Madeleine",
  "lastName": "Kamdem",
  "fullName": "Madeleine Kamdem",
  "phone": "+237695123456",
  "email": "mk@email.cm",
  "avatarInitial": "M",
  "status": "active",
  "totalSpent": 55000,
  "completedMissions": 3,
  "pendingPayment": {
    "amount": 25000,
    "missionLabel": "Mission Plomberie en cours"
  },
  "location": {
    "city": "Bafoussam",
    "district": "Quartier Commercial"
  },
  "createdAt": "2026-03-15T10:00:00+01:00"
}
```

### 4.3 `ProviderProfile` (extension de User)

```json
{
  "id": "usr_jcm456",
  "role": "provider",
  "firstName": "Jean-Claude",
  "lastName": "Mbarga",
  "fullName": "Jean-Claude Mbarga",
  "phone": "+237699234567",
  "email": "jcm@email.cm",
  "avatarInitial": "J",
  "status": "active",
  "specialty": "Plomberie",
  "rating": 4.8,
  "completedMissions": 47,
  "isAvailable": true,
  "hourlyRate": 4000,
  "serviceZone": {
    "city": "Bafoussam",
    "radiusKm": 20
  },
  "availability": {
    "monday":    { "start": "08:00", "end": "18:00", "available": true },
    "tuesday":   { "start": "08:00", "end": "18:00", "available": true },
    "wednesday": { "start": "08:00", "end": "18:00", "available": true },
    "thursday":  { "start": "08:00", "end": "18:00", "available": true },
    "friday":    { "start": "08:00", "end": "18:00", "available": true },
    "saturday":  { "start": "08:00", "end": "13:00", "available": true },
    "sunday":    { "start": null,    "end": null,    "available": false }
  },
  "monthlyEarnings": 185000,
  "certifications": ["Artisan certifié"],
  "estCertifie": true,
  "createdAt": "2025-11-10T08:00:00+01:00"
}
```

### 4.4 `AgentProfile` (nouveau — v2.0)

```json
{
  "id": "usr_agent01",
  "role": "agent",
  "firstName": "Pauline",
  "lastName": "Fotso",
  "fullName": "Pauline Fotso",
  "email": "p.fotso@serviloc.cm",
  "phone": "+237691000111",
  "avatarInitial": "P",
  "status": "active",
  "agentCode": "AGT-007",
  "department": "Service Client",
  "assignedLitigesCount": 4,
  "createdAt": "2026-01-10T08:00:00+01:00"
}
```

### 4.5 `ServiceDemand` (demande de service)

```json
{
  "id": "dem_xyz789",
  "clientId": "usr_abc123",
  "category": {
    "id": "cat_plomberie",
    "label": "Plomberie",
    "iconKey": "wrench"
  },
  "description": "Fuite sous l'évier de la cuisine, eau qui s'écoule en permanence",
  "photos": [
    {
      "id": "photo_001",
      "url": "https://cdn.serviloc.cm/demands/photo_001.jpg",
      "name": "photo_sous_evier.jpg"
    }
  ],
  "location": {
    "address": "Bafoussam, Quartier Commercial",
    "lat": 5.4764,
    "lng": 10.4207
  },
  "status": "en_cours",
  "isUrgent": false,
  "estimatedBudget": {
    "min": 20000,
    "max": 30000
  },
  "providerId": "usr_jcm456",
  "providerName": "Jean-Claude M.",
  "quoteId": "quote_001",
  "missionId": "msn_001",
  "createdAt": "2026-05-21T08:00:00+01:00",
  "updatedAt": "2026-05-21T09:32:00+01:00"
}
```

> `status` : `"ouverte"` | `"en_cours"` | `"terminee"` | `"annulee"` | `"litige"`

### 4.6 `Quote` (devis)

```json
{
  "id": "quote_001",
  "demandId": "dem_xyz789",
  "providerId": "usr_jcm456",
  "clientId": "usr_abc123",
  "reference": "DEV-2026-001",
  "status": "en_attente",
  "laborDescription": "Remplacement joint + siphon évier cuisine",
  "laborAmount": 15000,
  "materials": [
    {
      "id": "mat_001",
      "designation": "Joint silicone cuisine",
      "quantity": 2,
      "unitPrice": 1500,
      "subtotal": 3000
    },
    {
      "id": "mat_002",
      "designation": "Siphon PVC universel",
      "quantity": 1,
      "unitPrice": 4500,
      "subtotal": 4500
    },
    {
      "id": "mat_003",
      "designation": "Visserie + colliers",
      "quantity": 1,
      "unitPrice": 500,
      "subtotal": 500
    }
  ],
  "materialsTotal": 8000,
  "totalAmount": 23000,
  "estimatedDurationHours": 2,
  "validityDays": 5,
  "createdAt": "2026-05-20T14:00:00+01:00",
  "expiresAt": "2026-05-25T14:00:00+01:00"
}
```

> `status` : `"en_attente"` | `"accepte"` | `"refuse"` | `"expire"`

### 4.7 `Mission`

```json
{
  "id": "msn_001",
  "demandId": "dem_xyz789",
  "quoteId": "quote_001",
  "clientId": "usr_abc123",
  "providerId": "usr_jcm456",
  "category": "Plomberie",
  "title": "Fuite cuisine — Madeleine K.",
  "status": "en_cours",
  "totalAmount": 23000,
  "sequesteredAmount": 23000,
  "paymentStatus": "sequestre",
  "startedAt": "2026-05-21T09:32:00+01:00",
  "estimatedDurationHours": 2,
  "completedAt": null,
  "steps": [
    { "id": "step_001", "label": "Coupure eau principale vérifiée",        "completed": true,  "order": 1 },
    { "id": "step_002", "label": "Démontage siphon fissuré",               "completed": true,  "order": 2 },
    { "id": "step_003", "label": "Remplacement joint silicone ×2",         "completed": true,  "order": 3 },
    { "id": "step_004", "label": "Installation siphon PVC neuf",           "completed": true,  "order": 4 },
    { "id": "step_005", "label": "Test d'étanchéité (5 min eau courante)", "completed": false, "order": 5 },
    { "id": "step_006", "label": "Nettoyage zone d'intervention",          "completed": false, "order": 6 }
  ],
  "providerLocation": {
    "lat": 5.4764,
    "lng": 10.4207,
    "label": "Jean-Claude est arrivé",
    "sublabel": "Quartier Commercial, Bafoussam"
  },
  "location": {
    "address": "Bafoussam, Quartier Commercial",
    "lat": 5.4764,
    "lng": 10.4207
  }
}
```

> `status` : `"en_attente"` | `"en_cours"` | `"terminee"` | `"litige"`
> `paymentStatus` : `"sequestre"` | `"libere"` | `"litige"` | `"rembourse"`

### 4.8 `Conversation` (nouveau — v2.0)

```json
{
  "id": "conv_001",
  "demandId": "dem_xyz789",
  "client": {
    "id": "usr_abc123",
    "fullName": "Madeleine Kamdem",
    "avatarInitial": "M"
  },
  "provider": {
    "id": "usr_jcm456",
    "fullName": "Jean-Claude Mbarga",
    "avatarInitial": "J"
  },
  "lastMessagePreview": "D'accord, je peux intervenir demain matin.",
  "lastMessageAt": "2026-05-21T09:00:00+01:00",
  "unreadCount": 2,
  "hasQuote": true,
  "quoteStatus": "en_attente",
  "createdAt": "2026-05-20T13:00:00+01:00"
}
```

### 4.9 `Message` (nouveau — v2.0)

```json
{
  "id": "msg_001",
  "conversationId": "conv_001",
  "senderId": "usr_jcm456",
  "senderRole": "provider",
  "content": "Bonjour, je suis disponible demain à 8h.",
  "imageUrl": null,
  "sentAt": "2026-05-20T14:30:00+01:00",
  "read": true
}
```

> `senderRole` : `"client"` | `"provider"` | `"agent"`

### 4.10 `Litige` (mis à jour — v2.0)

```json
{
  "id": "lit_042",
  "reference": "LIT-2026-0042",
  "demandId": "dem_xyz789",
  "missionId": "msn_001",
  "transactionId": "txn_892",
  "clientId": "usr_abc123",
  "providerId": "usr_jcm456",
  "agentId": "usr_agent01",
  "motif": {
    "id": "motif_incomplete",
    "title": "Prestation incomplète",
    "description": "Les travaux prévus n'ont pas été entièrement réalisés"
  },
  "description": "Le plombier n'a pas remplacé le siphon comme prévu dans le devis.",
  "evidences": [
    {
      "id": "ev_001",
      "url": "https://cdn.serviloc.cm/litiges/photo_sous_evier.jpg",
      "name": "photo_sous_evier.jpg"
    }
  ],
  "amount": 23000,
  "status": "en_traitement",
  "resolution": null,
  "timeline": [
    { "event": "Litige ouvert",      "at": "2026-05-21T11:00:00+01:00" },
    { "event": "Assigné à l'agent", "at": "2026-05-21T11:30:00+01:00" }
  ],
  "createdAt": "2026-05-21T11:00:00+01:00",
  "assignedAt": "2026-05-21T11:30:00+01:00",
  "resolvedAt": null
}
```

> `status` : `"ouvert"` | `"assigne"` | `"en_traitement"` | `"resolu"` | `"cloture"`
> `resolution` : `null` | `"remboursement_partiel"` | `"remboursement_total"` | `"en_faveur_prestataire"`

### 4.11 `Resolution` (nouveau — v2.0)

```json
{
  "id": "res_001",
  "litigeId": "lit_042",
  "agentId": "usr_agent01",
  "type": "remboursement_partiel",
  "refundAmount": 11500,
  "note": "Remboursement de 50% accordé — prestation partiellement réalisée.",
  "clientAccepted": true,
  "providerAccepted": false,
  "proposedAt": "2026-05-22T10:00:00+01:00",
  "closedAt": null
}
```

> `type` : `"remboursement_partiel"` | `"remboursement_total"` | `"en_faveur_prestataire"`

### 4.12 `LitigeMessage` (nouveau — v2.0)

```json
{
  "id": "lmsg_001",
  "litigeId": "lit_042",
  "senderId": "usr_agent01",
  "senderRole": "agent",
  "senderName": "Pauline F.",
  "content": "Bonjour, j'ai bien pris en charge votre dossier. Pouvez-vous m'envoyer des photos supplémentaires ?",
  "attachmentUrl": null,
  "sentAt": "2026-05-21T14:00:00+01:00"
}
```

> `senderRole` : `"agent"` | `"client"` | `"provider"`

### 4.13 `ProviderReview` (nouveau — v2.0)

```json
{
  "id": "rev_001",
  "providerId": "usr_jcm456",
  "agentId": "usr_agent01",
  "agentName": "Pauline F.",
  "verdict": "approved",
  "comment": "Dossier complet. Tous les justificatifs sont valides. Casier judiciaire récent.",
  "reviewedAt": "2026-05-22T09:00:00+01:00",
  "finalDecision": null,
  "decidedAt": null,
  "decidedBy": null
}
```

> `verdict` : `"approved"` | `"rejected"` | `"needs_revision"`
> `finalDecision` : `null` (en attente) | `"validated"` | `"rejected"`

### 4.14 `Transaction`

```json
{
  "id": "txn_892",
  "reference": "#T-892",
  "demandId": "dem_xyz789",
  "missionId": "msn_001",
  "clientId": "usr_abc123",
  "clientName": "Madeleine K.",
  "providerId": "usr_jcm456",
  "providerName": "Jean-Claude M.",
  "category": "Plomberie",
  "amount": 25000,
  "commission": 2000,
  "providerPayout": 23000,
  "paymentMethod": "orange_money",
  "status": "sequestre",
  "createdAt": "2026-05-21T08:45:00+01:00"
}
```

> `paymentMethod` : `"orange_money"` | `"mtn_momo"`
> `status` : `"sequestre"` | `"libere"` | `"litige"` | `"rembourse"`

### 4.15 `ManagedUser` (vue admin)

```json
{
  "id": "usr_abc123",
  "fullName": "Madeleine Kamdem",
  "email": "mk@email.cm",
  "phone": "+237695123",
  "role": "client",
  "avatarInitial": "M",
  "missionsCount": 3,
  "rating": 4.2,
  "status": "active",
  "suspendedBy": null,
  "suspendedByRole": null,
  "suspensionReason": null,
  "createdAt": "2026-02-10T00:00:00+01:00"
}
```

> `role` : `"client"` | `"provider"`
> `status` : `"active"` | `"suspended"`
> Quand `status = "suspended"` : `suspendedBy`, `suspendedByRole` (`"admin"` | `"agent"`), `suspensionReason` sont renseignés.

### 4.16 `ServiceCategory`

```json
{
  "id": "cat_plomberie",
  "label": "Plomberie",
  "iconKey": "wrench",
  "color": "#dbeafe",
  "demandCount": 47,
  "percentageShare": 34
}
```

> `iconKey` : `"wrench"` | `"bolt"` | `"broom"` | `"key"` | `"brush"` | `"plus"`

### 4.17 `ProviderSearchResult` (nouveau — v2.0)

```json
{
  "id": "usr_jcm456",
  "fullName": "Jean-Claude Mbarga",
  "avatarInitial": "J",
  "specialty": "Plomberie",
  "rating": 4.8,
  "hourlyRate": 4000,
  "distanceKm": 0.8,
  "isAvailable": true,
  "completedMissions": 47,
  "serviceZone": {
    "city": "Bafoussam",
    "radiusKm": 20
  }
}
```

---

## 5. Endpoints — Auth

> Ces endpoints sont accessibles sans token. `POST /auth/login` accepte tous les rôles.

### `POST /auth/register`

Inscription d'un nouvel utilisateur (client ou prestataire uniquement — les agents sont créés par l'admin).

**Request body**

```json
{
  "role": "client",
  "firstName": "Madeleine",
  "lastName": "Kamdem",
  "phone": "+237695123456",
  "email": "mk@email.cm",
  "password": "motdepasse123"
}
```

> `role` : `"client"` | `"provider"` uniquement. Les rôles `"admin"` et `"agent"` ne sont pas auto-inscriptibles.

**Response 201**

```json
{
  "success": true,
  "data": {
    "userId": "usr_abc123",
    "phone": "+237695123456",
    "otpSent": true,
    "message": "Un code SMS a été envoyé au +237 695 XXX XXX"
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 409 | `ALREADY_EXISTS` | Email ou téléphone déjà enregistré |
| 400 | `VALIDATION_ERROR` | Champ manquant ou format invalide |
| 400 | `INVALID_ROLE` | Rôle non autorisé à l'auto-inscription |

---

### `POST /auth/verify-otp`

Vérification du code OTP reçu par SMS.

**Request body**

```json
{
  "userId": "usr_abc123",
  "otpCode": "4782"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "user": { "...ClientProfile ou ProviderProfile selon le rôle" }
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `INVALID_OTP` | Code incorrect — `error` contient `attemptsRemaining` |
| 400 | `OTP_EXPIRED` | Code expiré |
| 400 | `OTP_MAX_ATTEMPTS` | Compte bloqué temporairement |

---

### `POST /auth/resend-otp`

Renvoi du code OTP (soumis après expiration du countdown de 45s).

**Request body** : `{ "userId": "usr_abc123" }`

**Response 200**

```json
{
  "success": true,
  "data": {
    "otpSent": true,
    "cooldownSeconds": 45
  }
}
```

---

### `POST /auth/login`

Connexion pour tous les rôles (Client, Prestataire, Admin, Agent).

**Request body**

```json
{
  "email": "admin@serviloc.cm",
  "password": "••••••••"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "user": { "...User selon le rôle" }
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 401 | `INVALID_CREDENTIALS` | Email/mot de passe incorrects — `error` contient `attemptsRemaining` |
| 401 | `ACCOUNT_BLOCKED` | Compte bloqué après 5 tentatives |
| 403 | `ACCOUNT_SUSPENDED` | Compte suspendu — `error` contient `reason` |

---

### `POST /auth/refresh`

Rafraîchissement du token (appelé automatiquement par le frontend).

**Request body** : `{ "refreshToken": "eyJhbGci..." }`

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

---

### `POST /auth/logout`

Révocation du refresh token.

**Request body** : `{ "refreshToken": "..." }`

**Response 200** : `{ "success": true }`

---

## 6. Endpoints — Client

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "client"`.

### `GET /client/me`

Profil complet du client connecté.

**Response 200** → `{ "data": { ...ClientProfile } }`

---

### `GET /client/dashboard`

Données agrégées du tableau de bord client.

**Response 200**

```json
{
  "success": true,
  "data": {
    "profile": { "...ClientProfile" },
    "recentDemands": [ "...ServiceDemand[]" ],
    "financialSummary": {
      "totalSpent": 55000,
      "completedMissions": 3,
      "pendingPayment": {
        "amount": 25000,
        "missionLabel": "Mission Plomberie en cours"
      }
    },
    "unreadMessages": 2
  }
}
```

---

### `GET /client/demands`

Liste paginée de toutes les demandes du client.

**Query params** : `?page=1&limit=20&status=en_cours`

**Response 200** → `{ "data": ServiceDemand[], "meta": { pagination } }`

---

### `POST /client/demands`

Création d'une nouvelle demande de service.

**Request body**

```json
{
  "categoryId": "cat_plomberie",
  "description": "Fuite sous l'évier de la cuisine",
  "photoIds": ["photo_001"],
  "location": {
    "address": "Bafoussam, Quartier Commercial",
    "lat": 5.4764,
    "lng": 10.4207
  },
  "isUrgent": false,
  "estimatedBudget": {
    "min": 20000,
    "max": 30000
  }
}
```

**Response 201** → `{ "data": { ...ServiceDemand } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | Description vide ou champ manquant |
| 404 | `NOT_FOUND` | `categoryId` inexistante |

---

### `GET /client/demands/:demandId`

Détail d'une demande.

**Response 200** → `{ "data": { ...ServiceDemand } }`

---

### `GET /client/demands/:demandId/quote`

Devis associé à une demande.

**Response 200** → `{ "data": { ...Quote } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 404 | `NOT_FOUND` | Aucun devis pour cette demande |

---

### `POST /client/demands/:demandId/quote/accept`

Acceptation d'un devis et déclenchement du paiement.

**Request body**

```json
{
  "paymentMethod": "orange_money",
  "phoneNumber": "+237695123456"
}
```

> `paymentMethod` : `"orange_money"` | `"mtn_momo"`

**Response 200**

```json
{
  "success": true,
  "data": {
    "quoteId": "quote_001",
    "missionId": "msn_001",
    "paymentStatus": "sequestre",
    "message": "Paiement séquestré. La mission peut démarrer."
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 409 | `ALREADY_VALIDATED` | Devis déjà accepté ou expiré |
| 422 | `PAYMENT_FAILED` | Solde insuffisant ou API Mobile Money refus |
| 503 | `SERVICE_UNAVAILABLE` | Service Paiement indisponible |

---

### `POST /client/demands/:demandId/quote/reject`

Refus d'un devis.

**Response 200** → `{ "data": { "quoteId": "quote_001", "status": "refuse" } }`

---

### `GET /client/missions/:missionId`

Suivi d'une mission en cours (steps, localisation prestataire, montant séquestré).

**Response 200** → `{ "data": { ...Mission } }`

---

### `POST /client/missions/:missionId/validate`

Validation de fin de mission par le client.

**Response 200**

```json
{
  "success": true,
  "data": {
    "missionId": "msn_001",
    "validatedBy": "client",
    "bothValidated": true,
    "paymentStatus": "libere",
    "releasedAmount": 23000
  }
}
```

> Si `bothValidated: false`, le paiement n'est pas encore libéré — en attente de la validation prestataire.

---

### `POST /client/missions/:missionId/rate`

Notation du prestataire après mission terminée.

**Request body**

```json
{
  "rating": 5,
  "criteria": {
    "punctuality": "tres_ponctuel",
    "quality":     "excellent",
    "cleanliness": "tres_propre"
  },
  "comment": "Excellent travail, très professionnel."
}
```

> `rating` : entier entre 1 et 5 inclus.

**Response 201**

```json
{
  "success": true,
  "data": {
    "ratingId": "rat_001",
    "targetId": "usr_jcm456",
    "targetRole": "provider",
    "rating": 5
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | Note hors limites (1-5) |
| 409 | `ALREADY_EXISTS` | Mission déjà évaluée par ce client (délai dépassé) |

---

### `POST /client/missions/:missionId/litige`

Signalement d'un litige sur une mission.

**Request body**

```json
{
  "motifId": "motif_incomplete",
  "description": "Le plombier n'a pas remplacé le siphon comme prévu.",
  "evidenceIds": ["photo_001"]
}
```

**Response 201** → `{ "data": { ...Litige } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 404 | `NOT_FOUND` | Mission introuvable |
| 409 | `ALREADY_EXISTS` | Litige déjà ouvert pour cette mission |

---

### `PATCH /client/litiges/:litigeId/resolution/accept`

Le client accepte la proposition de résolution faite par l'agent.

**Response 200**

```json
{
  "success": true,
  "data": {
    "litigeId": "lit_042",
    "clientAccepted": true,
    "providerAccepted": false,
    "status": "en_traitement"
  }
}
```

---

### `PATCH /client/litiges/:litigeId/resolution/reject`

Le client refuse la proposition de résolution — l'agent peut en proposer une nouvelle.

**Request body** : `{ "reason": "Le remboursement proposé est insuffisant." }`

**Response 200** → `{ "data": { "litigeId": "lit_042", "status": "en_traitement" } }`

---

### `GET /client/conversations`

Liste des conversations du client connecté, triées par dernier message.

**Query params** : `?page=1&limit=20`

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": "conv_001",
      "demandId": "dem_xyz789",
      "provider": {
        "id": "usr_jcm456",
        "fullName": "Jean-Claude Mbarga",
        "avatarInitial": "J"
      },
      "lastMessagePreview": "D'accord, je peux intervenir demain matin.",
      "lastMessageAt": "2026-05-21T09:00:00+01:00",
      "unreadCount": 2,
      "hasQuote": true,
      "quoteStatus": "en_attente"
    }
  ],
  "meta": { "page": 1, "limit": 20, "total": 3, "totalPages": 1 }
}
```

---

### `POST /client/conversations`

Ouvrir une conversation avec un prestataire (flux urgence UC16 — sans demande préalable).

**Request body**

```json
{
  "providerId": "usr_jcm456",
  "demandId": "dem_xyz789"
}
```

> `demandId` est optionnel. S'il est absent, la conversation est libre (non rattachée à une demande).
> Si une conversation existe déjà pour ce couple `(clientId, providerId, demandId)`, l'existante est retournée (idempotent).

**Response 201**

```json
{
  "success": true,
  "data": { "...Conversation" }
}
```

---

### `GET /client/conversations/:conversationId/messages`

Messages d'une conversation, du plus récent au plus ancien.

**Query params** : `?page=1&limit=30`

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": "msg_001",
      "senderId": "usr_jcm456",
      "senderRole": "provider",
      "content": "Bonjour, je suis disponible demain à 8h.",
      "imageUrl": null,
      "sentAt": "2026-05-20T14:30:00+01:00",
      "read": true
    }
  ],
  "meta": { "page": 1, "limit": 30, "total": 12, "totalPages": 1 }
}
```

---

### `POST /client/conversations/:conversationId/messages`

Envoyer un message dans une conversation.

**Request body**

```json
{
  "content": "Bonjour, êtes-vous disponible demain matin ?",
  "imageId": null
}
```

> `imageId` : optionnel — ID retourné par `POST /uploads/photos`.

**Response 201** → `{ "data": { ...Message } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | Contenu vide et pas d'image |
| 403 | `FORBIDDEN` | Le client n'est pas partie de cette conversation |

---

### `GET /client/providers/search`

Recherche de prestataires disponibles par service et géolocalisation (flux urgence UC13/UC15).

**Query params**

| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `specialty` | string | Oui | Ex: `"Plomberie"` |
| `lat` | float | Oui | Latitude client |
| `lng` | float | Oui | Longitude client |
| `radiusKm` | int | Non (défaut: 15) | Rayon de recherche |
| `minRating` | float | Non | Note minimale ex: `4.0` |
| `maxRate` | int | Non | Tarif horaire max en XAF |
| `page` | int | Non (défaut: 1) | Pagination |
| `limit` | int | Non (défaut: 20) | Pagination |

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": "usr_jcm456",
      "fullName": "Jean-Claude Mbarga",
      "avatarInitial": "J",
      "specialty": "Plomberie",
      "rating": 4.8,
      "hourlyRate": 4000,
      "distanceKm": 0.8,
      "isAvailable": true,
      "completedMissions": 47,
      "serviceZone": { "city": "Bafoussam", "radiusKm": 20 }
    }
  ],
  "meta": { "page": 1, "limit": 20, "total": 6, "totalPages": 1 }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | `specialty`, `lat` ou `lng` manquants |
| 503 | `SERVICE_UNAVAILABLE` | Service Utilisateurs indisponible — résultat du cache si disponible |

---

### `GET /client/categories`

Liste des catégories de services disponibles.

**Response 200** → `{ "data": ServiceCategory[] }`

---

## 7. Endpoints — Prestataire

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "provider"`.

### `GET /provider/me`

Profil complet du prestataire connecté.

**Response 200** → `{ "data": { ...ProviderProfile } }`

---

### `PATCH /provider/profile`

Mise à jour du profil professionnel complet (UC18).

**Request body**

```json
{
  "specialty": "Plomberie",
  "hourlyRate": 4000,
  "serviceZone": {
    "city": "Bafoussam",
    "radiusKm": 20
  },
  "certifications": ["Artisan certifié"],
  "documentIds": ["doc_001", "doc_002"]
}
```

> Tous les champs sont optionnels — seuls les champs envoyés sont mis à jour.
> `documentIds` : IDs retournés par `POST /uploads/documents`. Ce champ remplace la liste existante.

**Response 200** → `{ "data": { ...ProviderProfile } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | `hourlyRate` négatif, `radiusKm` invalide |
| 404 | `NOT_FOUND` | Un `documentId` est introuvable |

---

### `GET /provider/dashboard`

Données agrégées du tableau de bord prestataire.

**Response 200**

```json
{
  "success": true,
  "data": {
    "profile": { "...ProviderProfile" },
    "metrics": {
      "missionsThisMonth": 18,
      "netEarnings": 185000,
      "averageRating": 4.8,
      "availableDemandsCount": 11,
      "trends": {
        "missions": { "value": "+3",   "direction": "up", "subtext": "+3 vs avril" },
        "earnings": { "value": "+22%", "direction": "up", "subtext": "+22%" },
        "rating":   { "value": "+0.1", "direction": "up", "subtext": "+0.1" }
      }
    },
    "recentMissions": [ "...Mission[]" ],
    "availability": { "...scheduleObject" }
  }
}
```

---

### `PATCH /provider/availability`

Mise à jour du statut de disponibilité en temps réel (toggle).

**Request body** : `{ "isAvailable": true }`

**Response 200** → `{ "data": { "isAvailable": true } }`

---

### `PATCH /provider/schedule`

Mise à jour du planning hebdomadaire.

**Request body**

```json
{
  "schedule": {
    "monday":   { "start": "08:00", "end": "18:00", "available": true },
    "saturday": { "start": "08:00", "end": "13:00", "available": true },
    "sunday":   { "start": null,    "end": null,    "available": false }
  }
}
```

**Response 200** → `{ "data": { "schedule": { "...updatedSchedule" } } }`

---

### `GET /provider/demands/available`

Liste des demandes disponibles correspondant aux compétences et à la zone du prestataire.

**Query params** : `?page=1&limit=20&zone=priority&category=cat_plomberie`

> `zone` : `"priority"` (défaut) | `"extended"`

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": "dem_xyz789",
      "category": { "id": "cat_plomberie", "label": "Plomberie", "iconKey": "wrench" },
      "description": "Fuite sous l'évier de la cuisine",
      "isUrgent": true,
      "status": "ouverte",
      "estimatedBudget": { "min": 20000, "max": 30000 },
      "distanceKm": 0.8,
      "clientRating": 4.2,
      "postedMinutesAgo": 5,
      "location": { "address": "Bafoussam, Quartier Commercial" }
    }
  ],
  "meta": { "page": 1, "limit": 20, "total": 11, "totalPages": 1 }
}
```

---

### `POST /provider/demands/:demandId/apply`

Postuler à une demande.

**Response 201**

```json
{
  "success": true,
  "data": {
    "demandId": "dem_xyz789",
    "status": "applied",
    "message": "Vous pouvez maintenant créer votre devis."
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 409 | `ALREADY_EXISTS` | Demande déjà attribuée à un autre prestataire |

---

### `POST /provider/demands/:demandId/quote`

Création d'un devis pour une demande.

**Request body**

```json
{
  "laborDescription": "Remplacement joint + siphon évier cuisine",
  "laborAmount": 15000,
  "materials": [
    { "designation": "Joint silicone cuisine", "quantity": 2, "unitPrice": 1500 },
    { "designation": "Siphon PVC universel",   "quantity": 1, "unitPrice": 4500 },
    { "designation": "Visserie + colliers",    "quantity": 1, "unitPrice": 500  }
  ],
  "estimatedDurationHours": 2,
  "validityDays": 5
}
```

**Response 201** → `{ "data": { ...Quote } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | Prix négatif ou champ manquant |
| 404 | `NOT_FOUND` | Demande introuvable ou déjà attribuée |

---

### `GET /provider/missions`

Liste des missions du prestataire.

**Query params** : `?status=en_cours&page=1`

**Response 200** → `{ "data": Mission[], "meta": { pagination } }`

---

### `GET /provider/missions/:missionId`

Détail d'une mission.

**Response 200** → `{ "data": { ...Mission } }`

---

### `POST /provider/missions/:missionId/start`

Démarrage d'une mission.

**Response 200**

```json
{
  "success": true,
  "data": {
    "missionId": "msn_001",
    "status": "en_cours",
    "startedAt": "2026-05-21T09:32:00+01:00"
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 422 | `UNPROCESSABLE` | Paiement non encore confirmé |

---

### `PATCH /provider/missions/:missionId/steps/:stepId`

Mise à jour d'une étape de mission.

**Request body** : `{ "completed": true }`

**Response 200** → `{ "data": { "stepId": "step_001", "completed": true, "missionProgress": 67 } }`

---

### `POST /provider/missions/:missionId/complete`

Déclaration de fin de mission par le prestataire.

**Response 200**

```json
{
  "success": true,
  "data": {
    "missionId": "msn_001",
    "validatedBy": "provider",
    "bothValidated": false,
    "status": "terminee",
    "message": "En attente de la validation client."
  }
}
```

---

### `POST /provider/missions/:missionId/rate`

Notation du client après mission terminée (UC26).

**Request body**

```json
{
  "rating": 4,
  "comment": "Client ponctuel et bien communicatif."
}
```

> `rating` : entier entre 1 et 5 inclus.

**Response 201**

```json
{
  "success": true,
  "data": {
    "ratingId": "rat_002",
    "targetId": "usr_abc123",
    "targetRole": "client",
    "rating": 4
  }
}
```

---

### `POST /provider/missions/:missionId/litige`

Signalement d'un litige par le prestataire (UC12 — symétrique au client).

**Request body**

```json
{
  "motifId": "motif_non_paiement",
  "description": "Le client refuse de valider malgré une prestation correctement réalisée.",
  "evidenceIds": ["photo_002"]
}
```

**Response 201** → `{ "data": { ...Litige } }`

---

### `PATCH /provider/litiges/:litigeId/resolution/accept`

Le prestataire accepte la proposition de résolution.

**Response 200**

```json
{
  "success": true,
  "data": {
    "litigeId": "lit_042",
    "clientAccepted": true,
    "providerAccepted": true,
    "status": "resolu",
    "message": "Les deux parties ont accepté. L'agent peut clôturer."
  }
}
```

---

### `PATCH /provider/litiges/:litigeId/resolution/reject`

Le prestataire refuse la proposition de résolution.

**Request body** : `{ "reason": "Je réclame le paiement total de la prestation." }`

**Response 200** → `{ "data": { "litigeId": "lit_042", "status": "en_traitement" } }`

---

### `GET /provider/conversations`

Liste des conversations du prestataire connecté.

**Query params** : `?page=1&limit=20`

**Response 200** — même structure que `GET /client/conversations` avec `client` à la place de `provider`.

---

### `GET /provider/conversations/:conversationId/messages`

Messages d'une conversation.

**Query params** : `?page=1&limit=30`

**Response 200** — même structure que `GET /client/conversations/:id/messages`.

---

### `POST /provider/conversations/:conversationId/messages`

Envoyer un message dans une conversation.

**Request body** : `{ "content": "...", "imageId": null }`

**Response 201** → `{ "data": { ...Message } }`

---

### `GET /provider/earnings`

Historique des gains et paiements.

**Query params** : `?page=1&month=2026-05`

**Response 200**

```json
{
  "success": true,
  "data": {
    "monthlyTotal": 185000,
    "missions": [ "...Mission[]" ],
    "payouts": [
      {
        "id": "pay_001",
        "amount": 22500,
        "missionId": "msn_001",
        "method": "orange_money",
        "paidAt": "2026-05-21T14:00:00+01:00"
      }
    ]
  },
  "meta": { "page": 1, "limit": 20, "total": 18 }
}
```

---

## 8. Endpoints — Admin

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "admin"`.

### `GET /admin/dashboard`

Tableau de bord administrateur complet.

**Response 200**

```json
{
  "success": true,
  "data": {
    "metrics": {
      "activeDemands":    { "value": 47,      "trend": "+12%" },
      "ongoingMissions":  { "value": 23,      "trend": "+5%"  },
      "monthlyRevenue":   { "value": 8400000, "trend": "+31%" },
      "commissionEarned": { "value": 672000,  "trend": "+31%" }
    },
    "pendingValidations": [ "...ProviderProfile[]" ],
    "activeLitiges":      [ "...Litige[]" ],
    "popularCategories":  [ "...ServiceCategory[]" ],
    "recentTransactions": [ "...Transaction[]" ]
  }
}
```

---

### `GET /admin/stats`

Statistiques complètes avec historique pour graphiques (UC33).

**Query params** : `?from=2026-01-01&to=2026-05-31`

**Response 200**

```json
{
  "success": true,
  "data": {
    "demands": {
      "total": 248,
      "open": 47,
      "inProgress": 23,
      "completed": 168,
      "cancelled": 10
    },
    "missions": {
      "total": 168,
      "completed": 155,
      "inDispute": 8,
      "completionRate": 92.3
    },
    "financials": {
      "totalRevenue": 8400000,
      "commissionEarned": 672000,
      "sequesteredAmount": 575000,
      "periodBreakdown": [
        { "month": "2026-05", "revenue": 2100000, "commission": 168000 },
        { "month": "2026-04", "revenue": 1850000, "commission": 148000 }
      ]
    },
    "topProviders": [
      {
        "id": "usr_jcm456",
        "fullName": "Jean-Claude M.",
        "completedMissions": 47,
        "rating": 4.8
      }
    ],
    "popularCategories": [
      {
        "id": "cat_plomberie",
        "label": "Plomberie",
        "demandCount": 84,
        "percentageShare": 34
      }
    ]
  }
}
```

---

### `GET /admin/providers`

Liste des prestataires avec filtre et pagination.

**Query params** : `?status=pending_verification&page=1`

> `status` : `"active"` | `"pending_verification"` | `"suspended"` | `"rejected"`

**Response 200** → `{ "data": ProviderProfile[], "meta": { pagination } }`

---

### `GET /admin/providers/:providerId`

Dossier complet d'un prestataire avec documents et instruction de l'agent si disponible.

**Response 200**

```json
{
  "success": true,
  "data": {
    "provider": { "...ProviderProfile" },
    "documents": [
      {
        "id": "doc_001",
        "type": "carte_professionnelle",
        "label": "Carte professionnelle",
        "reference": "Artisan électricien N°EL-2023-089",
        "status": "valide",
        "fileUrl": "https://cdn.serviloc.cm/docs/doc_001.pdf"
      },
      {
        "id": "doc_003",
        "type": "casier_judiciaire",
        "label": "Casier judiciaire < 3 mois",
        "reference": null,
        "status": "manquant",
        "fileUrl": null
      }
    ],
    "agentReview": {
      "agentName": "Pauline F.",
      "verdict": "approved",
      "comment": "Dossier complet. Justificatifs valides.",
      "reviewedAt": "2026-05-22T09:00:00+01:00"
    }
  }
}
```

> `document.status` : `"valide"` | `"manquant"` | `"en_verification"` | `"refuse"`
> `agentReview` est `null` si aucun agent n'a encore instruit le dossier.

---

### `POST /admin/providers/:providerId/validate`

Validation finale d'un dossier prestataire (après instruction de l'agent).

**Request body** : `{ "note": "Validé suite à instruction de l'agent." }` (optionnel)

**Response 200** → `{ "data": { "providerId": "...", "status": "active" } }`

---

### `POST /admin/providers/:providerId/reject`

Rejet d'un dossier prestataire avec motif.

**Request body** : `{ "reason": "Casier judiciaire manquant ou invalide." }`

**Response 200** → `{ "data": { "providerId": "...", "status": "rejected" } }`

---

### `POST /admin/providers/:providerId/notify`

Envoi d'un SMS de rappel pour document manquant.

**Response 200** → `{ "data": { "smsSent": true } }`

---

### `GET /admin/users`

Liste paginée de tous les utilisateurs (clients + prestataires).

**Query params** : `?role=client&status=active&search=Madeleine&page=1`

**Response 200** → `{ "data": ManagedUser[], "meta": { pagination } }`

---

### `PATCH /admin/users/:userId/suspend`

Suspension globale d'un compte (UC31 — admin).

**Request body**

```json
{
  "reason": "Comportement frauduleux répété",
  "duration": "7d"
}
```

> `duration` : `"24h"` | `"7d"` | `"indefinite"`

**Response 200** → `{ "data": { "userId": "...", "status": "suspended", "duration": "7d" } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 403 | `FORBIDDEN` | Tentative de suspendre un admin |
| 404 | `NOT_FOUND` | Utilisateur introuvable |

---

### `PATCH /admin/users/:userId/reactivate`

Réactivation d'un compte suspendu.

**Response 200** → `{ "data": { "userId": "...", "status": "active" } }`

---

### `GET /admin/agents`

Liste des agents service client.

**Query params** : `?status=active&page=1`

**Response 200** → `{ "data": AgentProfile[], "meta": { pagination } }`

---

### `POST /admin/agents`

Création d'un compte agent (seul l'admin peut créer des agents).

**Request body**

```json
{
  "firstName": "Pauline",
  "lastName": "Fotso",
  "email": "p.fotso@serviloc.cm",
  "phone": "+237691000111",
  "department": "Service Client"
}
```

> Un mot de passe provisoire est généré automatiquement et envoyé par email à l'agent.

**Response 201** → `{ "data": { ...AgentProfile } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 409 | `ALREADY_EXISTS` | Email déjà utilisé |

---

### `GET /admin/agents/:agentId`

Détail d'un agent avec ses statistiques de traitement.

**Response 200** → `{ "data": { ...AgentProfile } }`

---

### `PATCH /admin/agents/:agentId/suspend`

Suspension d'un compte agent.

**Request body** : `{ "reason": "..." }`

**Response 200** → `{ "data": { "agentId": "...", "status": "suspended" } }`

---

### `DELETE /admin/agents/:agentId`

Suppression définitive d'un compte agent.

**Response 200** → `{ "data": { "agentId": "...", "deleted": true } }`

---

### `GET /admin/litiges`

Liste paginée de tous les litiges avec métriques.

**Query params** : `?status=ouvert&page=1&limit=20`

**Response 200**

```json
{
  "success": true,
  "data": {
    "metrics": {
      "open": 7,
      "inProgress": 4,
      "resolvedThisMonth": 12,
      "totalBlockedAmount": 287000
    },
    "litiges": [ "...Litige[]" ]
  },
  "meta": { "page": 1, "limit": 20, "total": 23 }
}
```

---

### `GET /admin/litiges/:litigeId`

Détail complet d'un litige.

**Response 200**

```json
{
  "success": true,
  "data": {
    "...Litige",
    "client":   { "...ClientProfile" },
    "provider": { "...ProviderProfile" },
    "agent":    { "...AgentProfile ou null" }
  }
}
```

---

### `POST /admin/litiges/:litigeId/assign`

Assignation d'un agent à un litige.

**Request body** : `{ "agentId": "usr_agent01" }`

**Response 200** → `{ "data": { "litigeId": "...", "agentId": "...", "status": "assigne" } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 404 | `NOT_FOUND` | Agent inexistant |
| 409 | `ALREADY_EXISTS` | Litige déjà assigné à cet agent |

---

### `PUT /admin/litiges/:litigeId/assign`

Réassignation du litige à un autre agent.

**Request body** : `{ "agentId": "usr_agent02" }`

**Response 200** → `{ "data": { "litigeId": "...", "agentId": "usr_agent02", "status": "assigne" } }`

---

### `GET /admin/litiges/stats`

Statistiques de traitement des litiges.

**Response 200**

```json
{
  "success": true,
  "data": {
    "totalOpen": 7,
    "totalResolved": 42,
    "averageResolutionDays": 3.2,
    "byAgent": [
      {
        "agentId": "usr_agent01",
        "agentName": "Pauline F.",
        "resolved": 18,
        "pending": 4
      }
    ]
  }
}
```

---

### `GET /admin/transactions`

Liste paginée des transactions.

**Query params** : `?status=sequestre&page=1`

**Response 200** → `{ "data": Transaction[], "meta": { pagination } }`

---

### `PATCH /admin/settings/commission`

Mise à jour des taux de commission.

**Request body**

```json
{
  "standardRate": 8,
  "urgencyRate": 12
}
```

> Les taux sont en pourcentage (%). Valeurs acceptées : entre 0 et 30.

**Response 200** → `{ "data": { "standardRate": 8, "urgencyRate": 12, "updatedAt": "..." } }`

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | Taux hors limites (0–30%) |

---

### `GET /admin/categories`

Liste des catégories avec statistiques.

**Response 200** → `{ "data": ServiceCategory[] }`

---

### `POST /admin/categories`

Création d'une catégorie.

**Request body**

```json
{
  "label": "Jardinage",
  "iconKey": "leaf",
  "color": "#d1fae5"
}
```

**Response 201** → `{ "data": { ...ServiceCategory } }`

---

### `PUT /admin/categories/:categoryId`

Mise à jour d'une catégorie.

**Request body** : même structure que `POST /admin/categories`.

**Response 200** → `{ "data": { ...ServiceCategory } }`

---

### `DELETE /admin/categories/:categoryId`

Suppression d'une catégorie.

**Response 200** → `{ "data": { "categoryId": "...", "deleted": true } }`

---

## 9. Endpoints — Agent Service Client

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "agent"`.

---

### `GET /agent/providers`

Liste des dossiers prestataires à instruire (UC30-agent).

**Query params** : `?status=pending&page=1&limit=20`

> `status` : `"pending"` (en attente d'instruction) | `"reviewed"` (déjà instruit, en attente décision admin)

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": "usr_jcm456",
      "fullName": "Jean-Claude Mbarga",
      "specialty": "Plomberie",
      "status": "pending_verification",
      "documentsComplete": false,
      "missingDocuments": ["casier_judiciaire"],
      "submittedAt": "2026-05-20T08:00:00+01:00",
      "reviewStatus": "pending"
    }
  ],
  "meta": { "page": 1, "limit": 20, "total": 8, "totalPages": 1 }
}
```

---

### `GET /agent/providers/:providerId`

Dossier complet d'un prestataire à instruire.

**Response 200** — même structure que `GET /admin/providers/:providerId` (documents + profil complet).

---

### `POST /agent/providers/:providerId/review`

Dépôt de l'instruction de l'agent sur un dossier prestataire (UC30-agent).

**Request body**

```json
{
  "verdict": "approved",
  "comment": "Dossier complet. Tous les justificatifs sont valides. Casier judiciaire récent (02/2026)."
}
```

> `verdict` : `"approved"` | `"rejected"` | `"needs_revision"`
> `"needs_revision"` → notifie le prestataire de corriger son dossier sans passer par l'admin.

**Response 201**

```json
{
  "success": true,
  "data": {
    "...ProviderReview",
    "message": "Instruction enregistrée. L'administrateur a été notifié pour décision finale."
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 404 | `NOT_FOUND` | Prestataire introuvable |
| 409 | `ALREADY_EXISTS` | Ce prestataire a déjà une décision finale (validé ou rejeté) |

---

### `GET /agent/litiges`

Litiges assignés à l'agent connecté uniquement (UC36).

**Query params** : `?status=en_traitement&page=1&limit=20`

> `status` : `"ouvert"` | `"en_traitement"` | `"resolu"`

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": "lit_042",
      "reference": "LIT-2026-0042",
      "status": "en_traitement",
      "motif": "Travaux non conformes au devis",
      "amount": 45000,
      "client":   { "id": "usr_abc",    "fullName": "Alice Nguetse" },
      "provider": { "id": "usr_jcm456", "fullName": "Jean-Claude M." },
      "createdAt": "2026-05-18T10:00:00+01:00",
      "assignedAt": "2026-05-18T11:30:00+01:00",
      "unrepliedMessages": 1
    }
  ],
  "meta": { "page": 1, "limit": 20, "total": 4, "totalPages": 1 }
}
```

---

### `GET /agent/litiges/:litigeId`

Détail complet d'un litige assigné (UC36).

**Response 200**

```json
{
  "success": true,
  "data": {
    "id": "lit_042",
    "reference": "LIT-2026-0042",
    "status": "en_traitement",
    "motif": "Travaux non conformes au devis",
    "description": "Le client affirme que la plomberie a été mal réparée...",
    "amount": 45000,
    "evidences": [
      { "type": "photo", "url": "https://cdn.serviloc.cm/evidence/photo_001.jpg" }
    ],
    "client":   { "id": "usr_abc",    "fullName": "Alice Nguetse",    "phone": "+237691..." },
    "provider": { "id": "usr_jcm456", "fullName": "Jean-Claude M.",   "phone": "+237677..." },
    "resolution": null,
    "timeline": [
      { "event": "Litige ouvert",      "at": "2026-05-18T10:00:00+01:00" },
      { "event": "Assigné à l'agent", "at": "2026-05-18T11:30:00+01:00" }
    ]
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 403 | `FORBIDDEN` | Ce litige n'est pas assigné à cet agent |

---

### `GET /agent/litiges/:litigeId/history`

Historique du chat entre client et prestataire sur cette demande (UC37).

**Response 200**

```json
{
  "success": true,
  "data": {
    "conversationId": "conv_001",
    "messages": [ "...Message[]" ]
  }
}
```

---

### `GET /agent/litiges/:litigeId/messages`

Échanges entre l'agent et les parties dans le contexte du litige (UC37).

**Query params** : `?page=1&limit=30`

**Response 200** → `{ "data": LitigeMessage[], "meta": { pagination } }`

---

### `POST /agent/litiges/:litigeId/messages`

L'agent envoie un message à l'une des parties (UC37).

**Request body**

```json
{
  "content": "Bonjour, j'ai bien pris en charge votre dossier. Pouvez-vous préciser la date exacte des travaux ?",
  "recipientRole": "client",
  "attachmentId": null
}
```

> `recipientRole` : `"client"` | `"provider"`
> `attachmentId` : optionnel — ID retourné par `POST /uploads/photos`.

**Response 201** → `{ "data": { ...LitigeMessage } }`

---

### `POST /agent/litiges/:litigeId/resolution`

Proposition d'une résolution par l'agent (UC38).

**Request body**

```json
{
  "type": "remboursement_partiel",
  "refundAmount": 11500,
  "note": "Remboursement de 50% accordé — prestation partiellement réalisée selon les preuves."
}
```

> `type` : `"remboursement_partiel"` | `"remboursement_total"` | `"en_faveur_prestataire"`
> Si `type = "en_faveur_prestataire"`, `refundAmount` doit être `0`.

**Response 201**

```json
{
  "success": true,
  "data": {
    "...Resolution",
    "message": "Proposition envoyée aux deux parties pour acceptation."
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | `refundAmount` supérieur au montant du litige |

---

### `PUT /agent/litiges/:litigeId/resolution`

Modification de la proposition si une partie a refusé.

**Request body** — même structure que `POST /agent/litiges/:id/resolution`.

**Response 200** → `{ "data": { ...Resolution } }`

---

### `POST /agent/litiges/:litigeId/close`

Clôture définitive du litige après acceptation des deux parties (UC38).

**Response 200**

```json
{
  "success": true,
  "data": {
    "litigeId": "lit_042",
    "status": "cloture",
    "refundAmount": 11500,
    "message": "Litige clôturé. Remboursement en cours."
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 409 | `UNPROCESSABLE` | Les deux parties n'ont pas encore accepté la résolution |
| 503 | `SERVICE_UNAVAILABLE` | Service Paiement indisponible — la clôture sera retentée |

---

### `POST /agent/litiges/:litigeId/suspend-user`

Suspension contextuelle d'une des parties d'un litige (UC31-agent).

> ⚠️ Le `userId` doit obligatoirement être le `clientId` ou le `providerId` du litige. Toute autre valeur retourne `403 FORBIDDEN`.

**Request body**

```json
{
  "userId": "usr_abc123",
  "reason": "Fraude confirmée lors de l'instruction du litige LIT-2026-0042."
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "userId": "usr_abc123",
    "status": "suspended",
    "duration": "7d",
    "litigeId": "lit_042",
    "message": "Utilisateur suspendu pour 7 jours. L'administrateur a été notifié."
  }
}
```

> La durée de suspension est fixée à **7 jours** pour les agents (non modifiable).
> Seul un admin peut réactiver un compte suspendu par un agent.

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 403 | `FORBIDDEN` | `userId` n'est pas partie de ce litige |
| 404 | `NOT_FOUND` | Litige ou utilisateur introuvable |

---

## 10. Upload de fichiers

> Ces endpoints sont accessibles à tous les rôles authentifiés.

### `POST /uploads/photos`

Upload d'une ou plusieurs photos (demandes, litiges, chat).

**Request** : `multipart/form-data`

```
field: photos   (fichier binaire, max 5 Mo par fichier, formats: jpg, png, webp)
field: context  (string: "demand" | "litige" | "profile" | "chat")
```

**Response 201**

```json
{
  "success": true,
  "data": {
    "uploads": [
      {
        "id": "photo_001",
        "url": "https://cdn.serviloc.cm/uploads/photo_001.jpg",
        "name": "photo_sous_evier.jpg",
        "sizeBytes": 245000
      }
    ]
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `VALIDATION_ERROR` | Format non supporté ou fichier vide |
| 413 | `FILE_TOO_LARGE` | Fichier dépasse 5 Mo |

> Le frontend envoie ce endpoint en premier, reçoit les `id`, puis les inclut dans les appels métier.

---

### `POST /uploads/documents`

Upload d'un document officiel (dossier prestataire).

**Request** : `multipart/form-data`

```
field: document  (PDF ou image, max 10 Mo)
field: type      (string: "carte_professionnelle" | "cni" | "casier_judiciaire" | "assurance")
```

**Response 201** — même format que `POST /uploads/photos`.

---

## 11. Calendrier de livraison backend

> Ce tableau est la référence contractuelle. Le frontend utilise les mocks JSON correspondants tant que l'endpoint n'est pas livré.

| Semaine | Endpoints livrés | Statut |
|---------|-----------------|--------|
| **S1** | `POST /auth/register`, `POST /auth/verify-otp`, `POST /auth/resend-otp`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` | ⬜ À livrer |
| **S1** | `GET /client/categories`, `GET /admin/categories`, `POST /admin/categories`, `PUT /admin/categories/:id`, `DELETE /admin/categories/:id` | ⬜ À livrer |
| **S1** | Eureka Server + API Gateway opérationnels | ⬜ À livrer |
| **S2** | `GET /client/me`, `GET /client/dashboard`, `GET /client/demands`, `POST /client/demands` | ⬜ À livrer |
| **S2** | `GET /provider/me`, `PATCH /provider/profile`, `GET /provider/dashboard`, `PATCH /provider/availability`, `PATCH /provider/schedule`, `GET /provider/demands/available` | ⬜ À livrer |
| **S2** | `GET /admin/dashboard`, `GET /admin/providers`, `GET /admin/providers/:id`, `GET /admin/users` | ⬜ À livrer |
| **S2** | `GET /admin/agents`, `POST /admin/agents`, `GET /admin/agents/:id` | ⬜ À livrer |
| **S2** | `GET /client/providers/search` | ⬜ À livrer |
| **S3** | `GET /client/demands/:id`, `GET /client/demands/:id/quote`, `POST /client/demands/:id/quote/accept`, `POST /client/demands/:id/quote/reject` | ⬜ À livrer |
| **S3** | `GET /client/missions/:id`, `POST /client/missions/:id/validate`, `POST /client/missions/:id/rate`, `POST /client/missions/:id/litige` | ⬜ À livrer |
| **S3** | `POST /provider/demands/:id/apply`, `POST /provider/demands/:id/quote`, `GET /provider/missions`, `GET /provider/missions/:id`, `POST /provider/missions/:id/start`, `PATCH /provider/missions/:id/steps/:stepId`, `POST /provider/missions/:id/complete` | ⬜ À livrer |
| **S3** | `POST /provider/missions/:id/rate`, `POST /provider/missions/:id/litige` | ⬜ À livrer |
| **S3** | `POST /admin/providers/:id/validate`, `POST /admin/providers/:id/reject`, `POST /admin/providers/:id/notify` | ⬜ À livrer |
| **S3** | `PATCH /admin/users/:id/suspend`, `PATCH /admin/users/:id/reactivate` | ⬜ À livrer |
| **S3** | `GET /admin/litiges`, `GET /admin/litiges/:id`, `POST /admin/litiges/:id/assign`, `PUT /admin/litiges/:id/assign`, `GET /admin/litiges/stats` | ⬜ À livrer |
| **S3** | `GET /client/conversations`, `POST /client/conversations`, `GET /client/conversations/:id/messages`, `POST /client/conversations/:id/messages` | ⬜ À livrer |
| **S3** | `GET /provider/conversations`, `GET /provider/conversations/:id/messages`, `POST /provider/conversations/:id/messages` | ⬜ À livrer |
| **S3** | `GET /agent/providers`, `GET /agent/providers/:id`, `POST /agent/providers/:id/review` | ⬜ À livrer |
| **S3** | `GET /agent/litiges`, `GET /agent/litiges/:id`, `GET /agent/litiges/:id/history` | ⬜ À livrer |
| **S3** | `GET /agent/litiges/:id/messages`, `POST /agent/litiges/:id/messages` | ⬜ À livrer |
| **S4** | `POST /agent/litiges/:id/resolution`, `PUT /agent/litiges/:id/resolution`, `POST /agent/litiges/:id/close`, `POST /agent/litiges/:id/suspend-user` | ⬜ À livrer |
| **S4** | `PATCH /client/litiges/:id/resolution/accept`, `PATCH /client/litiges/:id/resolution/reject` | ⬜ À livrer |
| **S4** | `PATCH /provider/litiges/:id/resolution/accept`, `PATCH /provider/litiges/:id/resolution/reject` | ⬜ À livrer |
| **S4** | `GET /provider/earnings`, `GET /admin/stats`, `GET /admin/transactions`, `PATCH /admin/settings/commission` | ⬜ À livrer |
| **S4** | `POST /uploads/photos`, `POST /uploads/documents` | ⬜ À livrer |
| **S4** | `PATCH /admin/agents/:id/suspend`, `DELETE /admin/agents/:id` | ⬜ À livrer |

> **Convention de statut** : ⬜ À livrer · 🔄 En cours · ✅ Livré et testé · ❌ Bloqué (préciser la raison)

---

## 12. Données mock frontend (fallback)

> Ces fichiers JSON sont utilisés par le frontend quand un endpoint n'est pas encore disponible. Ils doivent correspondre exactement aux schémas définis en section 4.

```
src/data/
├── auth/
│   └── mock_user.json                  # Un User pour chaque rôle (client, provider, admin, agent)
├── client/
│   ├── mock_dashboard.json             # Réponse de GET /client/dashboard
│   ├── mock_demands.json               # Tableau de 5 ServiceDemand
│   ├── mock_quote.json                 # Un Quote complet
│   ├── mock_mission.json               # Une Mission avec 6 étapes
│   ├── mock_conversations.json         # 3 Conversation
│   ├── mock_messages.json              # 10 Message pour une conversation
│   └── mock_providers_search.json      # 6 ProviderSearchResult
├── provider/
│   ├── mock_dashboard.json             # Réponse de GET /provider/dashboard
│   ├── mock_available_demands.json     # 6 AvailableDemand
│   └── mock_earnings.json             # Réponse de GET /provider/earnings
├── admin/
│   ├── mock_dashboard.json             # Réponse de GET /admin/dashboard
│   ├── mock_stats.json                 # Réponse de GET /admin/stats
│   ├── mock_provider_dossier.json      # ProviderProfile + documents + agentReview
│   ├── mock_users.json                 # 5 ManagedUser
│   ├── mock_agents.json                # 3 AgentProfile
│   └── mock_litiges.json              # 4 Litige avec metrics
├── agent/
│   ├── mock_agent_litiges.json         # 4 Litige assignés à l'agent
│   ├── mock_litige_detail.json         # Litige complet avec timeline
│   ├── mock_litige_messages.json       # LitigeMessage[] d'un dossier
│   ├── mock_litige_history.json        # Conversation originale client/prestataire
│   └── mock_provider_reviews.json      # Dossiers à instruire
└── shared/
    ├── mock_categories.json            # 6 ServiceCategory
    └── mock_litige_motifs.json         # 4 LitigeMotif
```

**Convention de switch mock/API dans les services :**

```typescript
// src/services/clientService.ts
const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

export async function getDashboard(): Promise<DashboardData> {
  if (USE_MOCK) {
    const data = await import('../data/client/mock_dashboard.json');
    return data.default;
  }
  const res = await axios.get('/client/dashboard');
  return res.data.data;
}
```

> Mettre `VITE_USE_MOCK=true` dans `.env.development` et `VITE_USE_MOCK=false` en `.env.production`.

---

## 13. Règles de coordination

### Processus de modification de contrat

1. **L'équipe backend** qui veut modifier un endpoint crée un PR dans le repo partagé avec le diff dans ce fichier.
2. **L'équipe frontend** doit approuver le PR avant merge (minimum 1 review).
3. **Délai minimum** : 48h entre la notification et la mise en prod de la modification.
4. **Urgence** : En cas de bug critique nécessitant un changement immédiat, notification Slack #api-contract + appel direct au référent frontend.

### Points de synchronisation hebdomadaires

```
Chaque lundi — 30 minutes — Format standup

Frontend → Backend :
  - Quels endpoints avons-nous testés cette semaine ?
  - Quels problèmes de format ou de données avons-nous rencontrés ?
  - Quels endpoints nous manquent pour la semaine ?

Backend → Frontend :
  - Quels endpoints seront livrés cette semaine ?
  - Y a-t-il des changements de format prévus ?
  - Des blocages côté infra ?
```

### Règle de non-blocage frontend

Le frontend ne s'arrête jamais à cause du backend. Toute intégration suit ce pattern :

```typescript
// Toujours prévoir un fallback
try {
  const data = await getDashboard();
  setState(data);
} catch (error) {
  if (USE_MOCK) {
    const mockData = await import('../data/client/mock_dashboard.json');
    setState(mockData.default);
  } else {
    setError('Impossible de charger les données. Veuillez réessayer.');
  }
}
```

### Versioning du contrat

Ce fichier est versionné dans le repo GitHub à la racine du projet frontend.

```
/
├── src/
├── API_CONTRACT.md   ← ce fichier
└── ...
```

Toute modification est tracée dans l'historique Git avec un message de commit explicite :
```
git commit -m "api-contract: v2.0 — ajout section agent, chat, providers/search [S3]"
```

---

*Document ServiLoc — Frontend Team · Backend Team · Juin 2026*

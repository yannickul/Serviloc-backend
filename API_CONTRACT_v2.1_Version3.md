# API_CONTRACT.md — ServiLoc

> **Document fondateur — à maintenir par les deux équipes (frontend + backend)**
> Toute modification d'un endpoint, d'un format de réponse ou d'un schéma doit être notifiée 48h à l'avance avec un diff explicite dans ce fichier.
> Dernière mise à jour : Juillet 2026 · Version 2.1

**Changelog v2.1 :**
- `POST /auth/verify-otp` : accepte désormais `email` (pas `userId` ni numéro de téléphone) + `code` (pas `otpCode`)
- `POST /auth/resend-otp` : accepte désormais `email` (pas `userId`)
- `POST /auth/register` : réponse retourne `userId` (UUID brut) et `email`, plus de `phone` dans la réponse, `otpSent` retiré
- `POST /auth/verify-otp` : ne retourne plus de tokens — retourne uniquement un message de confirmation. Le login se fait ensuite via `POST /auth/login`
- `POST /auth/login` : réponse enrichie avec `role`, `tokenType`, `expiresIn` et `user` (objet complet selon le rôle)
- `POST /auth/refresh` : réponse enrichie avec `role`, `tokenType`, `expiresIn` et `user`
- Ajout `POST /auth/forgot-password` (nouveau)
- Ajout `POST /auth/reset-password` (nouveau)
- Format ID : tous les IDs retournés sont préfixés (`usr_`, `dem_`, `mis_`, `lit_`, `txn_`)
- JWT : `sub` contient l'email (pas l'UUID), `userId` est un claim séparé, `role` en majuscules dans le token
- `PATCH /provider/profile` : request body enrichi avec `latitude`, `longitude`, `serviceZoneCity`, `radiusKm`
- `PATCH /provider/availability` : réponse enrichie avec `providerId`, `isAvailable`, `message`
- `PATCH /provider/schedule` : request body direct (sans clé `schedule`)
- `GET /provider/earnings` : réponse simplifiée (`monthlyTotal` + `payouts[]` — sans `missions[]` pour l'instant)
- Events RabbitMQ : `user.registered` envoie `email` + `otpCode` (plus `phone`), `provider.validated/rejected` envoie `email` (plus `phone`), `user.suspended` envoie `email` (plus `phone`)
- Section 2.2 JWT mise à jour : payload réel documenté
- Calendrier S1–S4 mis à jour avec statuts réels

---

## Sommaire

1. [Configuration globale](#1-configuration-globale)  
2. [Authentification](#2-authentification)  
3. [Format des réponses](#3-format-des-réponses)  
4. [Schémas des objets métier](#4-schémas-des-objets-métier)  
5. [Endpoints — Auth et user](#5-endpoints--auth-et-user)  
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

### 2.2 Payload JWT décodé (v2.1)

```json
{
  "userId": "b2adb724-8bd7-46b3-b527-b564f5c05a59",
  "role": "CLIENT",
  "type": "access",
  "sub": "user@email.cm",
  "iss": "serviloc",
  "iat": 1748736000,
  "exp": 1748739600
}
```

> ⚠️ **v2.1** : `sub` contient l'**email** (pas l'UUID). L'UUID utilisateur est dans le claim `userId`.  
> Le Gateway extrait `userId` depuis le claim `userId` et l'injecte en header `X-User-Id` vers les services.  
> Le Gateway extrait `role` et l'injecte en header `X-User-Role`.  
> `role` est en **MAJUSCULES** dans le token JWT (`"CLIENT"`, `"PROVIDER"`, `"AGENT"`, `"ADMIN"`).

| Champ | Valeurs possibles | Usage frontend |
|-------|-------------------|----------------|
| `userId` | UUID utilisateur | ID courant (claim séparé de `sub`) |
| `role` | `"CLIENT"` \| `"PROVIDER"` \| `"ADMIN"` \| `"AGENT"` | Routing et affichage conditionnel |
| `sub` | Email de l'utilisateur | Identité Spring Security |

### 2.3 Rafraîchissement automatique

Le frontend appelle `/auth/refresh` automatiquement quand l'API retourne `401 UNAUTHORIZED`. Si le refresh échoue, l'utilisateur est redirigé vers `/login`.

### 2.4 Rôles et accès

| Rôle | Espaces accessibles | Espace interdit |
|------|--------------------|-----------------| 
| `client` | `/client/**`, `/auth/**` | Tout le reste |
| `provider` | `/provider/**`, `/auth/**` | Tout le reste |
| `admin` | `/admin/**`, `/auth/**` | Tout le reste |
| `agent` | `/agent/**`, `/auth/**` | Tout le reste |

> Les préfixes sont contrôlés par le Gateway (RoleAuthFilter). Un token `role=AGENT` ne peut jamais atteindre `/admin/**` → `403 FORBIDDEN`.

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

> `meta` est présent uniquement sur les endpoints paginés. Il est `null` sur les réponses d'entité unique.

### 3.2 Erreur

**Format unique pour TOUTES les erreurs, tous les endpoints.**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_OTP",
    "message": "Code OTP erroné ou expiré",
    "field": "code"
  }
}
```

| Champ | Type | Présence | Description |
|-------|------|----------|-------------|
| `code` | `string` | Toujours | Code machine lisible côté frontend |
| `message` | `string` | Toujours | Message en français pour l'affichage |
| `field` | `string\|null` | Optionnel | Champ concerné (validation de formulaire) |

### 3.3 Codes d'erreur standardisés

| Code HTTP | `error.code` | Signification |
|-----------|-------------|---------------|
| 400 | `VALIDATION_ERROR` | Données de formulaire invalides |
| 400 | `INVALID_OTP` | Code OTP incorrect ou expiré |
| 400 | `INVALID_ARGUMENT` | Argument invalide |
| 401 | `UNAUTHORIZED` | Token absent ou invalide |
| 401 | `INVALID_CREDENTIALS` | Email/mot de passe incorrects |
| 401 | `ACCOUNT_NOT_ACTIVATED` | Compte non activé — vérifier l'OTP |
| 403 | `ACCESS_DENIED` | Rôle insuffisant ou ressource non autorisée |
| 404 | `USER_NOT_FOUND` | Utilisateur introuvable |
| 409 | `EMAIL_ALREADY_EXISTS` | Email déjà enregistré |
| 409 | `INVALID_STATE` | État métier impossible (ex: devis déjà accepté) |
| 429 | `RATE_LIMITED` | Trop de requêtes |
| 500 | `INTERNAL_ERROR` | Erreur serveur |

---

## 4. Schémas des objets métier

(les schémas ci-dessous reprennent v2.1 ; là où des champs ont changé depuis v2.0, la version v2.1 prévaut)

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

> `status` : `"active"` | `"suspended"` | `"pending"`  
> `role` : `"client"` | `"provider"` | `"admin"` | `"agent"` (minuscules dans les réponses)  
> `id` format : `usr_` + 8 premiers caractères de l'UUID sans tirets (v2.1)

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
  "pendingPayments": [
    { "amount": 22000, "missionLabel": "Mission en cours" },
    { "amount": 10000, "missionLabel": "Mission en cours" }
  ],
  "location": {
    "city": "Bafoussam",
    "district": "Quartier Commercial"
  },
  "createdAt": "2026-03-15T10:00:00+01:00"
}
```

> `pendingPayment` : `null` si aucun paiement en cours  
> `location` : `null` si non renseigné

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

(etc. pour les autres schémas — les définitions complètes restent celles de v2.1 listées dans la version fournie)

---

## 5. Endpoints — Auth et user

> Ces endpoints sont accessibles sans token. `POST /auth/login` accepte tous les rôles.

### `POST /auth/register`

Inscription d'un nouvel utilisateur (client ou prestataire uniquement).

**Request body**

```json
{
  "firstName": "Madeleine",
  "lastName": "Kamdem",
  "email": "mk@email.cm",
  "password": "motdepasse123",
  "phone": "+237695123456",
  "role": "client"
}
```

> `role` : `"client"` | `"provider"` uniquement. Les rôles `"admin"` et `"agent"` ne sont pas auto-inscriptibles.

**Response 201**

```json
{
  "success": true,
  "data": {
    "userId": "b2adb724-8bd7-46b3-b527-b564f5c05a59",
    "email": "mk@email.cm",
    "message": "Compte créé. OTP de test : 123456"
  }
}
```

> ⚠️ **v2.1** : `userId` est l'UUID brut (non préfixé), `email` remplace `phone` dans la réponse.

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 409 | `EMAIL_ALREADY_EXISTS` | Email déjà enregistré |
| 400 | `VALIDATION_ERROR` | Champ manquant ou format invalide |

---

### `POST /auth/verify-otp`

Vérification du code OTP et activation du compte.

> ⚠️ **v2.1** : accepte `email` (pas `userId` ni numéro de téléphone). Champ `code` (pas `otpCode`).  
> Ne retourne **pas** de tokens — le compte est activé, l'utilisateur doit ensuite appeler `POST /auth/login`.

**Request body**

```json
{
  "email": "mk@email.cm",
  "code": "123456"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "message": "Compte activé avec succès"
  }
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `INVALID_OTP` | Code incorrect ou expiré |
| 404 | `USER_NOT_FOUND` | Email introuvable |

---

### `POST /auth/resend-otp`

Renvoi d'un nouvel OTP.

> ⚠️ **v2.1** : accepte `email` (pas `userId`).

**Request body**

```json
{
  "email": "mk@email.cm"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "message": "OTP renvoyé. Code de test : 123456"
  }
}
```

---

### `POST /auth/login`

Connexion pour tous les rôles (Client, Prestataire, Admin, Agent).

**Request body**

```json
{
  "email": "mk@email.cm",
  "password": "motdepasse123"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "role": "client",
    "user": {
      "id": "usr_abc123",
      "role": "client",
      "firstName": "Madeleine",
      "lastName": "Kamdem",
      "fullName": "Madeleine Kamdem",
      "phone": "+237695123456",
      "email": "mk@email.cm",
      "avatarInitial": "M",
      "status": "active",
      "createdAt": "2026-03-15T10:00:00+01:00"
    }
  },
  "meta": null
}
```

> ⚠️ **v2.1** : `role` en minuscules dans la réponse (`"client"`, pas `"CLIENT"`).  
> `user` contient le profil complet selon le rôle (ClientProfile, ProviderProfile, etc.).

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 401 | `INVALID_CREDENTIALS` | Email/mot de passe incorrects |
| 401 | `ACCOUNT_NOT_ACTIVATED` | Compte non activé — vérifier l'OTP |

---

### `POST /auth/refresh`

Rafraîchissement du token.

**Request body** : `{ "refreshToken": "eyJhbGci..." }`

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "role": "client",
    "user": { "...User selon le rôle" }
  },
  "meta": null
}
```

---

### `POST /auth/logout`

Révocation du refresh token.

**Request body** : `{ "refreshToken": "..." }`

**Response 200**

```json
{
  "success": true,
  "data": { "message": "Déconnexion réussie" },
  "meta": null
}
```

---

### `POST /auth/forgot-password` *(nouveau — v2.1)*

Demande de réinitialisation de mot de passe.

> Toujours `200` même si l'email n'existe pas (anti-énumération de comptes).

**Request body**

```json
{
  "email": "mk@email.cm"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "message": "Si un compte existe avec cet email, un code de réinitialisation a été envoyé."
  },
  "meta": null
}
```

---

### `POST /auth/reset-password` *(nouveau — v2.1)*

Réinitialisation du mot de passe avec le code OTP reçu.

**Request body**

```json
{
  "email": "mk@email.cm",
  "code": "123456",
  "newPassword": "NouveauMotDePasse123!"
}
```

> `newPassword` : minimum 8 caractères.  
> Après réinitialisation, tous les refresh tokens existants sont révoqués.

**Response 200**

```json
{
  "success": true,
  "data": { "message": "Mot de passe réinitialisé avec succès" },
  "meta": null
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 400 | `INVALID_OTP` | Code incorrect ou expiré |
| 404 | `USER_NOT_FOUND` | Email introuvable |
| 400 | `VALIDATION_ERROR` | Mot de passe trop court |

---

### `GET /user/{id}`

Informations publiques d'un utilisateur.

**Prestataire Response 200**

```json
{
  "success": true,
  "data": {
    "id": "2f19902b-0770-49b9-9974-a92dbb44a77c",
    "role": "provider",
    "firstName": "Jean-Claude",
    "lastName": "Mbarga",
    "fullName": "Jean-Claude Mbarga",
    "phone": "+237699234567",
    "email": "jcm@serviloc.cm",
    "avatarInitial": "J",
    "status": "active",
    "specialty": "Plomberie",
    "rating": 0.0,
    "completedMissions": 0,
    "isAvailable": true,
    "hourlyRate": 4000.0,
    "serviceZone": {
      "city": "Bafoussam",
      "radiusKm": 20.0
    },
    "availability": { /* schedule object */ },
    "certifications": ["Artisan certifié"],
    "estCertifie": true,
    "createdAt": "2026-06-12T12:39:10+01:00"
  },
  "meta": null
}
```

**Utilisateur Response 200**

```json
{
  "success": true,
  "data": {
    "id": "b2adb724-8bd7-46b3-b527-b564f5c05a59",
    "role": "client",
    "firstName": "Yannick",
    "lastName": "Ulrich",
    "fullName": "Yannick Ulrich",
    "phone": "+237612345678",
    "email": "yannick2@serviloc.cm",
    "avatarInitial": "Y",
    "status": "active",
    "completedMissions": 0,
    "location": null,
    "createdAt": "2026-06-08T21:15:28+01:00"
  },
  "meta": null
}
```

---

## 6. Endpoints — Client

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "client"`.

### `GET /client/me`

Profil complet du client connecté.

**Response 200**

```json
{
  "success": true,
  "data": { /* ClientProfile */ },
  "meta": null
}
```

---

### `GET /client/dashboard`

Données agrégées du tableau de bord client.

**Response 200**

```json
{
  "success": true,
  "data": {
    "profile": { /* ClientProfile */ },
    "recentDemands": [ /* ServiceDemand[] */ ],
    "financialSummary": {
      "totalSpent": 55000,
      "completedMissions": 3,
      "pendingPayment": { "amount": 25000, "missionLabel": "Mission Plomberie en cours" }
    },
    "unreadMessages": 2
  }
}
```

---

### `GET /client/demands`

Liste paginée des demandes du client.

**Query params** : `?page=1&limit=20&status=en_cours`

**Response 200**

```json
{
  "success": true,
  "data": [ /* ServiceDemand[] */ ],
  "meta": { "page": 1, "limit": 20, "total": 5, "totalPages": 1 }
}
```

---

### `POST /client/demands`

Création d'une nouvelle demande.

**Request body**

```json
{
  "categoryId": "cat_plomberie",
  "description": "Fuite sous l'évier de la cuisine",
  "photoIds": ["photo_001"],
  "location": { "address": "Bafoussam, Quartier Commercial", "lat": 5.4764, "lng": 10.4207 },
  "isUrgent": false,
  "estimatedBudget": { "min": 20000, "max": 30000 }
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* ServiceDemand */ },
  "meta": null
}
```

---

### `GET /client/demands/:demandId`

Détail d'une demande.

**Response 200**

```json
{
  "success": true,
  "data": { /* ServiceDemand */ },
  "meta": null
}
```

---

### `GET /client/demands/:demandId/quote`

Devis associé à une demande.

**Response 200**

```json
{
  "success": true,
  "data": { /* Quote */ },
  "meta": null
}
```

---

### `POST /client/demands/:demandId/quote/accept`

Acceptation d'un devis et déclenchement du paiement (Saga 1).

**Request body**

```json
{
  "paymentMethod": "orange_money",
  "phoneNumber": "+237695123456"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "quoteId": "quote_001",
    "paymentStatus": "sequestre",
    "message": "Paiement séquestré. La mission peut démarrer."
  }
}
```

---

### `POST /client/demands/:demandId/quote/reject`

Refus d'un devis.

**Response 200**

```json
{
  "success": true,
  "data": { "quoteId": "quote_001", "status": "refuse" }
}
```

---

### `GET /client/missions/:missionId`

Suivi d'une mission.

**Response 200**

```json
{
  "success": true,
  "data": { /* Mission */ },
  "meta": null
}
```

---

### `POST /client/missions/:missionId/validate`

Validation de fin de mission.

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

---

### `POST /client/missions/:missionId/rate`

Notation du prestataire.

**Request body**

```json
{
  "rating": 5,
  "criteria": { "punctuality": "tres_ponctuel", "quality": "excellent", "cleanliness": "tres_propre" },
  "comment": "Excellent travail."
}
```

**Response 201**

```json
{
  "success": true,
  "data": { "ratingId": "rat_001", "targetId": "usr_jcm456", "rating": 5 }
}
```

---

### `POST /client/missions/:missionId/litige`

Signalement d'un litige.

**Request body**

```json
{
  "motifId": "motif_incomplete",
  "description": "Le plombier n'a pas remplacé le siphon.",
  "evidenceIds": ["photo_001"]
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* Litige */ },
  "meta": null
}
```

---

### `PATCH /client/litiges/:litigeId/resolution/accept`

Acceptation de la résolution proposée par l'agent.

**Response 200**

```json
{
  "success": true,
  "data": { "litigeId": "lit_042", "clientAccepted": true, "providerAccepted": false, "status": "en_traitement" }
}
```

---

### `PATCH /client/litiges/:litigeId/resolution/reject`

Refus de la résolution.

**Request body**

```json
{ "reason": "Le remboursement proposé est insuffisant." }
```

**Response 200**

```json
{
  "success": true,
  "data": { "litigeId": "lit_042", "status": "en_traitement" }
}
```

---

### `GET /client/conversations`

Liste des conversations du client.

**Query params** : `?page=1&limit=20`

**Response 200**

```json
{
  "success": true,
  "data": {
    "conversations": [ /* Conversation[] with full fields as in v2.0 */ ],
    "meta": { "page": 1, "limit": 20, "total": 1, "totalPages": 1 }
  },
  "meta": null
}
```

> Structure complète des Conversation / Messages : voir section 4 (v2.0 definitions are fully compatible with v2.1; frontend should expect the fields listed in v2.0 Conversation/Message objects).

---

### `POST /client/conversations`

Ouvrir une conversation (idempotente).

**Request body**

```json
{
  "providerId": "2f19902b-0770-49b9-9974-a92dbb44a77c",
  "demandId": "00000000-0000-0000-0000-000000000001"
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* Conversation */ },
  "meta": null
}
```

---

### `GET /client/conversations/:conversationId/messages`

Messages d'une conversation (du plus récent au plus ancien).

**Query params** : `?page=1&limit=30`

**Response 200**

```json
{
  "success": true,
  "data": {
    "messages": [ /* Message[] with fields as in v2.0 (senderId = UUID brut, imageId in v2.1) */ ],
    "meta": { "page": 1, "limit": 20, "total": 1, "totalPages": 1 }
  },
  "meta": null
}
```

---

### `POST /client/conversations/:conversationId/messages`

Envoyer un message.

**Request body**

```json
{
  "content": "Bonjour, êtes-vous disponible demain matin ?",
  "imageId": null
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* Message */ },
  "meta": null
}
```

---

### `GET /client/providers/search`

Recherche de prestataires disponibles.

**Query params**

| Paramètre | Type | Obligatoire | Description |
|-----------|------|-------------|-------------|
| `specialty` | string | Oui | Ex: `"Plomberie"` |
| `lat` | float | Oui | Latitude client |
| `lng` | float | Oui | Longitude client |
| `radiusKm` | int | Non (défaut: 10) | Rayon de recherche |
| `minRating` | float | Non (défaut: 0) | Note minimale |
| `maxRate` | int | Non (défaut: 0 = illimité) | Tarif horaire max en XAF |

**Response 200**

```json
{
  "success": true,
  "data": [ /* ProviderSearchResult[] as per v2.0 schema */ ],
  "meta": { "page": 1, "limit": 20, "total": 6, "totalPages": 1 }
}
```

---

### `GET /client/categories`

Liste des catégories disponibles.

**Response 200**

```json
{
  "success": true,
  "data": [ /* ServiceCategory[] */ ],
  "meta": null
}
```

---

## 7. Endpoints — Prestataire

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "provider"`.

### `GET /provider/me`

Profil complet du prestataire connecté.

**Response 200**

```json
{
  "success": true,
  "data": { /* ProviderProfile */ },
  "meta": null
}
```

---

### `PATCH /provider/profile`

Mise à jour du profil professionnel (UC18).

> ⚠️ **v2.1** : ajout des champs `latitude`, `longitude`, `serviceZoneCity`, `radiusKm` dans le request body.

**Request body**

```json
{
  "specialty": "Plomberie",
  "hourlyRate": 4000,
  "serviceZoneCity": "Bafoussam",
  "latitude": 5.4737,
  "longitude": 10.4179,
  "radiusKm": 20,
  "estCertifie": true,
  "certifications": ["Artisan certifié"],
  "documentIds": []
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "providerId": "usr_2f19902b",
    "message": "Profil mis à jour avec succès"
  },
  "meta": null
}
```

---

### `PATCH /provider/availability`

Mise à jour de la disponibilité.

**Request body**

```json
{ "isAvailable": true }
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "providerId": "usr_2f19902b",
    "isAvailable": true,
    "message": "Disponibilité mise à jour"
  },
  "meta": null
}
```

---

### `PATCH /provider/schedule`

Mise à jour des horaires hebdomadaires.

> ⚠️ **v2.1** : request body direct (sans clé `schedule`).

**Request body**

```json
{
  "monday":    { "start": "08:00", "end": "18:00", "available": true },
  "tuesday":   { "start": "08:00", "end": "18:00", "available": true },
  "wednesday": { "start": "08:00", "end": "18:00", "available": true },
  "thursday":  { "start": "08:00", "end": "18:00", "available": true },
  "friday":    { "start": "08:00", "end": "18:00", "available": true },
  "saturday":  { "start": "08:00", "end": "13:00", "available": true },
  "sunday":    { "start": null,    "end": null,    "available": false }
}
```

**Response 200**

```json
{
  "success": true,
  "data": { "providerId": "usr_2f19902b", "message": "Horaires mis à jour avec succès" },
  "meta": null
}
```

---

### `GET /provider/dashboard`

Tableau de bord prestataire.

**Response 200** → (version complète reprise depuis v2.0)

```json
{
  "success": true,
  "data": {
    "profile": { /* ProviderProfile */ },
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
    "recentMissions": [ /* Mission[] */ ],
    "availability": { /* scheduleObject */ }
  }
}
```

---

### `GET /provider/demands/available`

Demandes disponibles dans la zone du prestataire.

**Response 200**

```json
{
  "success": true,
  "data": [ /* ServiceDemand[] — see v2.0 available demand example */ ],
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
  "data": { "demandId": "dem_xyz789", "status": "applied", "message": "Vous pouvez maintenant créer votre devis." }
}
```

---

### `POST /provider/demands/:demandId/quote`

Création d'un devis.

**Request body**

```json
{
  "demandId": "00000000-0000-0000-0000-000000000001",
  "providerId": "2f19902b-0770-49b9-9974-a92dbb44a77c",
  "amount": 25000,
  "description": "Remplacement joint + siphon évier cuisine",
  "materials": [
    { "name": "Joint silicone", "quantity": 2, "unitPrice": 1500 },
    { "name": "Siphon PVC",    "quantity": 1, "unitPrice": 4500 }
  ],
  "estimatedDurationHours": 2
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* Quote */ },
  "meta": null
}
```

---

### `GET /provider/missions`

Liste des missions.

**Query params** : `?status=en_cours&page=1`

**Response 200**

```json
{
  "success": true,
  "data": [ /* Mission[] */ ],
  "meta": { "page": 1, "limit": 20, "total": 6, "totalPages": 1 }
}
```

---

### `GET /provider/missions/:missionId`

Détail d'une mission.

**Response 200**

```json
{
  "success": true,
  "data": { /* Mission */ },
  "meta": null
}
```

---

### `POST /provider/missions/:missionId/start`

Démarrage d'une mission.

**Response 200**

```json
{
  "success": true,
  "data": { "missionId": "msn_001", "status": "en_cours", "startedAt": "2026-05-21T09:32:00+01:00" }
}
```

---

### `PATCH /provider/missions/:missionId/steps/:stepId`

Mise à jour d'une étape.

**Request body**

```json
{ "completed": true }
```

**Response 200**

```json
{
  "success": true,
  "data": { "stepId": "step_001", "completed": true, "missionProgress": 67 }
}
```

---

### `POST /provider/missions/:missionId/complete`

Déclaration de fin de mission.

**Response 200**

```json
{
  "success": true,
  "data": { "missionId": "msn_001", "bothValidated": false, "status": "terminee" }
}
```

---

### `POST /provider/missions/:missionId/rate`

Notation du client.

**Request body**

```json
{ "rating": 4, "comment": "Client ponctuel." }
```

**Response 201**

```json
{
  "success": true,
  "data": { "ratingId": "rat_002", "targetId": "usr_abc123", "rating": 4 }
}
```

---

### `POST /provider/missions/:missionId/litige`

Signalement d'un litige par le prestataire.

**Request body**

```json
{
  "motifId": "motif_non_paiement",
  "description": "Le client refuse de valider malgré une prestation correctement réalisée.",
  "evidenceIds": ["photo_002"]
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* Litige */ },
  "meta": null
}
```

---

### `PATCH /provider/litiges/:litigeId/resolution/accept`

Acceptation de la résolution.

**Response 200**

```json
{
  "success": true,
  "data": { "litigeId": "lit_042", "providerAccepted": true, "status": "resolu", "message": "Les deux parties ont accepté. L'agent peut clôturer." }
}
```

---

### `PATCH /provider/litiges/:litigeId/resolution/reject`

Refus de la résolution.

**Request body**

```json
{ "reason": "Je réclame le paiement total de la prestation." }
```

**Response 200**

```json
{
  "success": true,
  "data": { "litigeId": "lit_042", "status": "en_traitement" }
}
```

---

### `GET /provider/conversations`

Liste des conversations du prestataire.

**Response 200**

```json
{
  "success": true,
  "data": [ /* Conversation[] with provider as owner */ ],
  "meta": { "page": 1, "limit": 20, "total": 3, "totalPages": 1 }
}
```

---

### `GET /provider/conversations/:conversationId/messages`

Messages d'une conversation.

**Response 200**

```json
{
  "success": true,
  "data": { "messages": [ /* Message[] */ ], "meta": { "page":1, "limit":30, "total": 10, "totalPages": 1 } },
  "meta": null
}
```

---

### `POST /provider/conversations/:conversationId/messages`

Envoyer un message.

**Request body**

```json
{ "content": "...", "imageId": null }
```

**Response 201**

```json
{
  "success": true,
  "data": { /* Message */ },
  "meta": null
}
```

---

### `GET /provider/earnings`

Historique des gains.

**Query params** : `?page=1&month=2026-05`

**Response 200**

```json
{
  "success": true,
  "data": {
    "monthlyTotal": 18000,
    "payouts": [
      {
        "id": "pyt_5da5fc39",
        "transactionId": "txn_21d2602e",
        "amount": 16200,
        "commissionAmount": 1800,
        "status": "pending",
        "externalRef": null,
        "createdAt": "2026-06-19T08:46:28+01:00"
      }
    ]
  },
  "meta": null
}
```

> ⚠️ **v2.1** : `missions[]` non encore inclus (implémentation future). Seuls `monthlyTotal` et `payouts[]` sont retournés.

---

## 8. Endpoints — Admin

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "admin"`.

### `GET /admin/dashboard`

Tableau de bord administrateur. *(version complète reprise depuis v2.0 — inchangé en structure, inclus ici pour complétude)*

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
    "pendingValidations": [ /* ProviderProfile[] */ ],
    "activeLitiges":      [ /* Litige[] */ ],
    "popularCategories":  [ /* ServiceCategory[] */ ],
    "recentTransactions": [ /* Transaction[] */ ]
  }
}
```

---

### `GET /admin/stats`

Statistiques complètes avec historique (UC33).

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

Liste des prestataires.

**Query params** : `?status=pending_verification&page=1`

**Response 200**

```json
{
  "success": true,
  "data": { "providers": [ /* ProviderProfile[] */ ], "meta": { "page": 1, "limit": 20, "total": 5, "totalPages": 1 } },
  "meta": null
}
```

---

### `GET /admin/providers/:providerId`

Dossier complet d'un prestataire.

**Response 200**

```json
{
  "success": true,
  "data": {
    "id": "usr_2f19902b",
    "role": "provider",
    /* ...ProviderProfile */,
    "documents": [ /* docs as in v2.0 */ ],
    "agentReview": {
      "agentName": "Pauline F.",
      "verdict": "approved",
      "comment": "Dossier complet.",
      "reviewedAt": "2026-06-16T21:26:31+01:00"
    }
  },
  "meta": null
}
```

> `agentReview` est `null` si aucun agent n'a encore instruit le dossier.

---

### `POST /admin/providers/:providerId/validate`

Validation d'un dossier prestataire.

**Response 200**

```json
{
  "success": true,
  "data": { "providerId": "usr_2f19902b", "status": "validated", "message": "Dossier validé. Le prestataire a été notifié." },
  "meta": null
}
```

---

### `POST /admin/providers/:providerId/reject`

Rejet d'un dossier.

**Request body**

```json
{ "reason": "Casier judiciaire manquant." }
```

**Response 200**

```json
{
  "success": true,
  "data": { "providerId": "usr_2f19902b", "status": "rejected", "message": "Dossier rejeté. Le prestataire a été notifié." },
  "meta": null
}
```

---

### `POST /admin/providers/:providerId/notify`

Envoi d'une notification de rappel au prestataire.

**Request body**

```json
{ "message": "Votre casier judiciaire est manquant." }
```

**Response 200**

```json
{
  "success": true,
  "data": { "providerId": "usr_2f19902b", "status": "notified", "message": "Notification envoyée au prestataire." },
  "meta": null
}
```

---

### `GET /admin/users`

Liste paginée des utilisateurs.

**Query params** : `?role=client&status=active&search=Madeleine&page=1`

**Response 200**

```json
{
  "success": true,
  "data": {
    "users": [ /* ManagedUser[] */ ],
    "meta": { "page": 1, "limit": 20, "total": 4, "totalPages": 1 }
  },
  "meta": null
}
```

---

### `PATCH /admin/users/:userId/suspend`

Suspension d'un utilisateur.

**Request body**

```json
{
  "reason": "Comportement frauduleux",
  "duration": "7d"
}
```

**Response 200**

```json
{
  "success": true,
  "data": { "userId": "usr_2f19902b", "status": "suspended", "duration": "7d", "reason": "Comportement frauduleux" },
  "meta": null
}
```

---

### `PATCH /admin/users/:userId/reactivate`

Réactivation d'un compte suspendu.

**Response 200**

```json
{
  "success": true,
  "data": { /* User restored object */ },
  "meta": null
}
```

---

### `GET /admin/agents`

Liste des agents.

**Query params** : `?page=1&limit=20`

**Response 200**

```json
{
  "success": true,
  "data": { "agents": [ /* AgentProfile[] */ ], "meta": { "page": 1, "limit": 20, "total": 3, "totalPages": 1 } },
  "meta": null
}
```

---

### `POST /admin/agents`

Création d'un compte agent.

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

**Response 201**

```json
{
  "success": true,
  "data": { /* AgentProfile */ },
  "meta": null
}
```

---

### `GET /admin/agents/:agentId`

Détail d'un agent.

**Response 200**

```json
{
  "success": true,
  "data": { /* AgentProfile */ },
  "meta": null
}
```

---

### `PATCH /admin/agents/:agentId/suspend`

Suspension d'un agent.

**Request body**

```json
{ "reason": "..." , "duration": "7d" }
```

**Response 200**

```json
{
  "success": true,
  "data": { "userId": "usr_201a92e4", "status": "suspended", "duration": "7d", "reason": "..." },
  "meta": null
}
```

---

### `DELETE /admin/agents/:agentId`

Suppression définitive d'un agent.

**Response 200**

```json
{
  "success": true,
  "data": { "agentId": "usr_201a92e4", "deleted": true },
  "meta": null
}
```

---

### `GET /admin/litiges`

Liste des litiges avec métriques.

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
    "litiges": [ /* Litige[] */ ]
  },
  "meta": { "page": 1, "limit": 20, "total": 23 }
}
```

---

### `GET /admin/litiges/:litigeId`

Détail d'un litige.

**Response 200**

```json
{
  "success": true,
  "data": {
    /* ...Litige */,
    "client":   { /* ClientProfile */ },
    "provider": { /* ProviderProfile */ },
    "agent":    { /* AgentProfile or null */ }
  },
  "meta": null
}
```

---

### `POST /admin/litiges/:litigeId/assign`

Assignation d'un agent.

**Request body**

```json
{ "agentId": "usr_agent01" }
```

**Response 200**

```json
{
  "success": true,
  "data": { "litigeId": "lit_042", "agentId": "usr_agent01", "status": "assigne" },
  "meta": null
}
```

---

### `PUT /admin/litiges/:litigeId/assign`

Réassignation à un autre agent.

**Request body**

```json
{ "agentId": "usr_agent02" }
```

**Response 200**

```json
{
  "success": true,
  "data": { "litigeId": "lit_042", "agentId": "usr_agent02", "status": "assigne" },
  "meta": null
}
```

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
  },
  "meta": null
}
```

---

### `GET /admin/transactions`

Liste des transactions.

**Query params** : `?status=sequestre&page=1`

**Response 200**

```json
{
  "success": true,
  "data": { "transactions": [ /* Transaction[] */ ], "meta": { "page":1, "limit":20, "total": 12 } },
  "meta": null
}
```

---

### `PATCH /admin/settings/commission`

Mise à jour des taux de commission.

**Request body**

```json
{ "standardRate": 10, "urgencyRate": 15 }
```

**Response 200**

```json
{
  "success": true,
  "data": { "standardRate": 10, "urgencyRate": 15, "message": "Taux de commission mis à jour" },
  "meta": null
}
```

---

### `GET /admin/categories`, `POST /admin/categories`, `PUT /admin/categories/:id`, `DELETE /admin/categories/:id`

Endpoints de gestion des catégories (structures inchangées par rapport à v2.0 — incluses ici pour complétude). Request/response identiques aux définitions complètes de v2.0 (create/put acceptent label/iconKey/color, responses retournent ServiceCategory).

---

## 9. Endpoints — Agent Service Client

> Tous ces endpoints nécessitent `Authorization: Bearer <token>` avec `role: "agent"`.

(NB : v2.1 a ajouté `suspendedById` on `POST /agent/litiges/:litigeId/suspend-user`. Les autres endpoints conservent la définition complète de v2.0 — reprises ci-dessous.)

### `GET /agent/providers`

Liste des dossiers prestataires à instruire (UC30-agent).

**Query params** : `?page=1&limit=20`

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

**Response 200**

```json
{
  "success": true,
  "data": {
    /* same structure as GET /admin/providers/:providerId — profile + documents + agentReview */
  },
  "meta": null
}
```

---

### `POST /agent/providers/:providerId/review`

Instruction du dossier (UC30-agent).

**Request body**

```json
{
  "verdict": "approved",
  "comment": "Dossier complet. Certifications vérifiées."
}
```

> `verdict` : `"approved"` | `"rejected"` | `"needs_revision"`

**Response 200**

```json
{
  "success": true,
  "data": {
    /* ProviderReview structure, see section 4.13 from v2.0 */,
    "message": "Instruction enregistrée. L'administrateur a été notifié pour décision finale."
  },
  "meta": null
}
```

---

### `GET /agent/litiges`

Litiges assignés à l'agent connecté (UC36).

**Query params** : `?status=en_traitement&page=1&limit=20`

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

Détail d'un litige assigné (UC36).

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
    "evidences": [ { "type": "photo", "url": "https://cdn.serviloc.cm/evidence/photo_001.jpg" } ],
    "client":   { "id": "usr_abc",    "fullName": "Alice Nguetse",    "phone": "+237691..." },
    "provider": { "id": "usr_jcm456", "fullName": "Jean-Claude M.",   "phone": "+237677..." },
    "resolution": null,
    "timeline": [ { "event": "Litige ouvert", "at": "2026-05-18T10:00:00+01:00" }, { "event": "Assigné à l'agent", "at": "2026-05-18T11:30:00+01:00" } ]
  },
  "meta": null
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 403 | `FORBIDDEN` | Ce litige n'est pas assigné à cet agent |

---

### `GET /agent/litiges/:litigeId/history`

Historique du chat client/prestataire (UC37).

**Response 200**

```json
{
  "success": true,
  "data": {
    "conversationId": "conv_001",
    "messages": [ /* Message[] */ ]
  },
  "meta": null
}
```

---

### `GET /agent/litiges/:litigeId/messages`

Échanges agent/parties.

**Query params** : `?page=1&limit=30`

**Response 200**

```json
{
  "success": true,
  "data": [ /* LitigeMessage[] */ ],
  "meta": { "page":1, "limit":30, "total": 10, "totalPages": 1 }
}
```

---

### `POST /agent/litiges/:litigeId/messages`

L'agent envoie un message.

**Request body**

```json
{
  "content": "Bonjour, j'ai bien pris en charge votre dossier. Pouvez-vous préciser la date exacte des travaux ?",
  "recipientRole": "client",
  "attachmentId": null
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* LitigeMessage */ },
  "meta": null
}
```

---

### `POST /agent/litiges/:litigeId/resolution`

Proposition de résolution (UC38).

**Request body**

```json
{
  "type": "remboursement_partiel",
  "refundAmount": 11500,
  "note": "Remboursement de 50% accordé — prestation partiellement réalisée selon les preuves."
}
```

**Response 201**

```json
{
  "success": true,
  "data": { /* Resolution */ , "message": "Proposition envoyée aux deux parties pour acceptation." },
  "meta": null
}
```

---

### `PUT /agent/litiges/:litigeId/resolution`

Modification de la résolution.

**Request body** — même structure que POST.

**Response 200**

```json
{
  "success": true,
  "data": { /* Resolution updated */ },
  "meta": null
}
```

---

### `POST /agent/litiges/:litigeId/close`

Clôture définitive (UC38).

**Response 200**

```json
{
  "success": true,
  "data": { "litigeId": "lit_042", "status": "cloture", "refundAmount": 11500, "message": "Litige clôturé. Remboursement en cours." },
  "meta": null
}
```

**Erreurs possibles**

| Code | `error.code` | Détail |
|------|-------------|--------|
| 409 | `UNPROCESSABLE` | Les deux parties n'ont pas encore accepté la résolution |
| 503 | `SERVICE_UNAVAILABLE` | Service Paiement indisponible — la clôture sera retentée |

---

### `POST /agent/litiges/:litigeId/suspend-user`

Suspension contextuelle d'une partie (UC31-agent).

> ⚠️ **v2.1** : ajout du champ `suspendedById` (UUID de l'agent) dans la requête. La durée est fixée à **7 jours**.

**Request body**

```json
{
  "userId": "usr_abc123",
  "reason": "Fraude confirmée.",
  "suspendedById": "201a92e4-cda4-4769-8b7b-57d89d409c27"
}
```

> Le `userId` doit obligatoirement être le `clientId` ou le `providerId` du litige. Toute autre valeur retourne `403 FORBIDDEN`.

**Response 200**

```json
{
  "success": true,
  "data": {
    "userId": "usr_abc123",
    "status": "suspended",
    "duration": "7d",
    "litigeId": "lit_abc123"
  }
}
```

---

## 10. Upload de fichiers

> Ces endpoints sont accessibles à tous les rôles authentifiés.

### `POST /uploads/photos`

**Request** : `multipart/form-data` — champ `photos` (jpg/png/webp, max 5 Mo), champ `context`

**Response 201**

```json
{
  "success": true,
  "data": {
    "uploads": [
      { "id": "photo_001", "url": "https://cdn.serviloc.cm/uploads/photo_001.jpg", "name": "photo.jpg", "sizeBytes": 245000 }
    ]
  }
}
```

---

### `POST /uploads/documents`

**Request** : `multipart/form-data` — champ `document` (PDF/image, max 10 Mo), champ `type`

**Response 201**

```json
{
  "success": true,
  "data": {
    "uploads": [
      { "id": "doc_001", "url": "https://cdn.serviloc.cm/uploads/doc_001.pdf", "name": "doc.pdf", "sizeBytes": 345000 }
    ]
  }
}
```

---

## 11. Calendrier de livraison backend

| Semaine | Endpoints livrés | Statut |
|---------|-----------------|--------|
| **S1** | `POST /auth/register`, `POST /auth/verify-otp`, `POST /auth/resend-otp`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` | ✅ Livré et testé |
| **S1** | `GET /client/categories`, `GET/POST/PUT/DELETE /admin/categories/**` | ✅ Livré et testé (KKP) |
| **S1** | Eureka Server + API Gateway opérationnels | ✅ Livré et testé |
| **S2** | `GET /client/me`, `GET /provider/me` | ✅ Livré et testé |
| **S2** | `PATCH /provider/profile`, `PATCH /provider/availability`, `PATCH /provider/schedule` | ✅ Livré et testé |
| **S2** | `GET /admin/users`, `PATCH /admin/users/:id/suspend`, `PATCH /admin/users/:id/reactivate` | ✅ Livré et testé |
| **S2** | `GET /admin/providers`, `GET /admin/providers/:id` | ✅ Livré et testé |
| **S2** | `GET /admin/agents`, `POST /admin/agents`, `GET /admin/agents/:id` | ✅ Livré et testé |
| **S2** | `GET/POST /client/conversations`, `GET/POST /client|provider/conversations/:id/messages` | ✅ Livré et testé |
| **S2** | `GET /client/providers/search` | ⏳ En attente Service Missions (TK) |
| **S2** | `GET /client/dashboard`, `GET /provider/dashboard`, `GET /admin/dashboard` | ⏳ En attente Service Missions (TK) |
| **S3** | `POST /admin/providers/:id/validate|reject|notify` | ✅ Livré et testé |
| **S3** | `GET /agent/providers`, `GET /agent/providers/:id`, `POST /agent/providers/:id/review` | ✅ Livré et testé |
| **S3** | `POST /client/demands`, `GET /client/demands`, `GET /client/demands/:id` | ⏳ En attente Service Missions (TK) |
| **S3** | `GET /client/demands/:id/quote`, `POST /client/demands/:id/quote/accept|reject` | ⏳ En attente Service Missions (TK) |
| **S3** | `GET /client|provider/missions/**`, `POST /provider/missions/:id/start|complete` | ⏳ En attente Service Missions (TK) |
| **S3** | `GET /admin/litiges`, `GET /admin/litiges/:id`, `POST /admin/litiges/:id/assign` | ⏳ En attente Service Litiges (TK) |
| **S3** | `GET /agent/litiges/**`, `POST /agent/litiges/:id/messages` | ⏳ En attente Service Litiges (TK) |
| **S4** | `GET /provider/earnings` | ✅ Livré et testé |
| **S4** | `GET /admin/transactions`, `PATCH /admin/settings/commission` | ✅ Livré et testé |
| **S4** | `PATCH /admin/agents/:id/suspend`, `DELETE /admin/agents/:id` | ✅ Livré et testé |
| **S4** | `POST /auth/forgot-password`, `POST /auth/reset-password` | ✅ Livré et testé |
| **S4** | `POST /agent/litiges/:id/resolution`, `POST /agent/litiges/:id/close` | ⏳ En attente Service Litiges (TK) |
| **S4** | `PATCH /client|provider/litiges/:id/resolution/accept|reject` | ⏳ En attente Service Litiges (TK) |
| **S4** | `POST /uploads/photos`, `POST /uploads/documents` | ✅ Livré et testé (KKP) |
| **S4** | `GET /admin/stats` | ⏳ En attente Service Missions (TK) |

> **Convention de statut** : ⬜ À livrer · ⏳ En attente dépendance · 🔄 En cours · ✅ Livré et testé · ❌ Bloqué

---

## 12. Données mock frontend (fallback)

*(voir v2.0 — inchangé — listes et chemins de fichiers identiques, s'assurer que les JSONs respectent les schémas complétés ci-dessus)*

Structure recommandée (extrait) :

```
src/data/
├── auth/
│   └── mock_user.json
├── client/
│   ├── mock_dashboard.json
│   ├── mock_demands.json
│   ...
```

> Voir la section 12 de v2.0 pour l'arborescence complète et l'exemple de switch mock/API dans les services.

---

## 13. Règles de coordination

*(processus de modification, points de synchronisation, règles de non-blocage, versioning — identiques à v2.0 ; inclus ici pour référence)*

- Toute modification d'un endpoint, d'un schéma ou d'un format doit être notifiée 48h à l'avance avec un diff explicite dans ce fichier (PR + description).  
- En urgence, use Slack #api-contract + appel référent frontend.  
- Le frontend doit prévoir des mocks (VITE_USE_MOCK) et un fallback clair.

---

Document ServiLoc — Frontend Team · Backend Team · Juillet 2026 · Version 2.1
# Corrections apportées — service-litiges

Basé sur `AUDIT_service-litiges.md`. Les IDs (UUID brut) n'ont volontairement PAS été modifiés,
à votre demande — ils restent exploitables tels quels côté front.

## 1. Format global (`ApiResponse`)
- Ajout du champ `meta` (utilisé pour la pagination : `PageMeta{page, limit, total, totalPages}`).
- Erreurs désormais en objet imbriqué `error: { code, message, field }` au lieu de `errorCode`/`errorMessage` à plat.
- `GlobalExceptionHandler` mis à jour en conséquence (le `field` est renseigné automatiquement sur les erreurs de validation).

## 2. Statuts conformes au contrat
- `LitigeStatus.toContractLabel()` / `fromContractLabel()` : mappe `OUVERT→"ouvert"`, `EN_COURS→"assigne"`, `RESOLU→"resolu"`, `FERME→"cloture"`.
- `LitigeService.toResponse()` sérialise désormais ce libellé.

## 3. Nouveaux contrôleurs (le gros du manque)
- **`AgentLitigeController`** (`/agent/litiges/**`) : liste, détail, historique, messages (lecture/écriture),
  proposition de résolution (POST), modification (PUT), clôture (POST close), suspension d'un utilisateur.
- **`ClientProviderLitigeController`** (`/client/litiges/**`, `/provider/litiges/**`) : accept/reject de la résolution proposée.
- **`AdminLitigeController`** : `POST /admin/litiges/{id}/resolve` supprimé (hors contrat) ; ajout de
  `PUT /admin/litiges/{id}/assign` (réassignation), `GET /admin/litiges/stats`, et enrichissement de
  `GET /admin/litiges/{id}` avec les profils client/provider/agent.

## 4. Nouveau flux de résolution (remplace l'ancien `resolveLitige` admin)
`AgentLitigeService` :
1. `proposeResolution` (POST) — crée la `Resolution`, publie `litige.resolution_proposed`, litige reste `EN_COURS`.
2. Client/prestataire acceptent ou refusent via `ClientProviderLitigeService` (`clientAccepted`/`providerAccepted`).
3. `updateResolution` (PUT) — remet les deux acceptations à `null` si l'agent modifie la proposition.
4. `closeLitige` (POST close) — vérifie que les deux parties ont accepté (sauf `REJET`), déclenche le remboursement,
   passe le litige à `RESOLU`, publie `litige.resolved`.

Le bug du `if` dupliqué + log toujours exécuté (section 8 de l'audit) a disparu avec la réécriture de cette logique.

## 5. Événements RabbitMQ
- **`litige.resolved`** — payload corrigé selon votre spécification exacte :
  `{ litigeId, resolution, refundAmount, clientId, providerId }`, avec `resolution` = `"refund"` ou `"reject"`
  (mapping dans `LitigeEventPublisher.toResolutionLabel`).
- **`litige.assigned`** et **`litige.resolution_proposed`** : nouveaux events, émis respectivement lors de
  l'assignation/réassignation et de la proposition/modification de résolution.
- `litige.opened` : non modifié (vous n'avez signalé aucun problème dessus).

## 6. Appels sortants (Feign) mis à jour selon vos formats confirmés
- **`NegociationClient`** : `ConversationDto{id, demandId, clientId, providerId, status, createdAt}` — conforme au format que vous avez donné pour service-negociations.
- **`PaiementClient`** : `freeze` et `refund` renvoient désormais `TransactionDto` (format confirmé) ; le body de `refund` est réduit à `{ amount }` (le champ `reason` n'existe plus, il n'était pas dans votre spec).
- **`UtilisateurClient`** (nouveau) : `GET /internal/users/{id}` et `PATCH /internal/users/{id}/suspend`.
  ⚠️ Ce contrat interne n'était précisé nulle part (ni API_CONTRACT, ni vos notes) — les routes et le DTO
  `UserProfileDto{id, name, email, role}` sont une hypothèse raisonnable, à confirmer avec l'équipe
  service-utilisateurs avant la démo. Le code est isolé derrière `UserProfilePort`/`UtilisateurClient`
  donc l'ajustement sera localisé si le vrai contrat diffère.

## 7. Persistance
- `LitigeMessageJpaRepository` (vide dans le repo original) + `LitigeMessageRepositoryAdapter` +
  `domain/repository/LitigeMessageRepository` : implémentés pour stocker les messages agent↔client/prestataire
  (utilisés par `/agent/litiges/{id}/messages`).
- `LitigeJpaRepository` / `LitigeRepositoryAdapter` : ajout de `countByStatus`, `countByStatusAndUpdatedAtAfter`,
  `sumAmountByStatusIn` pour les metrics et `/admin/litiges/stats`.

## Ce qui reste à faire / à vérifier de votre côté
1. **Confirmer le contrat interne `service-utilisateurs`** (`UtilisateurClient`) et ajuster si besoin.
2. **`service-mission` appelle bien `POST /internal/litiges`** — vous avez indiqué que la version corrigée le fait ; vérifier que le payload envoyé correspond exactement à `CreateLitigeRequest` (demandId, missionId, clientId, providerId, motifId, description, evidenceIds, amount, transactionId).
3. Créer les migrations/DDL si `ddl-auto: create-drop` est remplacé par un outil de migration en prod (la table `litige_messages` est nouvelle du point de vue JPA mais l'entité existait déjà dans le code — seul le repository manquait).
4. Aucun test automatisé n'existe dans le module (`src/test` ne contient que des `.gitkeep`) — à prévoir pour les nouveaux services `AgentLitigeService` / `ClientProviderLitigeService`, notamment les règles métier (double acceptation, montant de remboursement, motif REJET).

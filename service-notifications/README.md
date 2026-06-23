# service-notifications — ServiLoc

Microservice responsable de l'envoi de **toutes** les notifications sortantes de la plateforme
ServiLoc : SMS (OTP + alertes), push mobile (Firebase FCM), emails transactionnels.

> Ce service ne publie **aucun événement** RabbitMQ — il est purement **consommateur**
> des événements émis par les autres microservices sur l'exchange `serviloc.events`.

## Infos service

| | |
|---|---|
| Port HTTP | `8086` |
| Base de données | PostgreSQL `db_notifications` (port hôte local `5446`) |
| Service discovery | Eureka (`@EnableDiscoveryClient`) |
| Messaging | RabbitMQ — exchange topic `serviloc.events`, queue `notifications.queue` |

## Architecture (DDD / Clean Architecture)

```
src/main/java/com/serviloc/notifications/
├── domain/                  # Cœur métier — zéro dépendance framework
│   ├── model/                # Entités/VO : NotificationLog, DeviceToken...
│   ├── repository/           # Ports de persistance (interfaces)
│   └── exception/            # Exceptions métier
├── application/
│   ├── port/in/               # Use cases (interfaces, ex: RegisterDeviceTokenUseCase)
│   ├── port/out/              # Ports sortants (ex: PushSender, SmsSender, EmailSender)
│   ├── service/                # Implémentations @Service @Transactional
│   └── dto/                    # DTOs d'entrée/sortie des use cases
├── infrastructure/
│   ├── persistence/            # Entités JPA + adapters de repository
│   ├── messaging/               # Consumers RabbitMQ (listeners par événement)
│   ├── client/                   # Adapters Firebase FCM, SMS sandbox
│   └── config/                    # @Configuration (Firebase, RabbitMQ, OpenAPI...)
└── presentation/
    ├── controller/              # @RestController (endpoints /internal/**)
    ├── dto/                      # DTOs REST (request/response)
    └── exception/                 # @RestControllerAdvice
```

## Lancer en local

> La base `db-notifications` (port hôte `5446`) est démarrée via le **docker-compose global du
> repo ServiLoc** (géré par l'équipe Service Utilisateurs), pas depuis ce dossier — il n'y a plus
> de `docker-compose.yaml` local ici pour éviter un conflit de `container_name`.

```bash
# 1. Crée ton .env (jamais commité) avec les VRAIES valeurs DB_NAME/DB_USERNAME/DB_PASSWORD
#    (= POSTGRES_NOTIFS_DB/USER/PASSWORD du docker-compose global — demande-les à l'équipe)
cp .env .env

# 2. Vérifie que la DB, Eureka et RabbitMQ tournent déjà (docker-compose racine du repo)
docker ps

# 3. Exporte les variables puis lance l'application avec le profil local
export $(grep -v '^#' .env | xargs)
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

Swagger UI : http://localhost:8086/swagger-ui.html

## Lancer les tests

```bash
mvn test
```

Le test d'intégration (`PaymentConfirmedNotificationIntegrationTest`) démarre automatiquement un
PostgreSQL et un RabbitMQ via Testcontainers (Docker requis, aucune configuration manuelle) :
publie `payment.confirmed` sur l'exchange réel, attend le traitement asynchrone du consumer, puis
vérifie le `NotificationLog` créé en base et via `GET /internal/notification-logs/:userId`.


## Statut d'avancement

- [x] Squelette + structure DDD + configuration (`application.yml`, `application-local.yml`)
- [x] Configuration Firebase Admin SDK (FCM sandbox)
- [x] Configuration client SMS sandbox
- [x] `POST /internal/device-tokens`
- [x] 16 consumers RabbitMQ activés (logique métier réelle, cf. `NotificationEventListener`)
- [x] DLQ `notifications.dlq` (retry ×3 → log alerte)
- [x] Consumer `litige.resolved`
- [x] `GET /internal/notification-logs/:userId`
- [x] Test Testcontainers (`payment.confirmed` → `NotificationLog` créé + vérifié via l'endpoint REST)
- [ ] Documentation Swagger (annotations posées, relecture finale à faire)

## ⚠️ Champs à ajouter aux payloads RabbitMQ (côté services émetteurs)

Ces champs sont lus de façon défensive (warning + envoi sauté si absent) — à ajouter dès que possible :

| Événement | Champ à ajouter | Émis par |
|---|---|---|
| `user.registered` | `otpCode` | Service Utilisateurs |
| `provider.validated` | `phone` | Service Utilisateurs |
| `provider.rejected` | `phone` | Service Utilisateurs |
| `user.suspended` | `phone` | Service Utilisateurs |
| `agent.created` | `provisionalPassword` | Service Utilisateurs |
| `demand.published` | `targetProviderIds` (liste résolue par zone+catégorie) | Service Missions/Demandes |
| `litige.resolved` | `clientId`, `providerId` | Service Litiges |

Hypothèse à confirmer avec l'équipe Service Négociations : `negotiation.conversation_opened` n'a pas de
champ `recipientId` explicite — le destinataire est actuellement supposé être `providerId`.

Convention à valider avec le frontend admin : les notifications "push admin" (`provider.reviewed`,
`litige.opened`) sont envoyées au pseudo-userId `admin_broadcast` — chaque session admin devrait
enregistrer un device token sous cet identifiant (en plus de son token personnel).

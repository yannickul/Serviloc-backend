# service-fichiers — ServiLoc

Service Fichiers (Media) : upload de photos/documents, stockage MinIO, endpoints
internes de consultation pour les autres microservices. Port `8088`, DB `db_media`
(port hôte `5448`), aucun événement RabbitMQ (service purement synchrone).

## Lancer en local

```bash
cp .env.example .env
docker compose up -d db-media minio
./mvnw spring-boot:run
```

Swagger UI : http://localhost:8088/swagger-ui.html

> Ce socle a été généré sans accès réseau (pas d'appel à start.spring.io ni
> `mvn` exécuté dans cet environnement) : à la première ouverture du projet,
> lance `./mvnw clean verify` (ou génère le wrapper avec `mvn -N wrapper:wrapper`
> si `mvnw` est absent) pour valider la compilation avant de continuer.

## Endpoints

### Publics (via Gateway, tous rôles authentifiés)

```
POST /uploads/photos       multipart: photos[] (jpg/png/webp, max 5 Mo), context
POST /uploads/documents    multipart: document (pdf/image, max 10 Mo), type
```

Réponse 201 :
```json
{ "success": true, "data": { "uploads": [{ "id", "url", "name", "sizeBytes" }] } }
```

Erreurs : `400 VALIDATION_ERROR` (format invalide), `413 FILE_TOO_LARGE`,
`415` (type dangereux détecté, ex. exécutable renommé).

### Internes (header `X-Internal-Token`, jamais exposés au Gateway)

```
GET    /internal/files/{id}            Métadonnées d'un fichier
GET    /internal/files/{id}/url        URL publique
DELETE /internal/files/{id}            Suppression (MinIO + base)
POST   /internal/files/batch/urls      { "ids": [...] } → { "urls": { id: url } }
GET    /internal/files/check/{id}      { "exists": true|false }
```

## Tester les limites (tâche "Test limites")

```bash
TOKEN=changeme-internal-token   # doit matcher INTERNAL_TOKEN

# > 5 Mo en photo → 413 FILE_TOO_LARGE
dd if=/dev/urandom of=big.jpg bs=1M count=6
curl -i -X POST http://localhost:8088/uploads/photos \
  -H "X-User-Id: usr_test123" \
  -F "photos=@big.jpg;type=image/jpeg" -F "context=test"

# > 10 Mo en document → 413 FILE_TOO_LARGE
dd if=/dev/urandom of=big.pdf bs=1M count=11
curl -i -X POST http://localhost:8088/uploads/documents \
  -H "X-User-Id: usr_test123" \
  -F "document=@big.pdf;type=application/pdf" -F "type=cni"

# .exe (même renommé en .jpg) → 415
cp /bin/ls fake.jpg   # binaire ELF, détecté comme non-image par Tika
curl -i -X POST http://localhost:8088/uploads/photos \
  -H "X-User-Id: usr_test123" \
  -F "photos=@fake.jpg;type=image/jpeg" -F "context=test"

# Endpoint interne — token invalide → 401
curl -i http://localhost:8088/internal/files/00000000-0000-0000-0000-000000000000 \
  -H "X-Internal-Token: mauvais-token"

# Endpoint interne — token valide
curl -i http://localhost:8088/internal/files/<id> -H "X-Internal-Token: $TOKEN"
```

## Ce qui reste à faire par l'équipe (hors scope généré)

- Générer/committer le Maven Wrapper (`mvnw`) si absent du dépôt Git du projet.
- Lancer `./mvnw clean verify` une première fois pour confirmer la compilation
  (non exécuté ici, pas d'accès réseau/Maven dans l'environnement de génération).
- Fusionner `docker-compose.yaml` avec le compose global du projet (eureka,
  gateway, rabbitmq...) plutôt que de le lancer isolément en prod/CI.
- Ajuster `INTERNAL_TOKEN` pour qu'il soit strictement identique dans tous les
  microservices (partagé via `.env` du dépôt principal).

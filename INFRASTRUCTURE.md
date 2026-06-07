# ServiLoc — Infrastructure Docker : guide de démarrage

Ce guide permet à chaque membre de l’équipe de lancer l’environnement de développement local (Eureka, bases de données, RabbitMQ, Redis, MinIO) et de vérifier son bon fonctionnement.

## 1. Prérequis

- **Docker** et **Docker Compose** installés
- **Git** (pour cloner le repo)
- **curl** (pour les tests)

## 2. Cloner le dépôt et préparer l’environnement

```bash
git clone <url-du-repo>
cd Serviloc-backend
cp .env.example .env
```
Éditez le fichier .env pour définir des mots de passe sécurisés (au minimum RABBITMQ_DEFAULT_PASS, REDIS_PASSWORD, MINIO_ROOT_PASSWORD).

Si vous ne changez rien, gardez au moins les valeurs dans le fichier .env pour RabbitMQ et le meme pour Redis .
## 3. Lancer toute l’infrastructure
```bash

docker compose up -d
```
Cette commande démarre :

    Eureka Server (port 8761)

    RabbitMQ (ports 5672, 15672)

    Redis (port 6379)

    8 bases PostgreSQL (ports 5441 à 5448)

    MinIO (ports 9000, 9001)

Les healthchecks sont configurés ; attendez environ 40 secondes pour que tous les services passent en healthy.
## 4. Vérifier que tout fonctionne
## 4.1. État général
```bash

docker compose ps
```
Vous devez voir tous les services avec un statut healthy (sauf peut‑être eureka-server si le healthcheck échoue – voir section 5).
## 4.2. Tests spécifiques

| Service | Commande / URL | Résultat attendu |
| :--- | :--- | :--- |
| **Eureka** | `curl http://localhost:8761/actuator/health` | `{"status":"UP"}` |
| **Eureka Dashboard** | http://localhost:8761 | Interface web visible |
| **RabbitMQ API** | `curl -u admin:admin123 http://localhost:15672/api/exchanges/%2F` | Liste des exchanges, dont `serviloc.events` |
| **RabbitMQ UI** | http://localhost:15672 *(admin / admin123)* | Interface de gestion |
| **Redis** | `docker exec serviloc-redis redis-cli -a redis123 ping` | `PONG` |
| **MinIO health** | `curl http://localhost:9000/minio/health/live` | 200 OK (corps vide) |
| **MinIO Console** | http://localhost:9001 *(minioadmin / mot de passe du .env)* | Interface web |
## 4.3. Accès aux bases de données (exemple pour utilisateurs)
```bash

docker exec -it serviloc-db-utilisateurs psql -U svc_utilisateurs -d db_utilisateurs
```

Tapez \q pour quitter.
## 5. Résolution des problèmes courants
## 5.1. RabbitMQ : not_authorized sur l’API ou l’UI

Cause : L’utilisateur admin n’a pas été créé automatiquement à cause du montage des fichiers de configuration.

Solution (une seule fois) :
```bash

# Arrêter et supprimer le conteneur RabbitMQ (sans supprimer les volumes)
docker compose stop rabbitmq
docker compose rm -f rabbitmq
docker volume rm serviloc_rabbitmq_data   # Supprime les données existantes
docker compose up -d rabbitmq

# Créer l’utilisateur admin avec les identifiants du .env
docker exec -it serviloc-rabbitmq rabbitmqctl add_user admin MOT_DE_PASSE
docker exec -it serviloc-rabbitmq rabbitmqctl set_user_tags admin administrator
docker exec -it serviloc-rabbitmq rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

Testez ensuite avec curl -u admin:admin123 http://localhost:15672/api/exchanges/%2F.
```
## 5.2. Eureka reste unhealthy

Cause : curl n’est pas installé dans l’image Docker par défaut.

Solution : L’image a déjà été corrigée et reconstruite. Si le problème persiste, relancez la construction :
```bash

cd eureka-server
./mvnw clean package -DskipTests   # ou mvn clean package
cd ..
docker compose build eureka-server --no-cache
docker compose up -d eureka-server
```

## 5.3. MinIO ne répond pas sur le port 9000

Cause Parfois, le healthcheck est vert mais le port n’est pas accessible depuis l’hôte. Vérifiez avec curl -v http://localhost:9000/. Si la connexion aboutit avec 200 OK, c’est normal (le contenu est vide). Si la connexion échoue, redémarrez MinIO :
```bash

docker compose restart minio
```

## 5.4. Impossible de se connecter à une base PostgreSQL

Vérifiez que le .env contient le bon mot de passe pour cette base. Redémarrez le conteneur concerné :
```bash

docker compose restart db-utilisateurs
```

## 5.5. Ports déjà utilisés

Si un port (ex: 8761, 5441, 5672) est déjà occupé, changez‑le dans le docker-compose.yml (côté hôte) et dans les configurations des microservices.
## 6. Mettre à jour l’infrastructure après un pull

Lorsque Yannick pousse des modifications (fichiers Docker, definitions.json, etc.), exécutez :
```bash

git pull
docker compose down -v   # Attention : supprime toutes les données des volumes (bases, messages, etc.)
docker compose build --no-cache
docker compose up -d
```

Si vous voulez conserver les données des volumes, omettez le -v. Mais pour appliquer certaines modifications (ex: definitions.json), il faut recréer RabbitMQ.
## 7. Arrêter et nettoyer
```bash

docker compose down       # arrête les conteneurs (conserve les volumes)
docker compose down -v    # arrête et supprime les volumes (perte des données)
```


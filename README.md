# ServiLoc – Plateforme de mise en relation entre clients et prestataires

ServiLoc est une plateforme backend basée sur une architecture microservices Spring Boot. Elle permet aux clients de publier des demandes de service, aux prestataires de postuler, de négocier, de payer via Mobile Money (séquestre), d’exécuter des missions, d’évaluer et de gérer les litiges.

## Architecture

- **8 microservices** : Utilisateurs, Missions, Négociations, Paiement, Litiges, Notifications, Catégories, Fichiers
- **API Gateway** (Spring Cloud Gateway) + **Eureka Server** (service discovery)
- **Communication asynchrone** : RabbitMQ (topic exchange `serviloc.events`)
- **Bases de données** : PostgreSQL (une par service)
- **Cache & rate limiting** : Redis
- **Stockage objet** : MinIO (S3-compatible)
- **Conteneurisation** : Docker & Docker Compose

## Équipe et répartition

| Rôle | Développeur(s) | Services |
|------|----------------|----------|
| Lead backend | Yannick        | API Gateway, Utilisateurs, Négociations, Paiement |
| Backend | baros          | Missions, Litiges |
| Backend | KKP            | Notifications, Catégories, Fichiers |

## Prérequis

- Java 21 (pour les microservices)
- Maven (ou wrapper Maven)
- Docker & Docker Compose
- Git
- curl (pour les tests)

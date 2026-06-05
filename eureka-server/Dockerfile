# =============================================================================
# ServiLoc — Eureka Server — Dockerfile
# Build multi-étapes : compile avec Maven, exécute avec JRE minimal
# =============================================================================

# --- Étape 1 : Build ---
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copier le wrapper Maven et le pom.xml en premier (cache layer)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Télécharger les dépendances (layer cachée tant que pom.xml ne change pas)
RUN ./mvnw dependency:go-offline -q

# Copier les sources et compiler
COPY src ./src
RUN ./mvnw package -DskipTests -q

# --- Étape 2 : Runtime ---
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Sécurité : exécuter avec un utilisateur non-root
RUN addgroup -S serviloc && adduser -S serviloc -G serviloc
USER serviloc

# Copier le JAR depuis l'étape builder
COPY --from=builder /build/target/*.jar eureka-server.jar

# Port Eureka
EXPOSE 8761

# Variables d'environnement avec valeurs par défaut
ENV EUREKA_HOSTNAME=localhost
ENV SPRING_PROFILES_ACTIVE=docker

# Point d'entrée avec options JVM optimisées pour conteneur
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "eureka-server.jar"]

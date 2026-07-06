# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copier le pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JDK 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copier le jar généré
COPY --from=build /app/target/service-files-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port (par défaut 8088 pour service-files)
EXPOSE 8088

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]

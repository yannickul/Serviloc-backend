# Étape 1 : Builder le projet avec Maven et JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copier le pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le code source et builder le jar
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JRE 21
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copier le jar généré depuis l’étape de build
COPY --from=build /app/target/service-categories-*.jar app.jar

# Exposer le port du microservice
EXPOSE 8087

# Commande de lancement
ENTRYPOINT ["java", "-jar", "app.jar"]

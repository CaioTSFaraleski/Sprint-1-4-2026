# Etapa 1: compila a API com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Etapa 2: imagem final só com o JRE e o jar
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
COPY --from=build /app/target/safefield-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

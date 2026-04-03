# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl

ENV PORT=8080
EXPOSE 8080

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:23-jdk
WORKDIR /app

COPY --from=build /app/target/fabia-1.0.0-SNAPSHOT-runner.jar app.jar
COPY src/main/resources/application.properties src/main/resources/application.properties

ENTRYPOINT ["java","-jar","app.jar"]

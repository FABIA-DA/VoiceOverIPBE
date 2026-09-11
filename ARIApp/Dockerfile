FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app

COPY CallContext /app/CallContext

RUN mvn -f /app/CallContext/pom.xml clean install -DskipTests

COPY ARIApp/pom.xml /app/ARIApp/pom.xml
COPY ARIApp/src /app/ARIApp/src

WORKDIR /app/ARIApp

RUN mvn clean package -DskipTests


FROM eclipse-temurin:23-jdk

WORKDIR /app

COPY --from=build \
    /app/ARIApp/target/fabia-1.0.0-SNAPSHOT-runner.jar \
    app.jar

COPY --from=build \
    /app/ARIApp/src/main/resources/application.properties \
    src/main/resources/application.properties

ENTRYPOINT ["java", "-jar", "app.jar"]

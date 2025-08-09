FROM gradle:jdk24-alpine AS build

WORKDIR /app

COPY --chown=gradle . .

RUN ./gradlew :jar --no-build-cache --no-daemon

FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
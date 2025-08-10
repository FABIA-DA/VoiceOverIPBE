FROM gradle:8.14-jdk24-alpine AS build

WORKDIR /app

COPY --chown=gradle . .

RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
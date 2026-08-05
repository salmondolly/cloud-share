FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S cloudshare && adduser -S cloudshare -G cloudshare \
    && mkdir -p /app/uploads \
    && chown -R cloudshare:cloudshare /app
COPY --from=build /workspace/target/*.jar app.jar
USER cloudshare
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

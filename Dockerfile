# Multi-stage build for Spring Boot app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy fat jar from build stage
COPY --from=build /app/target/smart-banking-system-0.0.1-SNAPSHOT.jar app.jar
# Required by many PaaS platforms
ENV PORT=8080
EXPOSE 8080
# Database config via environment variables
# Example (Render/Railway):
#   SPRING_DATASOURCE_URL=jdbc:mysql://<host>:<port>/<db>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
#   SPRING_DATASOURCE_USERNAME=<user>
#   SPRING_DATASOURCE_PASSWORD=<pass>
ENTRYPOINT ["java","-jar","/app/app.jar"]

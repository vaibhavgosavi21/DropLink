# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw.cmd mvnw pom.xml ./
RUN chmod +x mvnw 2>/dev/null || true
COPY src ./src
RUN ./mvnw package -DskipTests -q 2>/dev/null || ./mvnw.cmd package -DskipTests -q

# ---- Run stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p storage/uploads storage/temp && chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

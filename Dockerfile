# ===============================
# BUILD STAGE
# ===============================
FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

COPY src ./src

RUN ./mvnw -B clean package -DskipTests


# ===============================
# RUNTIME STAGE
# ===============================
FROM eclipse-temurin:26-jre

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --from=build /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

# Temporary diagnostic: prints presence only, never secret values
ENTRYPOINT ["sh", "-c", "echo '--- DATABASE CONFIG DIAGNOSTICS ---'; if [ -n \"$DB_URL\" ]; then echo 'DB_URL is present'; else echo 'DB_URL is MISSING'; fi; if [ -n \"$SPRING_DATASOURCE_URL\" ]; then echo 'SPRING_DATASOURCE_URL is present'; else echo 'SPRING_DATASOURCE_URL is missing'; fi; echo '--- APPLICATION CONFIGURATION ---'; jar tf app.jar | grep 'application.*properties' || true; exec java -jar app.jar --debug"]
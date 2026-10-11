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

# Create a non-root user for security
RUN useradd --system --uid 10001 appuser

# Copy the built Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Run as a non-root user
USER appuser

# Render web service port
EXPOSE 8080

# Start Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
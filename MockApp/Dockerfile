# Stage 1: Build with Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Step 1: Cache dependencies (only re-runs if pom.xml changes)
COPY pom.xml .
RUN mvn dependency:go-offline

# Step 2: Build the source code
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Tiny Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the final artifact
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# The Entrypoint allows Spring to pick up the SPRING_PROFILES_ACTIVE env var automatically
ENTRYPOINT ["java", "-jar", "app.jar"]
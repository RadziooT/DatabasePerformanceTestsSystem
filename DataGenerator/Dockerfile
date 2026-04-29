# Multi-stage build for data-generator
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -q dependency:resolve

COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/DataGenerator-*.jar app.jar
ENTRYPOINT ["sh", "-c", "exec java $JAVA_TOOL_OPTIONS $JAVA_OPTS -jar /app/app.jar"]

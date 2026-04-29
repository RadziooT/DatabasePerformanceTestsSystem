FROM maven:3.9.9-eclipse-temurin-21-alpine

WORKDIR /app

# Install required libraries for Netty native transport support in Alpine
RUN apk add --no-cache openssl libc6-compat ca-certificates

# Copy dependency metadata first so Docker can cache dependency downloads
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Copy test sources and resources used by Gatling.
COPY src ./src

ENV SIMULATION_CLASS=example.BasicSimulation
ENV BASE_URL=http://host.docker.internal:8080
ENV DATASET_SIZE=NOT_SET
ENV DATABASE_TYPE=NOT_SET

CMD ["sh", "-c", "\
export MAVEN_OPTS=\"$JAVA_TOOL_OPTIONS $JAVA_OPTS\"; \
SIMULATION_NAME=${SIMULATION_CLASS##*.}; \
OUTPUT_BASE=${SIMULATION_NAME}-${DATABASE_TYPE}-${DATASET_SIZE}; \
RUN_ID=$(cat /proc/sys/kernel/random/uuid); \
OUTPUT_DIR=${OUTPUT_BASE}-${RUN_ID}; \
echo \"Using OUTPUT_BASE=$OUTPUT_BASE\"; \
mvn -B test-compile gatling:test \
  -Dgatling.simulationClass=${SIMULATION_CLASS} \
  -DbaseUrl=${BASE_URL} \
  -DdatasetSize=${DATASET_SIZE} \
  -DdatabaseType=${DATABASE_TYPE}; \
LATEST=$(ls -td target/gatling/* | head -1); \
mv \"$LATEST\" \"target/gatling/${OUTPUT_DIR}\"; \
echo \"Renamed $LATEST -> target/gatling/${OUTPUT_DIR}\" \
"]

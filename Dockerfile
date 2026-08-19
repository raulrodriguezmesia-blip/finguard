# Dockerfile for AI-Powered Fintech & Cloud Observability Platform
# Multi-stage build: Maven/Temurin JDK 21 -> Distroless JRE

# Stage 1: Build with Maven and Temurin JDK 21
FROM eclipse-temurin:21-jdk AS build

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run with distroless JRE image
FROM gcr.io/distroless/java21-debian12

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/observability-platform-1.0.0-SNAPSHOT.jar ./app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
# Dockerfile for AI-Powered Fintech & Cloud Observability Platform
# Multi-stage build: Maven/Temurin JDK 21 -> Distroless JRE

# Stage 1: Build with Maven and Temurin JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (includes spring-boot repackage goal)
RUN mvn clean package -DskipTests

# Stage 2: Run with distroless JRE image
FROM gcr.io/distroless/java21-debian12

# Set working directory
WORKDIR /app

# Copy the fat JAR from the build stage
# Note: spring-boot-maven-plugin repackages to: *.jar (same name)
COPY --from=build /app/target/observability-platform-1.0.0-SNAPSHOT.jar /app/app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
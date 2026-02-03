# --- STAGE 1: BUILD THE APPLICATION ---
# We use a Docker image that already has Maven and Java 21 installed
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the build file first (to cache dependencies)
COPY pom.xml .

# Download dependencies (this step will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application (skip tests to speed it up)
RUN mvn clean package -DskipTests

# --- STAGE 2: RUN THE APPLICATION ---
# We switch to a smaller, lightweight Java runtime image
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the JAR file from the "build" stage to this new stage
COPY --from=build /app/target/*.jar app.jar

# Open port 8080 (where Spring Boot lives)
EXPOSE 8080

# The command to start the app
ENTRYPOINT ["java", "-jar", "app.jar"]

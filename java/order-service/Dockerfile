# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the Spring Boot jar with the Maven wrapper ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Copy the wrapper and POM first so the dependency layer is cached between builds
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Copy sources and build the executable jar (tests run in CI, not in the image build)
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# ---- Runtime stage: minimal JRE, non-root user ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/target/*.jar app.jar
USER app

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

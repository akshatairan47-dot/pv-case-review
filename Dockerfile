# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY backend/gradlew backend/settings.gradle backend/build.gradle ./
COPY backend/gradle ./gradle
RUN ./gradlew dependencies --no-daemon

COPY backend/src ./src
RUN ./gradlew bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system pvreview && useradd --system --gid pvreview pvreview
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown pvreview:pvreview app.jar
USER pvreview

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

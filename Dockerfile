# =============================================================================
# Stage 1 — Builder
# =============================================================================
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon -x test

# =============================================================================
# Stage 2 — Runtime
# =============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

RUN addgroup --system speechai \
    && adduser --system --ingroup speechai speechai

COPY --from=builder \
     --chown=speechai:speechai \
     /app/build/libs/*.jar app.jar

USER speechai

EXPOSE 8080

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
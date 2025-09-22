# ---- Build stage (JDK 21 + gradle wrapper) ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# copy wrapper & files để tận dụng cache
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY settings.gradle.kts build.gradle.kts ./


# tải deps để cache
RUN ./gradlew --no-daemon dependencies || true

# copy src cuối cùng
COPY src ./src

RUN ./gradlew --no-daemon clean bootJar -x test --stacktrace --info

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN adduser --disabled-password --gecos "" appuser && chown -R appuser:appuser /app
USER appuser
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8" \
    TZ=Asia/Ho_Chi_Minh \
    SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","/app/app.jar"]

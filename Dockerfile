# ---- Build stage ----
FROM gradle:8.10.2-jdk-21 AS build
WORKDIR /workspace

# Copy source vào container
COPY . .

# Build JAR (bỏ test cho nhanh, CI thật thì bỏ -x test đi)
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Tạo user không phải root để chạy app
RUN adduser --disabled-password --gecos "" appuser && chown -R appuser:appuser /app
USER appuser

# Copy jar từ stage build
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080

# Tối ưu JVM trong container
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8" \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java","-jar","/app/app.jar"]

# ---------- Stage 1: build với Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies: copy pom trước, chỉ tải lại khi pom đổi
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Stage 2: chạy trên JRE 17 gọn nhẹ ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Chạy bằng user không đặc quyền
RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /app/target/exam-query-forge-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

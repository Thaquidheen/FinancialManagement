FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml ./
# Avoid go-offline (it pulls lots of plugins and times out). Build with retries.
COPY src ./src
RUN mvn -B -U -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=8 \
    -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
    -Dmaven.wagon.http.connectionTimeout=60000 \
    -Dmaven.wagon.http.timeout=120000 \
    clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar /app/app.jar

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/auth/health || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
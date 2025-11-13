# Dockerfile

# 1) Build stage: compile the Spring Boot app with Maven on Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY . .
RUN mvn -q -DskipTests package

# 2) Run stage: run the fat JAR on a lightweight Java 21 runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
# Adjust JAR name if your artifact/version differ
COPY --from=build /app/target/booksreview-0.0.1-SNAPSHOT.jar app.jar

# Render sets PORT; pass it through to Spring Boot
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh","-c","java -jar /app/app.jar --server.port=${PORT}"]

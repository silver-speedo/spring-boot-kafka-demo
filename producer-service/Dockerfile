FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven
RUN mvn clean verify -pl producer-service -am

FROM eclipse-temurin:21-jdk
ARG JAR_FILE=/app/producer-service/target/producer-service-*.jar
COPY --from=builder ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
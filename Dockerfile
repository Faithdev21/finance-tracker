FROM maven:3.8.6-openjdk-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim

WORKDIR /app

COPY --from=builder /app/target/FinanceTracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8189

ENTRYPOINT ["java", "-jar", "app.jar"]
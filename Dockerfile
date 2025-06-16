FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/FinanceTracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8189

ENTRYPOINT ["java", "-jar", "app.jar"]
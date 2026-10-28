# Stage 1: Building the application using Maven
FROM maven:3.9.8-amazoncorretto-21-al2023 AS build
WORKDIR /app
COPY pom.xml .

# Caching Maven dependencies
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Launch the finished jar file
FROM amazoncorretto:21-alpine3.18
WORKDIR /app
COPY --from=build /app/target/average-price-tracker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

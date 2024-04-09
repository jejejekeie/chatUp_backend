# First stage: Maven build environment with Java 17
FROM maven:3.8.4-openjdk-17 AS build

# Copy the pom.xml and source code
COPY ./pom.xml /usr/src/app/pom.xml
COPY ./src /usr/src/app/src

# Set the working directory for following commands
WORKDIR /usr/src/app

# Package the application
RUN mvn clean package -DskipTests

# Second stage: Create the final image with just the JRE and our built JAR
FROM eclipse-temurin:17-jre-jammy

# Copy the JAR from the build stage to the final image
COPY --from=build /usr/src/app/target/*.jar /usr/app/app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/usr/app/app.jar"]

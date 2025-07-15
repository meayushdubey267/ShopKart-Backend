# Use a base image with Java
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy built JAR from local machine to container
COPY target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]

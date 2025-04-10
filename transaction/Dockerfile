# Use a lightweight JRE image
FROM docker-0.unsee.tech/openjdk:21-jdk-slim


# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/transaction-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8080


# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


# Use Maven with OpenJDK 21 as base image
FROM maven:3.9-openjdk-21-slim

# Set working directory
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies (cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/*.jar"]
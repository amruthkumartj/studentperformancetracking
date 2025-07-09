# --- Stage 1: Build the Application ---
# Use a full OpenJDK 17 image as the base for the build stage.
FROM openjdk:17-jdk-slim AS build

# Install Maven on top of the OpenJDK image.
# This ensures we have Maven available for building.
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Set the working directory inside the build container.
WORKDIR /app

# Copy the Maven project files (pom.xml) first to leverage Docker cache.
COPY pom.xml .

# Copy the rest of your source code.
COPY src ./src

# Package your application into a WAR file.
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
# Use a lightweight Tomcat image with JRE 17 for the final runtime.
FROM tomcat:9.0-jdk17-openjdk-slim

# Set the working directory inside the final container to Tomcat's webapps directory.
WORKDIR /usr/local/tomcat/webapps/

# Copy the WAR file from the 'build' stage into the final image's webapps directory.
COPY --from=build /app/target/studentrepo-0.0.1-SNAPSHOT/studentrepo-0.0.1-SNAPSHOT.war .

# Expose port 8080, the default port Tomcat listens on.
EXPOSE 8080

# Define the command to run when the container starts.
CMD ["catalina.sh", "run"]
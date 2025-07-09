# --- Stage 1: Build the Application ---
# Use a full JDK image for compiling and packaging your Java application.
# Corrected image tag: using 'jdk-17' instead of 'openjdk-17'
FROM maven:3.8.7-jdk-17 AS build

# Set the working directory inside the build container.
WORKDIR /app

# Copy the Maven project files (pom.xml) first to leverage Docker cache.
# If pom.xml doesn't change, Maven dependencies won't be re-downloaded.
COPY pom.xml .

# Copy the rest of your source code.
COPY src ./src

# Package your application into a WAR file.
# This command will generate 'target/studentrepo-0.0.1-SNAPSHOT.war'
# inside this build stage's /app directory.
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
# Use a lightweight Tomcat image with JRE 17 for the final runtime.
# This keeps the final Docker image size small.
FROM tomcat:9.0-jdk17-openjdk-slim

# Set the working directory inside the final container to Tomcat's webapps directory.
WORKDIR /usr/local/tomcat/webapps/

# Copy the WAR file from the 'build' stage into the final image's webapps directory.
# The path '/app/target/studentrepo-0.0.1-SNAPSHOT/studentrepo-0.0.1-SNAPSHOT.war'
# refers to the location where the WAR was generated in the 'build' stage.
COPY --from=build /app/target/studentrepo-0.0.1-SNAPSHOT/studentrepo-0.0.1-SNAPSHOT.war .

# Expose port 8080, the default port Tomcat listens on.
EXPOSE 8080

# Define the command to run when the container starts.
# 'catalina.sh run' starts the Tomcat server in the foreground.
CMD ["catalina.sh", "run"]
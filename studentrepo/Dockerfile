# --- Stage 1: Build the Application ---
FROM openjdk:17-jdk-slim AS build

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
COPY src ./src

# This command will now build 'target/studentrepo.war' due to <finalName> in pom.xml
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
FROM tomcat:9.0-jdk17-openjdk-slim

WORKDIR /usr/local/tomcat/webapps/

# Copy the WAR file from the 'build' stage.
# The path MUST be '/app/target/studentrepo.war' to match the finalName in pom.xml.
COPY --from=build /app/target/studentrepo.war .

EXPOSE 8080

CMD ["catalina.sh", "run"]
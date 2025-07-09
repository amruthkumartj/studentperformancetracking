# Use a lightweight OpenJDK 17 base image with Tomcat 9.
# This image provides a pre-configured Tomcat 9 server with JDK 17,
# which matches the Java 17 version specified in your pom.xml.
FROM tomcat:9.0-jdk17-openjdk-slim

# Set the working directory inside the container.
# This is where Tomcat's web applications are typically deployed.
WORKDIR /usr/local/tomcat/webapps/

# Copy your generated WAR file into the webapps directory.
# The WAR file name is 'studentrepo-0.0.1-SNAPSHOT.war' based on your pom.xml's artifactId and version.
# The path reflects where Maven places the WAR file within the 'target' directory.
COPY target/studentrepo-0.0.1-SNAPSHOT/studentrepo-0.0.1-SNAPSHOT.war .

# Expose port 8080. This is the default port that Tomcat listens on.
# Render will use this port to route external traffic to your application.
EXPOSE 8080

# Define the command to run when the container starts.
# 'catalina.sh run' starts the Tomcat server in the foreground.
# This ensures your application remains active and accessible.
CMD ["catalina.sh", "run"]
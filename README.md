
## Student Performance Tracking System

A comprehensive, self-hosted web application built with a pure Java backend for managing and tracking student academic performance. This project provides a secure, multi-user environment for administrators, faculty, and students to streamline academic monitoring, communication, and data analysis.

### ✨ Key Features

This platform is divided into three distinct user roles with tailored functionalities:


#### 👑 Admin

  * **Faculty Management:**  Approve or deny faculty registration requests, ensuring only authorized personnel access the system.
  * **Student Lifecycle Management:**  Add, view, edit, and delete student records. Assign students to specific programs and semesters.
  * **Data Integrity:**  Prevents duplicate student entries by validating unique IDs, email addresses, and phone numbers.
  * **Course Allocation:**  Assign specific programs (e.g., BCA, MCA) to faculty members, restricting their access to relevant students only.
  * **Secure Communication:**  Broadcast messages and important updates to all or specific faculty members.


#### 🧑‍🏫 Faculty

  * **Role-Based Access Control:** Faculty can only view and manage students within the programs they are assigned to by the admin.
 
  * **Timed Attendance System:**
      * Initiate attendance sessions for a specific class (program & semester).
      * A 15-minute timer ensures sessions are completed promptly.
      * Quickly mark students using filters, "Mark All as Present," and "Mark All as Absent" buttons.
      * If the timer expires, all un-marked students are defaulted to 'Present'.
      * Attendance sessions are locked upon starting and cannot be canceled.

  * **Efficient Marks Management:**
      * Enter and update marks for various exam types (e.g., Internal Assessment 1, SEE).
      * The system prevents duplicate mark entries for the same student and exam type.
      * Easily edit previously entered marks to correct errors.

  * **📊 Performance Analytics:**
      * Visualize overall class performance with charts and tables to identify academic trends.
      * Drill down to an individual student's performance, viewing detailed reports on marks and attendance.
      * Receive an automated analysis summary (e.g., "Excellent," "Good," "Needs Improvement").
 
  * **Utilities:**
      * **Event Scheduler:** Schedule seminars, guest lectures, and other events.
      * **Messaging:** Communicate directly with other faculty members.
      * **Profile Management:** Update personal details, profile picture, and password.


#### 🎓 Student

  * **Secure Registration:** Students can only register and log in if their Student ID has been added to the system by a faculty or admin.
  * **Performance Dashboard:** Track personal marks across all subjects and semesters.
  * **Attendance Monitoring:** View personal attendance percentages for each course to stay on track.
  * **Profile Management:** Manage personal profile and update login credentials.


### 🔒 Security & UI

  * **Strong Password Hashing:** All user passwords are secured using Bcrypt (12-factor), ensuring they are unreadable even by database administrators.
  * **OTP Password Reset:** A secure "Forgot Password" flow using One-Time Passwords.
  * **Modern & Responsive UI:** Built with Material Design 3, the interface is clean, intuitive, and fully responsive, providing a seamless experience on both desktop and mobile devices.
  * **Dark/Light Mode:** A user-friendly toggle for visual comfort.


### 🛠️ Tech Stack

  * **Backend:** Java (Servlets, JSP)
  * **Build Tool:** Apache Maven
  * **Database:** MySQL
  * **Deployment:** Docker
  * **Frontend:** HTML, Material Design 3 CSS, JavaScript
  * **Password Hashing:** BCrypt

-----


### 🚀 Getting Started

Follow these instructions to get a local copy up and running for development and testing purposes.


#### Prerequisites

Make sure you have the following software installed on your machine:

  * Java Development Kit (JDK) 11 or newer
  * Apache Maven
  * MySQL Server
  * Git
  * Apache Tomcat 9.x (for local deployment without Docker)


#### Installation & Setup

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/amruthkumartj/studentperformancetracking.git
    cd studentperformancetracking
    ```


2.  **Database Configuration:**

      * Create a new MySQL database for the project (e.g., `student_performance_db`).
      * You'll need to import the initial schema. Look for an `.sql` file in the repository (e.g., `schema/database.sql` or similar) and import it into your newly created database.
      * Update the database connection details (URL, username, password) in the project's configuration file. This is typically located in `src/main/resources/db.properties` or similar.


3.  **Build the project with Maven:**
    This command will compile the source code, run any tests, and package the application into a `.war` file in the `target/` directory.

    ```bash
    mvn clean install
    ```


4.  **Run the application (Local Tomcat Deployment):**
    If you're deploying locally without Docker, deploy the generated `.war` file (`target/studentrepo.war`) to your Apache Tomcat 9.x servlet container.

      * **Manual Deployment:** Copy `target/studentrepo.war` to the `webapps` directory of your Tomcat installation.
      * **Maven Tomcat Plugin (if configured):** Some projects use the Tomcat Maven plugin for direct deployment. Check `pom.xml` for `tomcat7-maven-plugin` or similar configurations.

    Once deployed, start your Tomcat server, and the application should be accessible at `http://localhost:8080/studentrepo` (or `http://localhost:8080/` if you renamed the `war` file to `ROOT.war` as done in the Dockerfile).

-----


### 🐳 Docker Deployment

Containerizing the application with Docker is the recommended way to deploy it to cloud platforms like Render, Heroku, or AWS. This packages the application and its entire environment, resolving any library or runtime inconsistencies.

**Note on Render & JSTL/Servlets:** Platforms like Render typically expect a self-contained service and do not have a "Java Servlet" environment out-of-box. By using Docker, you package a servlet container (like Tomcat) with your application, which Render can then run. Our `Dockerfile` uses **Tomcat 9.0** as the base image for deployment.

#### Understanding the `Dockerfile`

```dockerfile
# --- Stage 1: Build the Application ---
FROM openjdk:17-jdk-slim AS build

# Install Maven in the build stage
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the WAR file. The output will be 'target/studentrepo.war'
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
# Use a Tomcat 9.0 image with JDK 17
FROM tomcat:9.0-jdk17-openjdk-slim

WORKDIR /usr/local/tomcat/webapps/

# Copy the WAR file from the 'build' stage and RENAME it to ROOT.war
# Tomcat automatically serves ROOT.war at the root context (e.g., http://your-app-url.com/).
COPY --from=build /app/target/studentrepo.war ROOT.war

# Expose the default Tomcat port
EXPOSE 8080

# Command to run Tomcat when the container starts
CMD ["catalina.sh", "run"]
```


#### Steps to Deploy with Docker:

1.  **Ensure your Java code reads database credentials from environment variables.**
    For Docker deployments, you **must** configure your Java application to read database connection details (host, user, password) from environment variables (e.g., `System.getenv("DB_HOST")`, `System.getenv("DB_USER")`, `System.getenv("DB_PASSWORD")`) instead of directly from `db.properties` or similar files. This is a standard and secure practice for containerized applications.


2.  **Build the Docker image:**
    Make sure you are in the root directory of your project (where the `Dockerfile` is located).

    ```bash
    docker build -t student-performance-tracker .
    ```

    (You can replace `student-performance-tracker` with any name you prefer for your image.)


3.  **Run the container locally (for testing):**
    This command runs your container and maps the container's port 8080 to your local machine's port 8080. You'll need to pass your database credentials as environment variables.

    ```bash
    docker run -p 8080:8080 \
      -e DB_HOST='your_mysql_host' \
      -e DB_USER='your_mysql_user' \
      -e DB_PASSWORD='your_mysql_password' \
      student-performance-tracker
    ```

    Replace `your_mysql_host`, `your_mysql_user`, and `your_mysql_password` with your actual local MySQL credentials. After running, access your application at `http://localhost:8080/`.


## 📄 License
   This project is licensed under the MIT License - see the [LICENSE.md] file for details.

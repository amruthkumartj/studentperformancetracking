
# Aegis Academic Suite: AI-Powered Academic Management

An intelligent, self-hosted web application engineered with a pure Java backend (Servlets & JSP). **Aegis Academic Suite** provides a secure, robust, and multi-user environment for educational institutions to manage student performance, departmental data, and academic schedules with unparalleled efficiency.

This project integrates a **Gemini-powered AI assistant** to empower faculty, offers a modern and responsive user interface built with Material Design 3, and is fully containerized with **Docker** for seamless deployment on any cloud platform.

### ✨ Key Features Overview

#### 🌐 Multi-Platform & Personalized Dashboards

  * **👑 Admin Portal:** A central command center to manage the entire user lifecycle, approve faculty, assign departmental roles, and broadcast secure messages.
  * **🧑‍🏫 Faculty Workspace:** An intuitive interface for faculty to manage assigned students, handle attendance and marks, schedule classes, and leverage the AI assistant.
  * **🎓 Student Gateway:** A personal dashboard for students to track their academic progress, view attendance, and stay updated on events.
  * **📱 Responsive by Design:** The UI offers a seamless and consistent experience on any device, from desktops to mobile phones.

#### 🤖 Intelligent & Automated Tools

  * **🧠 AI-Powered Assistant:** A built-in Gemini assistant helps faculty generate performance summaries, draft emails, and gain insights from academic data.
  * **⏰ Timed Attendance System:** Automates attendance taking with 15-minute timed sessions, quick-marking tools, and intelligent defaults.
  * **📊 Performance Analytics:** In-depth visualization tools and automated analysis to track both individual and class-wide academic trends.
  * **📅 Advanced Scheduling:** A comprehensive module for managing weekly timetables and scheduling extra classes with built-in conflict detection.

#### 🔒 Enterprise-Grade Security

  * **🛡️ Robust Authentication:** Features strong **BCrypt password hashing** and a secure, OTP-based password reset flow to protect user accounts.
  * **🔐 Role-Based Access Control (RBAC):** A strict permissions system ensures that users can only access the data and functionalities relevant to their specific role.
  * **🚦 Filter-Based Session Management:** Secure servlet filters protect all sensitive routes, preventing unauthorized access to critical data and actions.

-----

### Detailed Role-Based Functionalities

#### 👑 Admin

  * **Centralized User Management:** Securely manage the entire user lifecycle. Add, view, edit, and delete student and faculty records with ease.
  * **Faculty Onboarding & Approval:** Review and approve or deny new faculty registration requests, ensuring only authorized personnel access the system.
  * **Role & Program Allocation:** Assign specific programs (e.g., BCA, MCA) to faculty members, enforcing strict access control so they can only manage relevant students.
  * **Data Integrity Controls:** The system actively prevents duplicate student entries by validating unique IDs, email addresses, and phone numbers upon creation.
  * **Secure Broadcast System:** Send important announcements, messages, and updates to all or specific groups of faculty members.

#### 🧑‍🏫 Faculty

  * **🤖 AI-Powered Faculty Assistant:** A built-in assistant powered by the **Google Gemini API** to enhance productivity. Faculty can:
      * Generate summaries of student performance.
      * Draft emails to students or parents.
      * Get insights and analyze course data.
  * **Advanced Scheduling & Timetabling:**
      * **Weekly Timetable:** A clear, organized visual interface to view the entire weekly schedule for any program and semester.
      * **Extra Class Management:** Schedule single or recurring daily classes with built-in conflict detection against the main timetable.
  * **Robust Attendance System:**
      * **Timed Sessions:** Initiate attendance sessions that automatically lock and expire after 15 minutes to ensure timely data entry.
      * **Efficient Marking:** Utilize quick-marking features like "Mark All as Present/Absent" for large classes.
      * **Default Status:** Unmarked students automatically default to 'Present' upon session expiry to prevent incomplete records.
  * **Comprehensive Marks Management:**
      * Enter, update, and manage marks for various exam types (e.g., Internal Assessment, SEE).
      * The system prevents duplicate mark entries for the same student/exam and allows for easy correction of errors.
  * **📊 In-Depth Performance Analytics:**
      * Visualize class-wide performance with dynamic charts and detailed data tables to identify academic trends.
      * Drill down into individual student reports, viewing detailed breakdowns of marks and attendance percentages.
      * Receive an automated performance analysis summary (e.g., "Excellent," "Good," "Needs Improvement").
  * **Event Coordination:** Schedule, view, and manage departmental or college-wide events like seminars and guest lectures.

#### 🎓 Student

  * **Secure Onboarding:** Students can only register and log in if their unique Student ID has been pre-loaded into the system by an administrator, preventing unauthorized access.
  * **Personalized Performance Dashboard:** A dedicated dashboard to track personal marks across all subjects and semesters.
  * **Real-time Attendance Monitoring:** View personal attendance percentages for each course to stay on track with academic requirements.
  * **Event Calendar:** View a list of upcoming college and departmental events.
  * **Self-Service Profile Management:** Easily manage personal profile information and update login credentials.

### 🔒 Security & Architecture

  * **Strong Password Hashing:** All user passwords are secured using **BCrypt (12-factor hashing)**, making them computationally expensive to crack and unreadable even by database administrators.
  * **Secure Authentication & Session Management:** Implemented with robust servlet filters to protect all sensitive routes and ensure only authenticated and authorized users can access specific resources.
  * **OTP-Based Password Reset:** A secure "Forgot Password" flow using One-Time Passwords sent via email to verify user identity.
  * **Modern & Responsive UI:** Built with **Material Design 3**, the interface is clean, intuitive, and fully responsive for a seamless experience on both desktop and mobile devices.
  * **User Experience:** Features a user-friendly Dark/Light mode toggle and intuitive navigation.

### 🛠️ Tech Stack

  * **Backend:** Java (Servlets, JSP)
  * **Database:** MySQL
  * **AI Integration:** Google Cloud Vertex AI (Gemini API)
  * **Build Tool:** Apache Maven
  * **Password Hashing:** BCrypt
  * **Deployment:** Docker, Apache Tomcat
  * **Frontend:** HTML, Material Design 3 CSS, JavaScript

-----

### 🚀 Getting Started

Follow these instructions to get a local copy up and running for development and testing.

#### Prerequisites

  * Java Development Kit (JDK) 17 or newer
  * Apache Maven
  * MySQL Server
  * Docker

#### Installation & Setup

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/amruthkumartj/studentperformancetracking.git
    cd studentperformancetracking/studentrepo
    ```

2.  **Database Configuration:**

      * Create a new MySQL database (e.g., `stud`).
      * Import the database schema from the `.sql` file provided in the repository.
      * For local development, update the database credentials directly in the `DBUtil.java` file.

3.  **Build the project with Maven:**
    This command compiles the source code and packages the application into a `.war` file in the `target/` directory.

    ```bash
    mvn clean install
    ```

-----

### 🐳 Docker Deployment

Containerizing the application with Docker is the recommended way to deploy it to any cloud platform.

#### Understanding the `Dockerfile`

The `Dockerfile` uses a multi-stage build to create a lean and efficient final image:

1.  **Build Stage:** An `openjdk:17-jdk-slim` image and Maven are used to build the `.war` file from the source.
2.  **Run Stage:** A `tomcat:9.0-jdk17-openjdk-slim` image is used for the final container. The `.war` file is copied into Tomcat's `webapps` directory as `ROOT.war`, making the app accessible at the root URL (`/`).

#### Steps to Deploy with Docker:

1.  **Code Configuration for Cloud Deployment:**
    Ensure your `DBUtil.java` is configured to read database credentials from **environment variables**. This is a critical security practice for containerized applications.

2.  **Build the Docker image:**
    From the root directory of the `studentrepo` project, run:

    ```bash
    docker build -t student-performance-tracker .
    ```

3.  **Run the container locally for testing:**
    This command runs the container and maps the container's port 8080 to your local machine's port 8080. You must pass your database and AI API credentials as environment variables.

    ```bash
    docker run -p 8080:8080 \
      -e AIVEN_MYSQL_HOST='your_mysql_host' \
      -e AIVEN_MYSQL_PORT='your_mysql_port' \
      -e AIVEN_MYSQL_DATABASE='your_database_name' \
      -e AIVEN_MYSQL_USERNAME='your_mysql_user' \
      -e AIVEN_MYSQL_PASSWORD='your_mysql_password' \
      -e GEMINI_API_KEY='your_gemini_api_key' \
      student-performance-tracker
    ```

    You can now access your application at `http://localhost:8080/`.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

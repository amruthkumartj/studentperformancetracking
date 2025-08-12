<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
%>
<%@ page isELIgnored="false" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Faculty Dashboard</title>
    
     <script>
        // --- AGGRESSIVE BACK BUTTON & FORWARD BUTTON CONTROL ---

        // 1. On load, explicitly go "forward" in history. This helps create a state
        // that makes the back button's target the page itself.
        window.onload = function () {
            history.pushState(null, "", location.href);
            history.forward(); 
        };
        
        // 2. Add the listener for back-clicks.
        window.addEventListener('popstate', function (event) {
            // The user clicked "back". Instead of just pushing a new state,
            // we forcefully tell the browser to go forward again.
            history.forward();
            
            // Show the alert after canceling the action.
            alert("You cannot go back from the dashboard. Please use the Logout button to exit.");
        });

        // 3. This listener for the bfcache remains essential.
        window.addEventListener('pageshow', function (event) {
            if (event.persisted) {
                window.location.reload();
            }
        });
    </script>
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" />
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/css/sidebar.css" />

<script>


    // Pass data from JSP to JavaScript
    window.currentFacultyId = "<c:out value='${sessionScope.userId}'/>";
    window.currentUserRole = "<c:out value='${sessionScope.userRole}'/>";
    
    // These are for other features and are fine
    window.allProgramsData = JSON.parse('<c:out value="${requestScope.allProgramsJson}" escapeXml="false" default="[]" />');
    window.assignedProgramIdsData = JSON.parse('<c:out value="${requestScope.assignedProgramIdsJson}" escapeXml="false" default="[]" />');
    
</script>

<style>
/* =================================================================== */
/* ========== 1. GENERAL & REUSABLE COMPONENT STYLES ========== */
/* =================================================================== */

/* General Button Styles */
.btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    padding: 0.6rem 1.2rem;
    font-size: 0.95rem;
    border-radius: 6px;
    transition: all 0.2s ease;
    text-decoration: none;
    justify-content: center;
    cursor: pointer;
    border: 1px solid transparent;
}
.btn-primary {
    color: white;
    background-color: var(--primary-color);
    border-color: var(--primary-color);
}
.btn-primary:hover {
    filter: brightness(1.1);
    transform: translateY(-1px);
}
.btn-secondary {
    color: var(--text-color);
    background-color: #e9ecef;
    border-color: #ddd;
}
.btn-secondary:hover {
    background-color: #dcdfe2;
}

/* Action Buttons in Tables (Edit/Delete) */
.btn-action {
    padding: 5px 10px;
    font-size: 14px;
    border-radius: 5px;
    color: #fff;
    border: none;
    cursor: pointer;
    transition: background-color 0.2s;
    margin: 0 2px;
}
.btn-action i {
    vertical-align: middle;
}
.btn-action.edit { background-color: #ffc107; }
.btn-action.edit:hover { background-color: #e0a800; }
.btn-action.delete { background-color: #dc3545; }
.btn-action.delete:hover { background-color: #c82333; }

</style>

</head>
<body class="${sessionScope.theme}">

<nav class="sidebar close">
    <header>
    <div class="image-text">
        <span class="image">
            <i class='bx bxs-school icon'></i>
        </span>

        <div class="text logo-text">
            <span class="name">NHCE</span>
            <span class="profession">Faculty Portal</span>
        </div>
    </div>

    <i class='bx bx-chevron-right toggle'></i>
</header>

    <div class="menu-bar">
        <div class="menu">
            <ul class="menu-links">
                <li class="nav-link">
    <a href="javascript:void(0);" id="openSearchModalBtn">
        <i class='bx bx-search icon'></i>
        <span class="text nav-text">Search</span>
    </a>
</li>
                <li class="nav-link">
                    <a href="javascript:void(0);" id="dashboardNavLink" >
                        <i class='bx bx-home-alt icon'></i>
                        <span class="text nav-text">Dashboard</span>
                    </a>
                </li>
   				<li class="nav-link has-dropdown">
                    <a href="#" class="dropdown-toggle">
                        <i class='bx bxs-graduation icon'></i>
                        <span class="text nav-text">Students</span>
                        <i class='bx bx-chevron-down dropdown-arrow'></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <a href="<c:url value='viewStudentPerformance.jsp' />">
                                <i class='bx bx-bar-chart-alt icon'></i>
                                <span class="text">View Student Performance</span>
                            </a>
                        </li>
                        <li>
                            <a href="javascript:void(0);" id="manageStudentsLink">
                                <i class='bx bx-cog icon'></i>
                                <span class="text">Manage Students</span>
                            </a>
                        </li>
                    </ul>
                </li>
                <li class="nav-link">
                    <a href="javascript:void(0);" id="enterMarksNavLink">
                        <i class='bx bx-pencil icon'></i>
                        <span class="text nav-text">Enter Marks</span>
                    </a>
                </li>
                <li class="nav-link">
    				<a href="javascript:void(0);" id="attendanceNavLink">
        			<i class='bx bx-check-square icon'></i>
        			<span class="text nav-text">Attendance</span>
    				</a>
				</li>
                <li class="nav-link has-dropdown">
                    <a href="#" class="dropdown-toggle">
                        <i class='bx bx-calendar icon'></i>
                        <span class="text nav-text">Schedule</span>
                        <i class='bx bx-chevron-down dropdown-arrow'></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <a href="javascript:void(0);" id="manageScheduleLink">
                                <i class='bx bx-list-ul icon'></i>
                                <span class="text">Manage Schedule</span>
                            </a>
                        </li>
                        <li>
    <a href="javascript:void(0);" id="hostEventsLink">
        <i class='bx bx-calendar-star icon'></i>
        <span class="text">Event Management</span>
    </a>
</li>
                    </ul>
                </li>
                <li class="nav-link">
    <a href="javascript:void(0);" id="aiAssistantNavLink">
        <i class='bx bxs-brain icon'></i>
        <span class="text nav-text">AI Assistant</span>
    </a>
</li>
                <li class="nav-link">
    				<a href="javascript:void(0);" id="profileNavLink">
        				<i class='bx bx-user icon'></i>
       					<span class="text nav-text">Profile</span>
    				</a>
				</li>
            </ul>
        </div>

        <div class="bottom-content">
            <ul class="menu-links">
                <li class="">
    <a href="<c:url value='/logout' />">
        <i class='bx bx-log-out icon'></i>
        <span class="text nav-text">Logout</span>
    </a>
</li>
                <li class="mode">
                    <div class="sun-moon">
                        <i class='bx bx-moon icon moon'></i>
                        <i class='bx bx-sun icon sun'></i>
                    </div>
                    <span class="mode-text text">Dark mode</span>
                    <div class="toggle-switch">
                        <span class="switch"></span>
                    </div>
                </li>
            </ul>
        </div>
    </div>
</nav>

<section class="home">
        <i class='bx bx-menu mobile-toggle'></i>

        <div class="text">Welcome, <c:out value="${sessionScope.user.role}" />!</div>
        </section>
<div class="home">
    <div class="main-content">
        <div id="dashboardSection">
            <h2 class="text page-title">Welcome, <c:out value="${sessionScope.user.username}" />!</h2>


            <div class="dashboard-widgets">
                <div class="card">
                    <i class='bx bx-group' style="font-size:36px;color:var(--primary);margin-bottom:10px"></i>
                    <h3>Total Students (Overall)</h3>
                    <p class="counter" data-target="<c:out value='${requestScope.totalOverallStudents}' default='0'/>">
                        <c:out value="${requestScope.totalOverallStudents}" default="0"/>
                    </p>
                </div>

                <div class="card">
                    <i class='bx bx-book' style="font-size:36px;color:var(--primary);margin-bottom:10px"></i>
                    <h3>Courses Assigned (Overall)</h3>
                    <p class="counter" data-target="<c:out value='${requestScope.totalAssignedCourses}' default='0'/>">
                        <c:out value="${requestScope.totalAssignedCourses}" default="0"/>
                    </p>
                </div>

               <!-- FINAL VERSION: Compact AI Assistant Card -->
<div class="card" id="ai-assistant-card">
    <button class="ai-settings-btn"><i class='bx bx-cog'></i></button>
    <div class="ai-card-main-content">
        <i class='bx bxs-brain icon'></i>
        <h3>Faculty Assistant</h3>
        <p class="ai-card-desc">AI Powered Service</p>
        <button class="ai-launch-btn-circle">
            <i class='bx bx-right-arrow-alt'></i>
        </button>
    </div>
    <div class="ai-card-settings-panel">
        <!-- The settings panel code remains the same -->
        <div class="settings-panel-header">
            <button class="ai-settings-close-btn"><i class='bx bx-arrow-back'></i></button>
            <h4>Settings</h4>
        </div>
        <div class="setting-row">
            <span>Show Floating Icon</span>
            <label class="switch-toggle">
               <input type="checkbox" id="enable-global-ai-toggle">
               <span class="slider-toggle"></span>
            </label>
        </div>
    </div>
</div>

                <c:if test="${sessionScope.user.role == 'ADMIN'}">
                    <div class="card admin-approval-widget-card">
                        <i class='bx bx-shield-alt icon'></i>
                        <h3>Approve Faculty</h3>
                        <a href="<c:url value='/admin/approveFaculty' />">
                            View & Approve
                        </a>
                    </div>
                </c:if>
            </div>

            <div class="assigned-programs-section" style="margin-top: 30px;">
                <h2 class="text page-title" style="font-size: 1.5rem; margin-bottom: 20px;">Assigned Programs</h2>
                
                <div class="dashboard-widgets">
                    <c:choose>
                        <c:when test="${not empty requestScope.assignedProgramsWithCounts}">
                            <c:forEach var="entry" items="${requestScope.assignedProgramsWithCounts}">
                                <div class="card">
                                    <i class='bx bx-chalkboard' style="font-size:36px;color:var(--primary);margin-bottom:10px;"></i>
                                    <h3><c:out value="${entry.key}" /></h3>
                                    <p>
                                        <strong><c:out value="${entry.value}" /></strong> Students
                                    </p>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                             <div class="card" style="width: 100%; text-align: center;">
                                <i class='bx bx-info-circle' style="font-size:36px;color:var(--text-color);margin-bottom:10px;"></i>
                                <p style="font-style: italic;">No programs currently assigned or no students are enrolled.</p>
                             </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            </div>
           <section id="aiAssistantSection" style="display: none;">
    <div class="ai-section-header">
        <h2 class="text page-title">Faculty Assistant</h2>
        <button class="ai-section-settings-btn"><i class='bx bx-cog'></i> Settings</button>
    </div>
    
    <div class="ai-chat-container">
        <div class="ai-chat-history" id="ai-chat-history">
            <div class="message assistant">
                <i class='bx bxs-brain'></i>
                <p>Welcome! How can I help you? You can ask me to summarize student performance, draft emails, or analyze course data.</p>
            </div>
        </div>
        <div class="ai-chat-input-area">
            <input type="text" id="ai-chat-input" placeholder="Ask a question or give a command... (e.g., 'Summarize performance for student 123')">
            <button id="ai-chat-send-btn"><i class='bx bxs-send'></i></button>
        </div>
    </div>
</section>

        <section id="studentFeatures" style="display:none;">
        <h2 class="text page-title">Manage Students</h2>

        <div class="feature-card-wrap">
            <div class="feature-card" onclick="showStudentFeature('add')">➕ Add&nbsp;Student</div>
            <div class="feature-card" onclick="showStudentFeature('view')">📋 View&nbsp;All</div>
            <div class="feature-card" onclick="showStudentFeature('edit')">✏️ Edit&nbsp;Info</div>
            <div class="feature-card" onclick="showStudentFeature('delete')">🗑️ Delete</div>
            <div class="feature-card" onclick="showStudentFeature('search')">🔍 Search&nbsp;/&nbsp;Filter</div>
        </div>

         <div id="addStudentCard" class="form-card" style="display:none;">
            <h3 style="color:#007bff;margin-bottom:20px;">Add New Student</h3>

            <div id="addStudentMessage" style="display:none;"></div>

            <form id="addStudentForm">
                <div class="form-group">
                    <label for="programIdAdd">Select Program</label>
                    <select id="programIdAdd" name="programId" required>
                        <option value="" selected disabled>-- choose program --</option>
                    </select>
                </div>

                <div id="studentFields" style="display:none; flex-direction: row; flex-wrap: wrap; gap: 20px; width: 100%;">
                    <div class="form-group">
                        <label for="studentId">Student ID</label>
                        <input type="number" min = "0" max="9999999" id="studentId" name="studentId" required>
                    </div>

                    <div class="form-group">
                        <label for="name">Full Name</label>
                        <input type="text" id="name" name="fullName" required>
                    </div>

                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" required>
                    </div>

                    <div class="form-group">
                        <label for="phone">Phone Number</label>
                        <input type="tel" id="phone" name="phone" required>
                    </div>

                    <div class="form-group">
                        <label for="semester">Semester</label>
                        <input type="number" id="semester" name="semester" min="1" max="6" required>
                    </div>

                    <div class="form-buttons">
                        <button type="submit">Add Student</button>
                        <button type="button" id="cancelAddBtn">Cancel</button>
                    </div>
                </div>
            </form>
        </div>

      <div id="viewStudentSection" style="display:none;">
        <button id="backToFeaturesBtn" class="btn btn-primary" style="margin-bottom: 25px;">Back to Features</button>

        <div class="table-filters">
            <div class="form-group">
                <label for="studentSearchInput">Search Students (ID, Name, Email, Phone)</label>
                <input type="text" id="studentSearchInput" placeholder="Type to search...">
            </div>

            <div class="form-group">
                <label for="programFilterDropdown">Filter by Program</label>
                <select id="programFilterDropdown">
                    <option value="">All Programs</option>
                </select>
            </div>
            <button id="resetFiltersBtn">Reset Filters</button>
        </div>

        <div class="table-responsive">
        <table id="studentsTable">
         <thead>
            <tr >
                <th data-sort-col="0" data-sort-dir="asc" >
                    Student ID &nbsp;&nbsp;<span class="fa-solid fa-sort sort-icon"></span>
                </th>
                <th>Name</th>
                <th>Program</th>
                <th data-sort-col="3" data-sort-dir="asc" >
                    Semester <span class="fa-solid fa-sort sort-icon"></span>
                </th>
                <th>Email</th>
                <th>Phone</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody  id="studentsTableBody">
        </tbody>
    </table>
    </div>
        <div id="noStudentsFilteredMessage" style="display:none;">No students match the current filters.</div>
    </div>

    <div id="todoPlaceholder" style="display:none;margin-top:30px;font-style:italic;color:grey"></div>
    </section>


    <section id="attendanceManagementSection" style="display:none;">
      <h2 class="text page-title">Attendance Management</h2>

      <div id="attendanceFeatureCards" class="feature-card-wrap">
          <div class="feature-card" onclick="showAttendanceAction('take')">
              📝 Take&nbsp;Attendance
          </div>
          <div class="feature-card" onclick="showAttendanceAction('view')">
              📊 View&nbsp;Attendance
          </div>
      </div>

    <div id="takeAttendanceSection" class="form-card" style="display:none;">

      <h3 style="color:#007bff;margin-bottom:20px;">Take Attendance</h3>

      <div class="form-group">
          <label for="attDateTime">Date & Time (auto)</label>
          <input id="attDateTime" type="text" readonly>
      </div>

      <div class="form-group">
          <label for="programSelect">Program</label>
          <select id="programSelect" required>
              <option value="" disabled selected>-- choose program --</option>
              </select>
      </div>

      <div class="form-group">
          <label for="semesterSelect">Semester</label>
          <select id="semesterSelect" disabled required>
              <option value="" disabled selected>-- choose semester --</option>
              <option>1</option><option>2</option><option>3</option>
              <option>4</option><option>5</option><option>6</option>
          </select>
      </div>

      <div class="form-group">
          <label for="subjectSelect">Subject</label>
          <select id="subjectSelect" disabled required>
              <option value="" disabled selected>-- choose subject --</option>
              </select>
      </div>

      <div class="form-buttons">
          <button id="startAttendanceSessionBtn">Take Attendance</button>
          <button type="button" id="cancelTakeAttendanceBtn" >Back</button>
      </div>
    </div>


      <div id="viewAttendanceSection" style="display:none;">
        <h3 style="color:#007bff;margin-bottom:20px;">View Attendance Records</h3>
        <button onclick="showAttendanceAction('')" class="btn btn-primary" style="margin-bottom: 25px;">Back to Features</button>

        <div class="table-filters">

            <div class="form-group">
                <label for="attProgramFilter">Filter by Program</label>
                <select id="attProgramFilter">
                    <option value="">All Programs</option>
                    </select>
            </div>

            <div class="form-group">
                <label for="attSemesterFilter">Filter by Semester</label>
                <select id="attSemesterFilter">
                    <option value="">All Semesters</option>
                    </select>
            </div>

            <div class="form-group">
                <label for="attSubjectFilter">Filter by Subject</label>
                <select id="attSubjectFilter">
                    <option value="">All Subjects</option>
                    </select>
            </div>

            <div class="form-group">
                <label for="attDateFilter">Filter by Date</label>
                <input type="date" id="attDateFilter">
            </div>

            <div class="form-group">
                <label for="attStudentSearchInput">Search by Student (ID or Name)</label>
                <input type="text" id="attStudentSearchInput" placeholder="Enter student name or ID...">
            </div>

            <button id="resetAttendanceFiltersBtn">Reset</button>
        </div>

        <div class="table-responsive">
            <table id="attendanceRecordsTable">
                <thead>
                    <tr>
                        <th>Student ID</th>
                        <th>Student Name</th>
                        <th>Date</th>
                        <th>Subject</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody id="attendanceRecordsTableBody">
                    <tr>
                        <td colspan="6" style="text-align:center;padding:20px;">No records to display. Use filters to load data.</td>
                    </tr>
                </tbody>
            </table>
        </div>

      </div>
      </section>

      <section id="marksManagementSection" style="display:none;">
          <h2 class="text page-title">Enter Marks</h2>

          <div id="marksFeatureCards" class="feature-card-wrap">
              <div class="feature-card" onclick="window.location.href='<%= request.getContextPath() %>/enterCourseMarks.jsp'">
                  <i class='bx bx-book-add' style="font-size:36px;color:var(--primary);margin-bottom:10px;"></i>
                  <p>Course-wise Entry</p>
              </div>
              <div class="feature-card" onclick="showMarksAction('student')">
                  <i class='bx bx-user-plus' style="font-size:36px;color:var(--primary);margin-bottom:10px;"></i>
                  <p>Student-wise Entry</p>
              </div>
          </div>

          <div id="enterMarksStudentSection" class="form-card" style="display:none;">
              <h3 style="color:#007bff;margin-bottom:20px;">Enter Marks for a Specific Student</h3>
              <button onclick="showMarksAction('')" class="btn-primary" style="margin-bottom: 15px;">Back to Options</button>
              <p>This feature will be implemented later. It would allow you to search for a student and then view/enter marks for them across different courses/exam types.</p>
              </div>

      </section>
      
      <section id="scheduleLandingSection" style="display: none;">
          <h2 class="text page-title">Manage Schedule</h2>
          <div class="feature-card-wrap">
              <div class="feature-card" id="addExtraClassFeatureLink">
                  <i class='bx bx-calendar-plus icon'></i>
                  <p>Add Extra Class</p>
              </div>
              <div class="feature-card" id="viewWeeklyTimetableFeatureLink">
                  <i class='bx bx-list-ul icon'></i>
                  <p>View Weekly Timetable</p>
              </div>
          </div>
      </section>

      <section class="home-section" id="dailyClassScheduleSection" style="display: none;">
        <h2 class="text page-title">Add Extra Class</h2>
        <button id="backToScheduleOptionsBtn" class="btn btn-primary" style="margin-bottom: 25px;">Back</button>
        <div class="schedule-layout-container">
            <div class="schedule-form-container">
                <form id="dailyClassForm">
                    <div class="form-group">
                        <label for="scheduleProgramSelect">Select Program</label>
                        <select id="scheduleProgramSelect" name="programId" required>
                            <option value="" selected disabled>-- choose program --</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="scheduleSemesterSelect">Select Semester</label>
                        <select id="scheduleSemesterSelect" name="semester" disabled required>
                            <option value="" disabled selected>-- choose semester --</option>
                            <option>1</option><option>2</option><option>3</option><option>4</option><option>5</option><option>6</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="scheduleTopicInput">Topic for the Extra Class</label>
                        <input type="text" id="scheduleTopicInput" name="subjectName" required minlength="7" placeholder="e.g., Exception Handling">
                    </div>
                    <div class="form-group">
                        <label for="scheduleDateInput">Start Date of Class</label>
                        <input type="date" id="scheduleDateInput" name="classDate" required>
                    </div>
                    <div class="form-group">
                        <label for="scheduleDayDisplay">Day of Week</label>
                        <input type="text" id="scheduleDayDisplay" readonly style="background-color: #eee; cursor: not-allowed;">
                    </div>
                    <div class="form-group">
                        <label for="scheduleTimeInput">Time of Class</label>
                        <input type="time" id="scheduleTimeInput" name="classTime" required>
                    </div>
                    <div class="form-group">
                        <label for="scheduleDurationInput">Duration (in minutes)</label>
                        <input type="number" id="scheduleDurationInput" name="durationMinutes" 
                               min="15" max="240" value="60" required>
                    </div>
                    <div class="form-group">
                        <label for="scheduleRoomInput">Room/Location</label>
                        <input type="text" id="scheduleRoomInput" name="roomLocation" placeholder="e.g., Block A, Lab 402" required>
                    </div>
                    <div class="form-group" style="flex-direction: row; align-items: center; gap: 10px;">
                        <input type="checkbox" id="scheduleRecurrenceCheckbox" name="isRecurring" style="width: auto;">
                        <label for="scheduleRecurrenceCheckbox">Repeat this class daily?</label>
                    </div>
                    <div id="recurrenceOptions" style="display:none;">
                        <div class="form-group">
                            <label for="scheduleEndDateInput">End Date (for recurrence)</label>
                            <input type="date" id="scheduleEndDateInput" name="endDate">
                        </div>
                    </div>
                    <div class="form-buttons">
                        <button type="submit">Schedule Class</button>
                        <button type="button" id="cancelDailyScheduleBtn">Cancel</button>
                    </div>
                </form>
            </div>
            <div class="schedule-table-container">
                <div class="table-wrapper">
                    <h4 style="color:var(--text-color); margin-bottom:15px;">Regular Timetable for Selected Day</h4>
                    <div class="table-responsive">
                        <table id="regularTimetable" class="styled-table">
                            <thead>
                                <tr>
                                    <th>Time</th>
                                    <th>Subject</th>
                                    <th>Location</th>
                                </tr>
                            </thead>
                            <tbody id="regularTimetableBody">
                                <tr><td colspan="3" style="text-align:center;">Select program, semester, and date to view schedule.</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="table-wrapper" style="margin-top: 30px;">
                    <h4 style="color:var(--text-color); margin-bottom:15px;">Existing Extra Classes for Selected Day</h4>
                    <div class="table-responsive">
                        <table id="extraClassesTable" class="styled-table">
                            <thead>
                                <tr>
                                    <th>Time</th>
                                    <th>Topic</th>
                                    <th>Location</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody id="extraClassesTableBody">
                                <tr><td colspan="4" style="text-align:center;">Select program, semester, and date to view schedule.</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </section>

   <section id="viewTimetableSection" style="display: none;">
    <h2 class="text page-title">View Weekly Timetable</h2>
    <button id="backToScheduleOptionsBtn2" class="btn btn-primary" style="margin-bottom: 25px;">Back</button>

    <div class="table-filters">
        <div class="form-group">
            <label for="timetableProgramSelect">Select Program</label>
            <select id="timetableProgramSelect">
                <option value="">-- choose program --</option>
            </select>
        </div>
        <div class="form-group">
            <label for="timetableSemesterSelect">Select Semester</label>
            <select id="timetableSemesterSelect" disabled>
                <option value="">-- choose semester --</option>
                <option>1</option><option>2</option><option>3</option><option>4</option><option>5</option><option>6</option>
            </select>
        </div>
        <button id="resetTimetableFiltersBtn" class="btn-secondary">Reset</button>
    </div>

    <div id="timetableDisplay" class="weekly-timetable-display">
        <p class="initial-message">Please select a program and semester.</p>
    </div>
</section>
<section id="eventManagementSection" style="display: none;">
    <h2 class="text page-title">Event Management</h2>

    <div id="eventFeatureCards" class="feature-card-wrap">
        <div class="feature-card" onclick="window.location.href='Hostevents.jsp'">
            <i class='bx bx-calendar-plus icon'></i>
            <p>Host New Event</p>
        </div>
        <div class="feature-card" onclick="showEventManagementFeature('view')">
            <i class='bx bx-list-check icon'></i>
            <p>View & Manage Events</p>
        </div>
    </div>

    <div id="viewAndManageEventsSection" style="display:none;">
       <button onclick="showEventManagementFeature('')" class="btn btn-primary" style="margin-bottom: 25px;">
            <i class='bx bx-arrow-back'></i> Back to Options
        </button>

        <div class="table-filters">
            <div class="form-group">
                <label for="eventSearchInput">Search by Event Name</label>
                <input type="text" id="eventSearchInput" placeholder="Type to search...">
            </div>
            <div class="form-group">
                <label for="eventTypeFilter">Filter by Type</label>
                <select id="eventTypeFilter">
                    <option value="">All Types</option>
                    <option>College Event</option>
                    <option>Fest</option>
                    <option>Department Based</option>
                    <option>National</option>
                    <option>Sports</option>
                    <option>Other</option>
                </select>
            </div>
            <button id="resetEventFiltersBtn" class="btn-secondary">Reset</button>
        </div>

        <div class="table-responsive">
            <table id="eventsTable" class="styled-table">
                <thead>
                    <tr>
                        <th>Event Name</th>
                        <th>Event Type</th>
                        <th>Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody id="eventsTableBody">
                    </tbody>
            </table>
        </div>
        <div id="noEventsMessage" style="display:none; text-align:center; padding: 20px; color: #888;">
            No events match the current filters.
        </div>
    </div>
</section>
 <section class="home-section" id="profileSection" style="display: none;">
    <div class="home-content" 
         data-username="<c:out value='${requestScope.profileDetails.username}'/>"
         data-email="<c:out value='${requestScope.profileDetails.email}'/>"
         data-role="<c:out value='${sessionScope.userRole}'/>"
         data-id="<c:out value='${requestScope.profileDetails.facultyId}'/>"
         data-phone="<c:out value='${requestScope.profileDetails.phone}'/>">

        <h2 class="section-title">Faculty Profile</h2>

        <div class="profile-header-card">
            <div class="profile-header-avatar">
                <i class='bx bxs-user-circle'></i>
            </div>
            <div class="profile-header-info">
                <h3 id="profileFacultyName" class="faculty-name">Loading...</h3>
                <p class="faculty-id-display">Faculty ID: <span id="profileFacultyId">Loading...</span></p>
            </div>
        </div>

        <div class="profile-widget-grid">
            <div class="profile-widget">
                <h4 class="widget-title">Contact Information</h4>
                <div class="widget-content">
                    <div class="info-row">
                        <i class='bx bx-envelope'></i>
                        <span id="profileFacultyEmail">Loading...</span>
                    </div>
                    <div class="info-row">
                        <i class='bx bx-phone'></i>
                        <span id="profileFacultyPhone">Loading...</span>
                    </div>
                </div>
            </div>
            <div class="profile-widget">
                <h4 class="widget-title">Role & Permissions</h4>
                <div class="widget-content">
                    <div class="info-row">
                        <i class='bx bx-shield-quarter'></i>
                        <span id="profileFacultyRole">Loading...</span>
                    </div>
                </div>
            </div>
            <div class="profile-widget">
                <h4 class="widget-title">Account Security</h4>
                <div class="widget-content">
                   <button class="btn-secondary" id="openChangePasswordBtn">
                       <i class='bx bx-lock-alt'></i> Change Password
                   </button>
                   <div id="changePasswordFormContainer" style="max-height: 0; opacity: 0; overflow: hidden; transition: all 0.5s ease;">
                       <div class="form-group" style="margin-top: 20px;">
                           <label for="newPassword">New Password</label>
                           <input type="password" id="newPassword" class="form-control" placeholder="Enter new password">
                       </div>
                       <div class="form-group">
                           <label for="confirmPassword">Confirm Password</label>
                           <input type="password" id="confirmPassword" class="form-control" placeholder="Confirm new password">
                       </div>
                       <div class="form-buttons">
                           <button class="btn btn-primary" id="savePasswordBtn">Save Changes</button>
                       </div>
                   </div>
                   <div id="passwordChangeMessage" style="margin-top: 15px;"></div>
                </div>
            </div>
        </div>
    </div>
</section>

<div id="searchModal" class="search-modal-overlay" style="display: none;">
    <div class="search-modal-panel">
        <div class="search-modal-input-wrapper">
            <i class='bx bx-search'></i>
            <input type="text" id="modalSearchInput" placeholder="Search for students or commands (e.g., 'add student')...">
            <i class='bx bx-x' id="modalClearBtn"></i>
        </div>
        <div id="modalSearchResults" class="search-modal-results">
            </div>
    </div>
</div>

<script src="<%= request.getContextPath() %>/js/facultymain.js"></script>
<script src="<%= request.getContextPath() %>/js/facultystud.js"></script>
<script src="<%= request.getContextPath() %>/js/theme.js"></script>



    </div>
<button id="global-ai-fab">
        <i class='bx bxs-brain'></i>
    </button>
    
   
<div id="ai-search-modal" class="ai-search-modal">
    <div class="ai-search-panel">
       
<div class="ai-modal-header">
    <h4 class="text">AI Assistant</h4>
    <!-- Add this new wrapper div -->
    <div class="ai-modal-header-actions">
        <!-- The dustbin icon will be added here by JavaScript -->
        <button id="ai-modal-close-btn" title="Close"><i class='bx bx-x'></i></button>
    </div>
</div>

        <!-- Results Area -->
        <div class="ai-search-results" id="ai-modal-results">
            <p class="ai-placeholder">Ask a question to get started.</p>
        </div>

        <!-- Input Area at the bottom with Send Button -->
        <div class="ai-search-input-wrapper">
            <input type="text" id="ai-modal-input" placeholder="Type your question...">
            <button id="ai-modal-send-btn" title="Send">
                <i class='bx bxs-send'></i>
            </button>
        </div>
    </div>
</div>
    
</div>
</body>
</html>
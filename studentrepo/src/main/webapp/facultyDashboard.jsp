<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Faculty Dashboard</title>

    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" />
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/css/sidebar.css" />

   <script>
    // Pass data from JSP to JavaScript
    window.currentFacultyId = "<c:out value='${sessionScope.user.id}'/>";
    window.allProgramsData = JSON.parse('<c:out value="${requestScope.allProgramsJson}" escapeXml="false" default="[]" />');
    window.assignedProgramIdsData = JSON.parse('<c:out value="${requestScope.assignedProgramIdsJson}" escapeXml="false" default="[]" />');
   </script>

</head>
<body>

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
            <!-- Main Menu Links -->
            <ul class="menu-links">
                <!-- NEW: Search button that opens the modal -->
                <li class="nav-link">
                    <a href="#" id="openSearchModalBtn">
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
                <li class="nav-link">
                    <a href="<c:url value='/schedule.jsp' />">
                        <i class='bx bx-calendar icon'></i>
                        <span class="text nav-text">Schedule</span>
                    </a>
                </li>
                <li class="nav-link">
                    <a href="<c:url value='/messages.jsp' />">
                        <i class='bx bx-envelope icon'></i>
                        <span class="text nav-text">Messages</span>
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

        <!-- Correctly placed bottom content -->
        <div class="bottom-content">
            <ul class="menu-links">
                <li class="">
                    <a href="login.html">
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

                <div class="card">
                    <i class='bx bx-envelope' style="font-size:36px;color:var(--primary);margin-bottom:10px"></i>
                    <h3>Messages</h3>
                    <p class="counter" data-target="0">0</p>
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
        <button id="backToFeaturesBtn">Back to Features</button>

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
          <button type="button" id="cancelTakeAttendanceBtn">Back</button>
      </div>
    </div>


      <div id="viewAttendanceSection" style="display:none;">
        <h3 style="color:#007bff;margin-bottom:20px;">View Attendance Records</h3>
        <button onclick="showAttendanceAction('')" class="btn-primary" style="margin-bottom: 15px;">Back to Features</button>

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
      <section class="home-section" id="profileSection" style="display: none;">
    <div class="home-content">
        <h2 class="section-title">Faculty Profile</h2>

        <div class="profile-header-card">
            <div class="profile-header-avatar">
                <i class='bx bxs-user-circle'></i>
            </div>
            <div class="profile-header-info">
                <h3 id="profileFacultyName" class="faculty-name"></h3>
                <p class="faculty-id-display">Faculty ID: <span id="profileFacultyId"></span></p>
            </div>
            <div class="profile-header-action">
                <button class="btn-edit-profile">
                    <i class='bx bx-pencil'></i> Edit Profile
                </button>
            </div>
        </div>

        <div class="profile-widget-grid">

            <div class="profile-widget">
                <h4 class="widget-title">Contact Information</h4>
                <div class="widget-content">
                    <div class="info-row">
                        <i class='bx bx-envelope'></i>
                        <span id="profileFacultyEmail"></span>
                    </div>
                    <div class="info-row">
                        <i class='bx bx-phone'></i>
                        <span>+91 98765 43210 (Example)</span>
                    </div>
                </div>
            </div>

            <div class="profile-widget">
                <h4 class="widget-title">Role & Permissions</h4>
                <div class="widget-content">
                    <div class="info-row">
                        <i class='bx bx-shield-quarter'></i>
                        <span>Faculty Member</span>
                    </div>
                    <div class="info-row">
                        <i class='bx bx-time-five'></i>
                        <span>Joined: 20 July 2025</span>
                    </div>
                </div>
            </div>

            <div class="profile-widget">
                <h4 class="widget-title">Account Security</h4>
                <div class="widget-content">
                   <button class="btn-secondary">
                       <i class='bx bx-lock-alt'></i> Change Password
                   </button>
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
            <!-- Search results will be dynamically inserted here -->
        </div>
    </div>
</div>

<!-- Load JavaScript at the end of the body -->

<script src="<%= request.getContextPath() %>/js/facultymain.js"></script>
<script src="<%= request.getContextPath() %>/js/facultystud.js"></script>


    </div>
</div>

</body>
</html>
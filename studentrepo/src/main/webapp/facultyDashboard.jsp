<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" /> <title>Faculty Dashboard</title>

    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" integrity="sha512-SnH5WK+bZxgPHs44uWIX+LLJAJ9/2PkPKZ5QiAj6Ta86w+fsb2TkcmfRyVX3pBnMFcV7oQPJkl9QevSCWr3W6A==" crossorigin="anonymous" referrerpolicy="no-referrer" />

    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/css/sidebar.css" />
	<script src="js/faculty.js"></script>
</head>

<body>

<nav class="sidebar close">
    <header>
        <div class="image-text">
            <span class="image"></span>
            <div class="text logo-text">
                <span class="profession">Menu</span>
            </div>
        </div>
        <br/>
        <i class='bx bx-chevron-right toggle'></i>
    </header>

    <div class="menu-bar">
        <div class="menu">
            <ul class="menu-links">
                <li class="search-box">
                    <i class='bx bx-search icon'></i>
                    <input type="text" placeholder="Search..." />
                </li>
                <!-- Main navigation links -->
                <li class="nav-link">
                    <a href="facultyDashboard.jsp">
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
            <a href="<%= request.getContextPath() %>/faculty/view-student-performance.jsp">
                <i class='bx bx-bar-chart-alt icon'></i>
                <span class="text">View Student Performance</span>
            </a>
        </li>
        <li>
            <a href="#" id="manageStudentsLink">
                <i class='bx bx-cog icon'></i>
                <span class="text">Manage Students</span>
            </a>
        </li>
    </ul>
</li>
                <li class="nav-link">
                    <a href="<c:url value='/entermarks.jsp' />">
                        <i class='bx bx-pencil icon'></i>
                        <span class="text nav-text">Enter Marks</span>
                    </a>
                </li>
                <li class="nav-link">
    				<a href="#" id="attendanceNavLink">
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
                    <a href="<c:url value='/profile.jsp' />">
                        <i class='bx bx-user icon'></i>
                        <span class="text nav-text">Profile</span>
                    </a>
                </li>
            </ul>
        </div>

        <div class="bottom-content">
            <ul>
                <li>
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

<div class="home">
    <!-- DASHBOARD SECTION -->
    <div id="dashboardSection">
        <h2 class="text" style="font-weight:bold;margin-top:60px">Welcome, Faculty!</h2>

        <div class="dashboard-widgets">
            <div class="card">
                <i class='bx bx-group' style="font-size:36px;color:var(--primary);margin-bottom:10px"></i>
                <h3>Total Students</h3>
                <p class="counter" data-target="<%=request.getAttribute("totalStudents")%>">0</p>
            </div>

            <div class="card">
                <i class='bx bx-book' style="font-size:36px;color:var(--primary);margin-bottom:10px"></i>
                <h3>Courses Assigned</h3>
                <p class="counter" data-target="<%=request.getAttribute("assignedCourses")%>">0</p>
            </div>

            <div class="card">
                <i class='bx bx-envelope' style="font-size:36px;color:var(--primary);margin-bottom:10px"></i>
                <h3>Messages</h3>
                <p class="counter" data-target="<%=request.getAttribute("newMessages")%>">0</p>
            </div>
        </div>
    </div>

    <!-- STUDENT MANAGEMENT SECTION -->
   <section id="studentFeatures" style="display:none;margin-top:70px;margin-left: 50px;">
    <h2 class="text" style="margin-bottom:18px;font-weight: bold; font-size: 30px;">Manage Students</h2>

    <div class="feature-card-wrap" style="display:flex;gap:20px;flex-wrap:wrap">
        <div class="feature-card" onclick="showStudentFeature('add')">➕ Add&nbsp;Student</div>
        <div class="feature-card" onclick="showStudentFeature('view')">📋 View&nbsp;All</div>
        <div class="feature-card" onclick="showStudentFeature('edit')">✏️ Edit&nbsp;Info</div>
        <div class="feature-card" onclick="showStudentFeature('delete')">🗑️ Delete</div>
        <div class="feature-card" onclick="showStudentFeature('search')">🔍 Search&nbsp;/&nbsp;Filter</div>
    </div>

    <div id="addStudentCard" style="display:none;max-width:700px;margin-top:30px;padding:25px;border-radius:10px;background:white;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
        <h3 style="color:#007bff;margin-bottom:20px;">Add New Student</h3>

        <div id="addStudentMessage" style="display:none; margin:15px 0; padding:12px; border-radius:6px;"></div>

        <form id="addStudentForm" style="display:flex;flex-direction:column;gap:18px;">


            <div class="form-group">
                <label for="coursePickerAdd">Select Course</label>
                <select id="coursePickerAdd" name="course" required>
                    <option value="" selected disabled>-- choose course --</option>
                    <option>B.Tech</option>
                    <option>BCA</option>
                    <option>MCA</option>
                    <option>M.Tech</option>
                    <option>BVoc</option>
                    <option>Mech</option>
                </select>
            </div>

            <div id="studentFields" style="display:none;flex-direction:column;gap:15px;">
                <div class="form-group">
                    <label for="studentId">Student ID</label>
                    <input type="number" id="studentId" name="studentId" required>
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

  <div id="viewStudentSection" style="display:none; margin-top: 30px; max-width: 1700px;">
    <button id="backToFeaturesBtn" style="margin-bottom: 15px; /* Add any other existing styles here */">Back to Features</button>

    <div style="display: flex; gap: 20px; margin-bottom: 20px; flex-wrap: wrap; align-items: flex-end;">
        <div class="form-group" style="flex: 1; min-width: 250px;">
            <label for="studentSearchInput">Search Students (ID, Name, Email, Phone)</label>
            <input type="text" id="studentSearchInput" placeholder="Type to search..." style="width: 100%;">
        </div>

        <div class="form-group" style="min-width: 150px;">
            <label for="courseFilterDropdown">Filter by Course</label>
            <select id="courseFilterDropdown" style="width: 100%;">
                <option value="">All Courses</option>
                <option>B.Tech</option>
                <option>BCA</option>
                <option>MCA</option>
                <option>M.Tech</option>
                <option>BVoc</option>
                <option>Mech</option>
            </select>
        </div>
        <button id="resetFiltersBtn" style="height: 40px; margin-bottom: 1px;">Reset Filters</button>
    </div>
    
    <div class="table-responsive">
    <table id="studentsTable">
     <thead>
        <tr >
            <th data-sort-col="0" data-sort-dir="asc" >
                Student ID &nbsp;&nbsp;<span style="position: relative; right: -165px;" class="fa-solid fa-sort sort-icon"></span>
            </th>
            <th style="padding: 12px 15px; width:300px; text-align: left; background: #3; color: white;">Name</th>
            <th style="padding: 12px 15px; width: 100px;text-align: left; background: #3; color: white;">Course</th>
            <th data-sort-col="3" data-sort-dir="asc" style="padding: 12px 15px; width: 150px; text-align: left; background: #3; color: white; cursor: pointer;">
                Semester <span style="position: relative; right: -30px;" class="fa-solid fa-sort sort-icon"></span>
            </th>
            <th style="padding: 12px 15px; width: 300px;text-align: left; background: #3; color: white;">Email</th>
            <th style="padding: 12px 15px; width: 300px;text-align: left; background: #3; color: white;">Phone</th>
            <th style="padding: 12px 15px; width:250px; text-align: left; background: #3; color: white;">Actions</th>
        </tr>
    </thead>
    <tbody  id="studentsTableBody">
    </tbody>
</table>
</div>
    </div>

    <div id="todoPlaceholder" style="display:none;margin-top:30px;font-style:italic;color:grey"></div>
</section>
<!-- ───────────────────  ATTENDANCE MANAGEMENT  ──────────────────── -->
<section id="attendanceManagementSection"
         style="display:none;margin-top:70px;margin-left:50px;">
  <h2 class="text" style="margin-bottom:18px;font-weight:bold;font-size:30px;">
      Attendance Management
  </h2>

  <div id="attendanceFeatureCards" class="feature-card-wrap"
       style="display:flex;gap:20px;flex-wrap:wrap">
      <div class="feature-card" onclick="showAttendanceAction('take')">
          📝 Take&nbsp;Attendance
      </div>
      <div class="feature-card" onclick="showAttendanceAction('view')">
          📊 View&nbsp;Attendance
      </div>
  </div>

<!-- TAKE ATTENDANCE ────────────────────────────────────────────── -->
<div id="takeAttendanceSection" style="display:none;max-width:800px;
     margin-top:30px;padding:25px;border-radius:10px;
     background:#fff;box-shadow:0 4px 20px rgba(0,0,0,.08);">

  <h3 style="color:#007bff;margin-bottom:20px;">Take Attendance</h3>

  <!-- TIMESTAMP -->
  <div class="form-group" style="margin-bottom:18px;">
      <label for="attDateTime">Date & Time (auto)</label>
      <input id="attDateTime" type="text" readonly
             style="width:100%;background:#f5f5f5;border:1px solid #ccc;
                    padding:8px 10px;border-radius:6px;font-weight:500;">
  </div>

  <!-- PROGRAM -->
  <div class="form-group" style="margin-bottom:18px;">
      <label for="programSelect">Program</label>
      <select id="programSelect" required style="width:100%;">
          <option value="" disabled selected>-- choose program --</option>
          <option value="MCA">MCA</option>
          <option value="MBA">MBA</option>
          <option value="B.Tech">B.Tech</option>
          <option value="BCA">BCA</option>
          <option value="M.Tech">M.Tech</option>
          <option value="BVoc">BVoc</option>
          <option value="Mech">Mech</option>
      </select>
  </div>

  <!-- SEMESTER -->
  <div class="form-group" style="margin-bottom:18px;">
      <label for="semesterSelect">Semester</label>
      <select id="semesterSelect" disabled required style="width:100%;">
          <option value="" disabled selected>-- choose semester --</option>
          <option>1</option><option>2</option><option>3</option>
          <option>4</option><option>5</option><option>6</option>
      </select>
  </div>

  <!-- SUBJECT -->
  <div class="form-group" style="margin-bottom:25px;">
      <label for="subjectSelect">Subject</label>
      <select id="subjectSelect" disabled required style="width:100%;">
          <option value="" disabled selected>-- choose subject --</option>
      </select>
  </div>

  <button id="loadStudentsBtn" style="margin-right:15px;">Load students</button>
  <button onclick="showAttendanceAction('')">Back</button>
</div>


  <div id="viewAttendanceSection" style="display:none;max-width:900px;margin-top:30px;">
      <h3 style="color:#007bff;margin-bottom:20px;">View Attendance</h3>
      <!-- TODO: replace with your table / filters -->
      <p>Display attendance records here…</p>
      <button onclick="showAttendanceAction('')" style="margin-top:15px;">Back</button>
  </div>
</section>
<!-- ──────────────────────────────────────────────────────────────── -->


</div>

</body>
</html>
<%@ page isELIgnored="false" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>Student Dashboard | ${dashboard.studentName}</title>
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500&family=Poppins:wght@500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/stud.css" />
<script src="<%= request.getContextPath() %>/js/theme.js"></script>


</head>

<body class="${sessionScope.theme}">

<div class="app-container">
    <header class="app-header">
        <div class="header-logo"><img src="${pageContext.request.contextPath}/New_Horizon_College_of_Engineering_logo.png" alt="NHCE Logo"></div>
        <div class="header-actions">
            <button class="action-btn theme-toggle">
                <i class='bx bx-sun'></i>
                <i class='bx bx-moon'></i>
            </button>
            <div class="profile-menu-wrapper">
                <button class="action-btn profile-avatar-btn">
                    <img src="Graduation-icon.png" alt="Graduation Icon" class="profile-avatar">
                </button>
                <div class="profile-dropdown">
                    <div class="profile-dropdown-item nav-link" data-target="profile-view">
                        <i class='bx bxs-user-circle'></i>
                        <span>View Profile</span>
                    </div>
                    <div class="profile-dropdown-divider"></div>
                    <a href="${pageContext.request.contextPath}/logout" class="profile-dropdown-item logout">
                        <i class='bx bxs-log-out'></i>
                        <span>Logout</span>
                    </a>
                </div>
            </div>
        </div>
    </header>

    <main class="content-area">
        <div class="content-scroll-wrapper">
            
            <div id="dashboard-home" class="view is-active">
                 <div class="welcome-header">
                    <h1>Welcome, <c:out value="${dashboard.studentName}"/>!</h1>
                    <br>
                    <br>
                </div>
                <div class="home-layout-grid">
                    <div class="home-col-left">
                        <div class="widget">
                            <div class="widget-header"><i class='bx bxs-star'></i><span>Today's Highlights</span></div>
                            <ul class="data-list">
                                <li class="data-list-item" id="next-class-item">
                                    <div class="icon" style="background-color: var(--accent-orange);"><i class='bx bx-time-five'></i></div>
                                    <div class="content"><h4>Next Class: <span id="next-class-subject">N/A</span></h4><p class="meta" id="next-class-time">No class scheduled</p></div>
                                </li>
                                <li class="data-list-item">
                                    <div class="icon" style="background-color: var(--accent-green);"><i class='bx bxs-pie-chart-alt-2'></i></div>
                                    <div class="content"><h4>Overall Attendance</h4></div>
                                    <span class="trailing"><fmt:formatNumber value="${dashboard.overallAttendance.overallPercentage}" maxFractionDigits="0"/>%</span>
                                </li>
                            </ul>
                        </div>
                        <div class="nav-card nav-link" data-target="attendance-view">
                            <div class="icon-wrapper" style="background: linear-gradient(135deg, #23C35F, #34c759);"><i class='bx bxs-pie-chart-alt-2'></i></div>
                            <div><h3>Attendance</h3><p>View your detailed status and history</p></div>
                        </div>
                        <div class="nav-grid">
                            <div class="nav-card nav-link" data-target="marks-view">
                                <div class="icon-wrapper" style="background: linear-gradient(135deg, #007BFF, #0056b3);"><i class='bx bxs-medal'></i></div>
                                <div><h3>Marks & Grades</h3><p>Check recent scores by semester</p></div>
                            </div>
                            <div class="nav-card nav-link" data-target="schedule-view">
                                <div class="icon-wrapper" style="background: linear-gradient(135deg, #FF9500, #c77400);"><i class='bx bxs-calendar'></i></div>
                                <div><h3>Schedule</h3><p>See your timetable</p></div>
                            </div>
                        </div>
                    </div>
                    <div class="home-col-right">
            			<div class="widget">
                			<div class="widget-header"><i class='bx bxs-calendar-star'></i><span>Upcoming Events</span></div>
                				<ul class="data-list" id="upcoming-events-list">
                    			<div style="text-align: center; color: var(--text-secondary); padding: 10px;" id="events-placeholder">
                        		<p>No upcoming events.</p>
                    			</div>
                				</ul>
                        </div>
                    </div>
                </div>
            </div>
            
            <div id="profile-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>My Profile</h2></div>
                <div class="widget">
                    <div class="profile-avatar-large-wrapper">
                        <img src="Graduation-icon.png" alt="Student Avatar" class="profile-avatar-large">
                    </div>
                    <div class="form-group">
                        <label for="studentName">Full Name</label>
                        <input type="text" id="studentName" class="form-control" value="<c:out value="${dashboard.studentName}"/>" readonly>
                    </div>
                    <div class="form-group">
                        <label for="studentId">Student ID</label>
                        <input type="text" id="studentId" class="form-control" value="<c:out value="${dashboard.studentId}"/>" readonly>
                    </div>
                    <div class="form-group">
                        <label for="ProgramName">Program</label>
                        <input type="text" id="Program" class="form-control" value="<c:out value="${dashboard.programName}"/>" readonly>
                    </div>
                    <div class="form-group">
                        <label for="studentProgram">Current Semester</label>
                        <input type="text" id="CrrSem" class="form-control" value="<c:out value="${dashboard.currentSemester}"/>" readonly>
                    </div>
                    <div class="form-group">
                        <label for="studentEmail">Email</label>
                        <input type="text" id="Email" class="form-control" value="<c:out value="${dashboard.email}"/>" readonly>
                    </div>
                    
                </div>
            </div>

          <div id="attendance-view" class="view">
    <div class="view-header">
        <button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button>
        <h2>Attendance</h2>
    </div>

    <div class="semester-filter-wrapper">
        <label for="attendance-semester-filter">Semester:</label>
        <select id="attendance-semester-filter" class="semester-filter" data-view-target="attendance-view">
            <c:forEach var="entry" items="${dashboard.performanceBySemester}">
                <option value="${entry.key}" ${entry.key == dashboard.currentSemester ? 'selected' : ''}>
                    Semester ${entry.key}
                </option>
            </c:forEach>
        </select>
    </div>

    <c:forEach var="entry" items="${dashboard.performanceBySemester}">
        <div class="semester-content" data-view="attendance-view" data-semester="${entry.key}">
            <div class="attendance-grid">
                <c:forEach var="perf" items="${entry.value}">
                     <div class="attendance-card">
                         <h4><c:out value="${perf.subjectName}"/></h4>
                         <div class="attendance-bar-container">
                            <div class="attendance-bar" data-percentage="<fmt:formatNumber value='${perf.attendancePercentage}' maxFractionDigits='2'/>"></div>
                         </div>
                         <div class="attendance-meta">
                            <span><c:out value="${perf.classesAttended}"/> / <c:out value="${perf.totalClassesHeld}"/> classes</span>
                            <span><fmt:formatNumber value="${perf.attendancePercentage}" maxFractionDigits="0"/>%</span>
                         </div>
                     </div>
                </c:forEach>
            </div>
        </div>
    </c:forEach>
</div>

               <div id="marks-view" class="view">
    <div class="view-header">
        <button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button>
        <h2>Marks & Grades</h2>
    </div>

    <div class="semester-filter-wrapper">
        <label for="marks-semester-filter">Semester:</label>
        <select id="marks-semester-filter" class="semester-filter" data-view-target="marks-view">
           <c:forEach var="entry" items="${dashboard.performanceBySemester}">
                <option value="${entry.key}" ${entry.key == dashboard.currentSemester ? 'selected' : ''}>Semester ${entry.key}</option>
            </c:forEach>
        </select>
    </div><br><br>
 

                    <c:forEach var="entry" items="${dashboard.performanceBySemester}">
                        <div class="semester-content" data-view="marks-view" data-semester="${entry.key}">
                            <div class="widget">
                                <div class="exam-filter-container">
                                    <button class="exam-filter-btn active" data-exam-target="cie1">CIE-1</button>
                                    <button class="exam-filter-btn" data-exam-target="cie2">CIE-2</button>
                                    <button class="exam-filter-btn" data-exam-target="see">Final (SEE)</button>
                                </div>

                                <div class="exam-content is-visible" data-exam-type="cie1">
                                <div class="table-scroll-wrapper">
                                    <table class="marks-table">
                                        <thead><tr><th>Course</th><th>Marks Obtained</th><th>Total Marks</th><th>Percentage</th></tr></thead>
                                        <tbody>
                                            <c:forEach var="perf" items="${entry.value}">
                                                <tr>
                                                    <td><c:out value="${perf.subjectName}"/></td>
                                                    <td><c:out value="${not empty perf.ia1Marks ? perf.ia1Marks : 'N/A'}"/></td>
                                                    <td><c:out value="${not empty perf.ia1Marks ? perf.maxCieMarks : 'N/A'}"/></td>
                                                    <td><c:if test="${not empty perf.cie1Percentage}"><fmt:formatNumber value="${perf.cie1Percentage}" maxFractionDigits="2"/>%</c:if><c:if test="${empty perf.cie1Percentage}">N/A</c:if></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                  </div>
                                </div>

                                <div class="exam-content" data-exam-type="cie2">
                                <div class="table-scroll-wrapper">
                                    <table class="marks-table">
                                        <thead><tr><th>Course</th><th>Marks Obtained</th><th>Total Marks</th><th>Percentage</th></tr></thead>
                                        <tbody>
                                            <c:forEach var="perf" items="${entry.value}">
                                                <tr>
                                                    <td><c:out value="${perf.subjectName}"/></td>
                                                    <td><c:out value="${not empty perf.ia2Marks ? perf.ia2Marks : 'N/A'}"/></td>
                                                    <td><c:out value="${not empty perf.ia2Marks ? perf.maxCieMarks : 'N/A'}"/></td>
                                                    <td><c:if test="${not empty perf.cie2Percentage}"><fmt:formatNumber value="${perf.cie2Percentage}" maxFractionDigits="2"/>%</c:if><c:if test="${empty perf.cie2Percentage}">N/A</c:if></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                    </div>
                                </div>

                                <div class="exam-content" data-exam-type="see">
                                <div class="table-scroll-wrapper">
                                    <table class="marks-table">
                                        <thead><tr><th>Course</th><th>Combined CIE</th><th>SEE Marks</th><th>Total</th><th>Percentage</th></tr></thead>
                                        <tbody>
                                            <c:forEach var="perf" items="${entry.value}">
                                                <tr>
                                                    <td><c:out value="${perf.subjectName}"/></td>
                                                    <td><c:out value="${not empty perf.combinedCieMarks ? perf.combinedCieMarks : 'N/A'}"/> / <fmt:formatNumber value="${perf.maxCieMarks}" maxFractionDigits="0"/></td>
                                                    <td><c:out value="${not empty perf.seeMarks ? perf.seeMarks : 'N/A'}"/> / <fmt:formatNumber value="${perf.maxSeeMarks}" maxFractionDigits="0"/></td>
                                                    <td><c:out value="${not empty perf.finalTotalMarks ? perf.finalTotalMarks : 'N/A'}"/> / <fmt:formatNumber value="${perf.finalMaxMarks}" maxFractionDigits="0"/></td>
                                                    <td><c:if test="${not empty perf.finalPercentage}"><fmt:formatNumber value="${perf.finalPercentage}" maxFractionDigits="2"/>%</c:if><c:if test="${empty perf.finalPercentage}">N/A</c:if></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                        </div>
                    </c:forEach>
                </div>
                
      

            
            <div id="schedule-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>Today's Schedule</h2></div>
                <div class="widget">
                    <div class="widget-header"><i class='bx bxs-calendar'></i><span id="schedule-header-date">Classes for <fmt:formatDate value="<%= new java.util.Date() %>" pattern="MMM d, yyyy"/></span></div>
                    <ul class="data-list" id="todays-schedule-list">
                         <div style="text-align: center; color: var(--text-secondary); padding: 20px;">
                             <p>Loading schedule...</p>
                         </div>
                    </ul>
                </div>
            </div>
        </div>
    </main>
</div>
<div class="modal-overlay" id="event-modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="modal-event-title">Event Title</h3>
            <button class="action-btn modal-close-btn"><i class='bx bx-x'></i></button>
        </div>
        <div class="modal-body">
            <p id="modal-event-description">Event description will go here...</p>
            <div class="event-meta-grid">
                <div class="meta-item" id="modal-event-date-item">
                    <span class="meta-label">Event Date</span>
                    <span class="meta-value" id="modal-event-date"></span>
                </div>
                <div class="meta-item" id="modal-event-time-item">
                    <span class="meta-label">Time</span>
                    <span class="meta-value" id="modal-event-time"></span>
                </div>
                <div class="meta-item" id="modal-event-reg-date-item">
                    <span class="meta-label">Registration</span>
                    <span class="meta-value" id="modal-event-reg-date"></span>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#" target="_blank" class="btn btn-register" id="modal-event-link">
                <i class='bx bx-link-external'></i> Register 
            </a>
        </div>
    </div>
</div>
<script>
    window.studentProgramId = '<c:out value="${dashboard.programId}"/>';
    window.studentSemester = '<c:out value="${dashboard.currentSemester}"/>';
    window.serverDate = '<fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy-MM-dd"/>';
</script>

<script src="${pageContext.request.contextPath}/js/student.js"></script>


</body>
</html>
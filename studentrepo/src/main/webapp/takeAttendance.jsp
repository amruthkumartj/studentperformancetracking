<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.portal.AttendanceSession" %>
<%@ page import="com.portal.Student" %>
<%-- REMOVE THIS: @page import="java.time.LocalDateTime" --%>
<%@ page import="java.time.Instant" %>             <%-- Keep this --%>
<%@ page import="java.time.ZonedDateTime" %>        <%-- Keep this --%>
<%@ page import="java.time.ZoneId" %>               <%-- ADD THIS for specific timezone --%>
<%@ page import="java.time.ZoneOffset" %>          <%-- ADD THIS for UTC --%>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.GsonBuilder" %>
<%@ page import="com.fatboyindustrial.gsonjavatime.Converters" %>

<%
    // --- Your Java code remains the same ---
    AttendanceSession currentSession = (AttendanceSession) session.getAttribute("currentAttendanceSession");
    String sessionIdStr = request.getParameter("sessionId");

    int sessionId = -1;
    if (sessionIdStr != null && !sessionIdStr.isEmpty()) {
        try {
            sessionId = Integer.parseInt(sessionIdStr);
        } catch (NumberFormatException e) {
            System.err.println("Invalid sessionId format: " + sessionIdStr);
            response.sendRedirect(request.getContextPath() + "/facultyDashboard.jsp");
            return;
        }
    } else {
        response.sendRedirect(request.getContextPath() + "/facultyDashboard.jsp");
        return;
    }

    if (currentSession == null || currentSession.getSessionId() != sessionId) {
        response.sendRedirect(request.getContextPath() + "/facultyDashboard.jsp");
        return;
    }

    GsonBuilder gsonBuilder = new GsonBuilder();
    Converters.registerAll(gsonBuilder);
    Gson gson = gsonBuilder.create();
    String sessionJson = gson.toJson(currentSession);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Take Attendance - <%= currentSession.getSubjectName() %></title>
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/> 
    <style>
       @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

:root {
    --primary-color: #6a5acd; /* SlateBlue */
    --success-color: #28a745;
    --warning-color: #ffc107;
    --danger-color: #dc3545;
    --light-color: #f8f9fa;
    --dark-color: #343a40;
    --text-color: #495057;
    --bg-color: #eef5f9;
    --card-bg-color: rgba(255, 255, 255, 0.6);
    --border-radius: 12px;
    --box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
    --backdrop-filter: blur(10px);
}

body {
    font-family: 'Poppins', sans-serif;
    background-color: var(--bg-color);
    margin: 0;
    padding: 20px;
    color: var(--text-color);
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

/* --- Header --- */
.header-card {
    background: var(--card-bg-color);
    border-radius: var(--border-radius);
    padding: 20px;
    box-shadow: var(--box-shadow);
    backdrop-filter: var(--backdrop-filter);
    border: 1px solid rgba(255, 255, 255, 0.18);
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 20px;
}

.header-card h2 {
    margin: 0;
    color: var(--primary-color);
    font-size: 1.8em;
}

.timer-wrapper {
    display: flex;
    align-items: center;
    gap: 15px;
}

#countdownTimer {
    background-color: var(--success-color);
    color: white;
    padding: 10px 20px;
    border-radius: 25px;
    font-size: 1.5em;
    font-weight: 600;
    min-width: 100px;
    text-align: center;
    transition: background-color 0.5s ease;
}

#countdownTimer.expiring { background-color: var(--warning-color); }
#countdownTimer.expired { background-color: var(--danger-color); }

/* --- Session Details --- */
.session-details-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
}

.detail-card {
    background: var(--card-bg-color);
    border-radius: var(--border-radius);
    padding: 15px;
    box-shadow: var(--box-shadow);
    backdrop-filter: var(--backdrop-filter);
    border: 1px solid rgba(255, 255, 255, 0.18);
}

.detail-card strong {
    display: block;
    color: var(--primary-color);
    margin-bottom: 5px;
}

/* --- Attendance Actions & Filters --- */
.controls-card {
    background: var(--card-bg-color);
    border-radius: var(--border-radius);
    padding: 20px;
    box-shadow: var(--box-shadow);
    backdrop-filter: var(--backdrop-filter);
    border: 1px solid rgba(255, 255, 255, 0.18);
}

.action-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 20px;
}

.btn {
    padding: 10px 20px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    font-size: 0.95em;
    transition: all 0.3s ease;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.btn-present { background-color: var(--success-color); color: white; }
.btn-present:hover { background-color: #218838; transform: translateY(-2px); }
.btn-absent { background-color: var(--warning-color); color: var(--dark-color); }
.btn-absent:hover { background-color: #e0a800; transform: translateY(-2px); }
.btn-cancel { background-color: var(--danger-color); color: white; }
.btn-cancel:hover { background-color: #c82333; transform: translateY(-2px); }
.btn-filter { background-color: var(--primary-color); color: white; }
.btn-filter:hover { background-color: #5948b1; transform: translateY(-2px); }

.filter-section {
    display: none; /* Initially hidden */
    margin-top: 20px;
}

.filter-section input {
    width: 100%;
    padding: 12px;
    border: 1px solid #ced4da;
    border-radius: 8px;
    box-sizing: border-box;
    font-size: 1em;
}

/* --- Student List Table --- */
.student-list-card {
    background: var(--card-bg-color);
    border-radius: var(--border-radius);
    padding: 20px;
    box-shadow: var(--box-shadow);
    backdrop-filter: var(--backdrop-filter);
    border: 1px solid rgba(255, 255, 255, 0.18);
    overflow: hidden;
}

.table-container {
    width: 100%;
    max-height: 500px;
    overflow-y: auto;
    /* --- SCROLLBAR STYLES (FIREFOX) --- */
    scrollbar-width: thin;
    scrollbar-color: var(--primary-color) #eef5f9;
}

/* --- SCROLLBAR STYLES (CHROME, SAFARI) --- */
.table-container::-webkit-scrollbar {
    width: 8px;
}

.table-container::-webkit-scrollbar-track {
    background: #eef5f9;
    border-radius: 10px;
}

.table-container::-webkit-scrollbar-thumb {
    background-color: var(--primary-color);
    border-radius: 10px;
    border: 2px solid #eef5f9;
}

.table-container::-webkit-scrollbar-thumb:hover {
    background-color: #5948b1;
}


.attendance-table {
    width: 100%;
    border-collapse: collapse;
}

.attendance-table th, .attendance-table td {
    padding: 15px;
    text-align: left;
    border-bottom: 1px solid rgba(0,0,0,0.05);
}

.attendance-table thead th {
    position: sticky;
    top: 0;
    background-color: var(--primary-color);
    color: white;
    font-weight: 600;
    z-index: 1; /* Lifts header layer */
}

.attendance-table tbody tr {
    transition: background-color 0.3s ease;
}

.attendance-table tbody tr:hover {
    background-color: rgba(106, 90, 205, 0.1);
}

/* --- iOS-style switch --- */
.ios-switch {
    position: relative;
    display: inline-block;
    width: 50px;
    height: 28px;
}
.ios-switch input { display: none; }
.slider {
    position: absolute;
    cursor: pointer;
    top: 0; left: 0; right: 0; bottom: 0;
    background-color: #ccc;
    transition: .4s;
    border-radius: 28px;
}
.slider:before {
    position: absolute;
    content: "";
    height: 22px; width: 22px;
    left: 3px; bottom: 3px;
    background-color: white;
    transition: .4s;
    border-radius: 50%;
}
input:checked + .slider { background-color: var(--success-color); }
input:checked + .slider:before { transform: translateX(22px); }

/* --- Floating Action Button (FAB) for Submit --- */
.fab-submit {
    position: fixed;
    bottom: 30px;
    right: 30px;
    width: 60px;
    height: 60px;
    background-color: var(--primary-color);
    color: white;
    border: none;
    border-radius: 50%;
    box-shadow: 0 6px 20px rgba(106, 90, 205, 0.4);
    font-size: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    z-index: 1000;
}

.fab-submit:hover {
    transform: scale(1.1);
    background-color: #5948b1;
}

/* --- Basic Message Box --- */
#messageBox {
    display: none;
    padding: 15px;
    border-radius: var(--border-radius);
    font-weight: bold;
    text-align: center;
    box-shadow: var(--box-shadow);
    color: white;
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 2000;
    opacity: 0;
    transition: opacity 0.3s ease, transform 0.3s ease;
}
#messageBox.show {
    display: block;
    opacity: 1;
    transform: translateX(-50%) translateY(0);
}
#messageBox.success { background-color: var(--success-color); }
#messageBox.error { background-color: var(--danger-color); }
#messageBox.info { background-color: var(--primary-color); }

/* --- Custom Message OVERLAY Styles --- */
#customMessageOverlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(8px);
    display: none;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.3s ease, visibility 0.3s ease;
}

#customMessageOverlay.show {
    display: flex;
    opacity: 1;
    visibility: visible;
}

#customMessageContent {
    background-color: white;
    padding: 30px;
    border-radius: var(--border-radius);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
    max-width: 400px;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    opacity: 0;
    transform: translateY(-20px);
    transition: opacity 0.3s ease, transform 0.3s ease;
    border: 1px solid rgba(0, 0, 0, 0.1);
}

#customMessageContent.show {
    opacity: 1;
    transform: translateY(0);
}

#customMessageContent .material-icons {
    font-size: 60px;
    margin-bottom: 10px;
}

#customMessageContent p {
    font-size: 1.2em;
    font-weight: 500;
    color: var(--text-color);
    margin: 0;
}

#customMessageContent.success .material-icons,
#customMessageContent.success p {
    color: var(--success-color);
}

#customMessageContent.error .material-icons,
#customMessageContent.error p {
    color: var(--danger-color);
}

#customMessageContent.info .material-icons,
#customMessageContent.info p {
    color: var(--primary-color);
}

/* --- Message Box Buttons --- */
.message-buttons {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 15px;
    width: 100%;
    flex-wrap: wrap;
}

#customMessageOkBtn, #customMessageCancelBtn {
    padding: 10px 25px;
    border: none;
    border-radius: 8px;
    font-size: 1em;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    flex-grow: 1;
    min-width: 120px;
}

#customMessageOkBtn {
    background-color: var(--primary-color);
    color: white;
}
#customMessageOkBtn:hover {
    background-color: #5948b1;
}

#customMessageCancelBtn {
    background-color: #ccc;
    color: var(--dark-color);
}
#customMessageCancelBtn:hover {
    background-color: #bbb;
}

#customMessageContent.error #customMessageOkBtn {
    background-color: var(--danger-color);
}
#customMessageContent.error #customMessageOkBtn:hover {
    background-color: #c82333;
}
#customMessageContent.success #customMessageOkBtn {
    background-color: var(--success-color);
}
#customMessageContent.success #customMessageOkBtn:hover {
    background-color: #218838;
}

    </style>
</head>
<body>

    <div class="container">

        <div id="messageBox"></div>

        <div class="header-card">
            <h2>Take Attendance</h2>
            <div class="timer-wrapper">
                <div id="countdownTimer">--:--</div>
            </div>
        </div>

        <div class="session-details-grid">
            <div class="detail-card"><strong>Program:</strong> <span><%= currentSession.getProgramName() != null ? currentSession.getProgramName() : "N/A" %></span></div>
            <div class="detail-card"><strong>Semester:</strong> <span><%= currentSession.getSemester() %></span></div>
            <div class="detail-card"><strong>Subject:</strong> <span><%= currentSession.getSubjectName() %></span></div>
            <div class="detail-card"><strong>Expiry Time:</strong> <span id="sessionExpiryTime">
                <%
                    // THIS IS THE LINE THAT HAS BEEN CHANGED TO IST
                    Instant expiryInstant = currentSession.getSessionExpiryTime();
                    // Convert to ZonedDateTime in Indian Standard Time (Asia/Kolkata) for formatting
                    ZonedDateTime expiryIST = expiryInstant.atZone(ZoneId.of("Asia/Kolkata"));
                    // Now format it, including 'z' for timezone abbreviation
                    out.print(expiryIST.format(DateTimeFormatter.ofPattern("HH:mm:ss z")));
                %>
            </span></div></div>

        <div class="controls-card">
            <div class="action-buttons">
                <button id="markAllPresentBtn" class="btn btn-present"><i class='bx bx-check-double'></i> Mark All Present</button>
                <button id="markAllAbsentBtn" class="btn btn-absent"><i class='bx bx-user-x'></i> Mark All Absent</button>
                <button id="toggleFilterBtn" class="btn btn-filter"><i class='bx bx-filter-alt'></i> Filter</button>
                <button id="cancelSessionBtn" class="btn btn-cancel"><i class='bx bx-x-circle'></i> Cancel Session</button>
            </div>
            <div class="filter-section" id="filterSection">
                <input type="text" id="studentSearchInput" placeholder="Search by ID, Name, etc...">
            </div>
        </div>

        <div class="student-list-card">
            <div class="table-container">
                <table class="attendance-table">
                    <thead>
                        <tr>
                            <th>Student ID</th>
                            <th>Student Name</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody id="studentListTableBody">
                        </tbody>
                </table>
            </div>
        </div>
    </div>

    <button id="submitAttendanceBtn" class="fab-submit" title="Submit Attendance">
        <i class='bx bx-send'></i>
    </button>

    <div id="customMessageOverlay">
        <div id="customMessageContent">
            <span id="customMessageIcon" class="material-icons"></span>
            <p id="customMessageText"></p>
            <div class="message-buttons"> <button id="customMessageOkBtn">OK</button>
                <button id="customMessageCancelBtn">Cancel</button> </div>
        </div>
    </div>
    
<script>
    // --- Variable Declarations (All elements the JS will interact with) ---
    const currentSession = <%= sessionJson %>;
    const studentListTableBody = document.getElementById('studentListTableBody');
    const countdownTimer = document.getElementById('countdownTimer');
    const timerBox = document.querySelector('.timer-wrapper');

    const messageBox = document.getElementById('messageBox');
    let messageBoxTimeout;

    // Custom overlay message elements - ENSURE THESE IDs MATCH THE HTML ABOVE!
    const customMessageOverlay = document.getElementById('customMessageOverlay');
    const customMessageContent = document.getElementById('customMessageContent');
    const customMessageIcon = document.getElementById('customMessageIcon');
    const customMessageText = document.getElementById('customMessageText');
    const customMessageOkBtn = document.getElementById('customMessageOkBtn');
    const customMessageCancelBtn = document.getElementById('customMessageCancelBtn'); // NEW

    const markAllPresentBtn = document.getElementById('markAllPresentBtn');
    const markAllAbsentBtn = document.getElementById('markAllAbsentBtn');
    const submitAttendanceBtn = document.getElementById('submitAttendanceBtn');
    const cancelSessionBtn = document.getElementById('cancelSessionBtn');
    const studentSearchInput = document.getElementById('studentSearchInput');
    const toggleFilterBtn = document.getElementById('toggleFilterBtn');
    const filterSection = document.getElementById('filterSection');

    let studentsData = [];
    let countdownInterval;

    // --- Basic Message Box Function (for general notifications, disappears after time) ---
    function showMessage(message, type = 'info') {
        clearTimeout(messageBoxTimeout);

        messageBox.textContent = message;
        messageBox.classList.remove('success', 'error', 'info');
        messageBox.classList.add(type);
        messageBox.classList.add('show');

        messageBoxTimeout = setTimeout(() => {
            messageBox.classList.remove('show');
        }, 3000);
    }

    // --- Custom Overlay Message Function (for critical actions, requires user interaction) ---
    function showCustomOverlayMessage(message, type = 'info', options = {}) {
        let iconClass = '';

        customMessageContent.classList.remove('success', 'error', 'info');
        customMessageIcon.textContent = ''; // Clear existing icon

        switch (type) {
            case 'success':
                iconClass = 'check_circle';
                customMessageContent.classList.add('success');
                break;
            case 'error':
                iconClass = 'error';
                customMessageContent.classList.add('error');
                break;
            case 'info':
                iconClass = 'info';
                customMessageContent.classList.add('info');
                break;
            default:
                iconClass = 'info';
                customMessageContent.classList.add('info');
        }

        customMessageIcon.textContent = iconClass;
        customMessageText.textContent = message;

        customMessageOverlay.classList.add('show');
        customMessageContent.classList.add('show');

        // Clear previous event listeners on both buttons
        customMessageOkBtn.onclick = null;
        customMessageCancelBtn.onclick = null;

        // Conditionally show/hide and set up Cancel button
        if (options.showCancelButton) {
            customMessageCancelBtn.style.display = 'inline-block'; // Show cancel button
            // If you want buttons to size equally regardless of text length:
            // customMessageOkBtn.style.width = 'auto'; 
            // customMessageCancelBtn.style.width = 'auto';
            
            customMessageCancelBtn.onclick = () => {
                customMessageOverlay.classList.remove('show');
                customMessageContent.classList.remove('show');
                if (options.onCancel) {
                    options.onCancel();
                }
            };
        } else {
            customMessageCancelBtn.style.display = 'none'; // Hide cancel button
            // Make OK button full width when no cancel button is present
            // This is optional and depends on your desired layout when only one button is visible
            // customMessageOkBtn.style.width = '100%'; 
        }

        // Attach event listener to OK button (always present)
        customMessageOkBtn.onclick = () => {
            customMessageOverlay.classList.remove('show');
            customMessageContent.classList.remove('show');
            if (options.onOk) {
                options.onOk();
            }
        };
    }

    // --- Disable all action buttons and toggles ---
    function disableAllActions() {
        console.log("Disabling all actions...");
        markAllPresentBtn.disabled = true;
        markAllAbsentBtn.disabled = true;
        submitAttendanceBtn.disabled = true;
        cancelSessionBtn.disabled = true;
        studentSearchInput.disabled = true;
        toggleFilterBtn.disabled = true;
        document.querySelectorAll('.ios-switch input').forEach(toggle => toggle.disabled = true);
    }

    // NEW: Create a separate function to encapsulate the actual submission logic
    async function proceedSubmission(isAutoSubmit) {
        // Use custom overlay message for submission process
        if(isAutoSubmit) {
            // Default to Present on auto-submit if session expired and not yet marked
            studentsData.forEach(student => {
                student.attendanceStatus = 'P';
            });
            // Show as 'info' while processing auto-submit, then change based on result
            showCustomOverlayMessage('Session expired. Submitting all students as Present.', 'info');
            displayStudents(studentsData); // Re-render to show all as present
        } else {
             showCustomOverlayMessage('Submitting attendance...', 'info');
        }

        submitAttendanceBtn.disabled = true;

        const recordsToSubmit = studentsData.map(student => ({
            studentId: student.studentId,
            status: student.attendanceStatus || 'A'
        }));

        try {
            const response = await fetch('SubmitAttendanceServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: currentSession.sessionId,
                    records: recordsToSubmit
                })
            });
            const data = await response.json();
            if (response.ok && data.status === 'success') {
                showCustomOverlayMessage(data.message, 'success', {
                    onOk: () => { // Callback for success (redirect to dashboard)
                    	window.location.href = '<%= request.getContextPath() %>/facultydashboard';
                    }
                });
                disableAllActions();
                clearInterval(countdownInterval);

            } else {
                showCustomOverlayMessage(data.message || 'Failed to submit.', 'error');
                if (!isAutoSubmit) submitAttendanceBtn.disabled = false;
            }
        } catch (error) {
            console.error("Submission Error:", error);
            showCustomOverlayMessage('An error occurred during submission.', 'error');
            if (!isAutoSubmit) submitAttendanceBtn.disabled = false;
        }
    }

    // Reusable function for submitting attendance data
    async function submitAttendanceData(isAutoSubmit = false) {
        if (timerBox.classList.contains('expired') && !isAutoSubmit) {
            showMessage('Session has expired. Manual submission not allowed.', 'error');
            return;
        }

        if (!isAutoSubmit) {
            // MANUAL SUBMIT: Show confirmation with OK and CANCEL options
            showCustomOverlayMessage(
                'Are you sure you want to submit? This cannot be undone.',
                'info', // Use 'info' for the confirmation prompt itself
                {
                    showCancelButton: true, // Show the cancel button for manual submission
                    onOk: async () => { // This will be executed if the user clicks 'OK'
                        await proceedSubmission(isAutoSubmit);
                    },
                    onCancel: () => { // This will be executed if the user clicks 'Cancel'
                        console.log("Submission cancelled by user.");
                        // No further action needed, just close the overlay.
                    }
                }
            );
        } else {
            // AUTO-SUBMIT: Proceed directly without confirmation
            await proceedSubmission(isAutoSubmit);
        }
    }

    // --- Countdown Timer Logic ---
    function updateCountdown() {
        // console.log("⚠️ updateCountdown() called"); // Kept for debugging
        const expiryTimeStr = currentSession.sessionExpiryTime;
        // console.log("sessionExpiryTime:", expiryTimeStr); // Kept for debugging

        let expiry;
        try {
            let parseableTimeStr = expiryTimeStr;
            if (parseableTimeStr && !parseableTimeStr.includes('T')) {
                parseableTimeStr = parseableTimeStr.replace(' ', 'T');
            }
            expiry = new Date(parseableTimeStr);

            if (isNaN(expiry.getTime())) {
                console.warn("DEBUG: Direct Date() constructor failed, attempting manual parse.");
                const dateParts = parseableTimeStr.match(/(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2}):(\d{2})/);
                if (dateParts) {
                    expiry = new Date(
                        parseInt(dateParts[1]), parseInt(dateParts[2]) - 1, parseInt(dateParts[3]),
                        parseInt(dateParts[4]), parseInt(dateParts[5]), parseInt(dateParts[6])
                    );
                    console.log("DEBUG: Manual parse successful:", expiry);
                } else {
                    console.error("CRITICAL ERROR: Could not parse expiry time with any known format.");
                    expiry = new Date(0); // Force expired
                }
            }
        } catch (e) {
            console.error("Error parsing expiry time:", e);
            expiry = new Date(0); // Force expired
        }

        const now = new Date();
        // console.log("🕒 Current Time:", now); // Kept for debugging
        // console.log("🛑 Expiry Time:", expiry); // Kept for debugging

        const timeLeft = expiry.getTime() - now.getTime();
        // console.log("⏳ Time Left (ms):", timeLeft); // Kept for debugging

        const timerElem = document.getElementById("countdownTimer");
        if (!timerElem) {
            console.error("Timer element #countdownTimer not found!");
            clearInterval(countdownInterval);
            return;
        }

        if (timeLeft > 0) {
            const totalSeconds = Math.floor(timeLeft / 1000);
            const minutes = Math.floor(totalSeconds / 60);
            const seconds = totalSeconds % 60;

            // CORRECTED: Escaped dollar signs to prevent JSP EL parsing
            const display = `\${String(minutes).padStart(2, '0')}:\${String(seconds).padStart(2, '0')}`;

            // console.log("✅ Updating countdown display to:", display); // Kept for debugging
            timerElem.innerText = display;
            timerBox.classList.remove('expired');
            if (timeLeft < 60000) { // Less than 1 minute remaining
                timerElem.classList.add('expiring');
                timerElem.style.color = 'white';
                timerElem.style.backgroundColor = 'var(--warning-color)';
            } else {
                timerElem.classList.remove('expiring');
                timerElem.style.color = 'white';
                timerElem.style.backgroundColor = 'var(--success-color)';
            }

        } else {
            // Time is up
            timerElem.innerText = "00:00";
            timerBox.classList.add('expired');
            timerElem.style.color = 'white';
            timerElem.style.backgroundColor = 'var(--danger-color)';

            clearInterval(countdownInterval);
            disableAllActions();
            // Show red text for expired session (using 'error' type)
            showCustomOverlayMessage('Attendance session has expired. Submitting attendance automatically...', 'error');
            submitAttendanceData(true);
        }
    }

    // --- Main Initialization Logic ---
    document.addEventListener('DOMContentLoaded', () => {
        // Initial fetch of students when the page loads
        fetchStudents();

        if (currentSession && currentSession.sessionExpiryTime) {
            updateCountdown(); // Initial call to set up the timer display immediately
            countdownInterval = setInterval(updateCountdown, 1000); // Start the interval
            console.log("DEBUG: Timer initialized. Starting countdown with setInterval.");
        } else {
            console.error("ERROR: currentSession or sessionExpiryTime is not available. Timer will not start.");
            if (countdownTimer) {
                countdownTimer.textContent = 'N/A';
                if (timerBox) timerBox.classList.add('expired');
            }
            disableAllActions();
            showMessage('Session expiry time not found. Timer unavailable.', 'error');
        }
    });

    // --- Fetch Students for the Session ---
// --- Fetch Students for the Session ---
// REVISED FOR DEBUGGING
async function fetchStudents() {
    if (!studentListTableBody) {
        console.error("Student list table body element not found!");
        return;
    }
    studentListTableBody.innerHTML = '<tr><td colspan="3" style="text-align:center;padding:20px;">Loading students...</td></tr>';
    
    try {
        const response = await fetch('GetStudentsServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                programId: currentSession.programId,
                semester: currentSession.semester
            })
        });

        console.log("Response received from server:", response);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        // Let's get the raw text first to see what it is
        const responseText = await response.text();
        console.log("Raw response text from server:", responseText);

        // Now, let's try to parse it
        const data = JSON.parse(responseText);
        console.log("Parsed JSON data:", data);

        // Check for a nested array (assuming the key is 'students')
        // **IMPORTANT**: If your key is different, change `data.students` here
        if (data && Array.isArray(data.students)) { 
            studentsData = data.students.map(s => ({
                studentId: s.studentId,
                fullName: s.fullName,
                attendanceStatus: 'P' 
            }));
            displayStudents(studentsData);
        } else if (Array.isArray(data)) { // Fallback for raw array
             studentsData = data.map(s => ({
                studentId: s.studentId,
                fullName: s.fullName,
                attendanceStatus: 'P' 
            }));
            displayStudents(studentsData);
        } else {
            const serverMessage = data.message || 'Data is not an array or a recognized object.';
            throw new Error(serverMessage);
        }
    } catch (error) {
        // This will log the full error object, which is more helpful
        console.error('An error occurred in fetchStudents:', error); 
        
        const errorMessage = 'Failed to load students: ' + (error.message || 'Unknown error');
        studentListTableBody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:20px;color:red;">${errorMessage}</td></tr>`;
        disableAllActions();
        showMessage('Failed to load students for attendance.', 'error');
    }
}
    // --- Display Students in Table (now takes an array for filtering) ---
    function displayStudents(studentsToDisplay) {
            studentListTableBody.innerHTML = '';
            if (studentsToDisplay.length === 0) {
                studentListTableBody.innerHTML = '<tr><td colspan="3" style="text-align:center;">No students found matching your criteria.</td></tr>';
                return;
            }
            studentsToDisplay.forEach(student => {
                const row = document.createElement('tr');
                const isChecked = student.attendanceStatus === 'P';
                row.innerHTML = `
                    <td>\${student.studentId}</td>
                    <td>\${student.fullName}</td>
                    <td>
                        <label class="ios-switch">
                            <input type="checkbox" data-student-id="\${student.studentId}" \${isChecked ? 'checked' : ''}>
                            <span class="slider"></span>
                        </label>
                    </td>
                `;
                row.querySelector('input[type="checkbox"]').addEventListener('change', function() {
                    const studentId = parseInt(this.dataset.studentId);
                    const status = this.checked ? 'P' : 'A';
                    const studentIndex = studentsData.findIndex(s => s.studentId === studentId);
                    if (studentIndex > -1) {
                        studentsData[studentIndex].attendanceStatus = status;
                    }
                });
                studentListTableBody.appendChild(row);
            });
        }
    
    // --- Event Listeners ---
    toggleFilterBtn.addEventListener('click', () => {
        if (filterSection.style.display === 'none' || filterSection.style.display === '') {
            filterSection.style.display = 'block';
        } else {
            filterSection.style.display = 'none';
        }
    });

    studentSearchInput.addEventListener('input', function() {
        const filterText = this.value.toLowerCase();
        const filteredStudents = studentsData.filter(s => 
            (s.studentId && s.studentId.toString().includes(filterText)) ||
            (s.fullName && s.fullName.toLowerCase().includes(filterText))
        );
        displayStudents(filteredStudents);
    });

    markAllPresentBtn.addEventListener('click', () => {
        studentsData.forEach(s => s.attendanceStatus = 'P');
        displayStudents(studentsData);
        showMessage('All students marked Present.', 'info');
    });

    markAllAbsentBtn.addEventListener('click', () => {
        studentsData.forEach(s => s.attendanceStatus = 'A');
        displayStudents(studentsData);
        showMessage('All students marked Absent.', 'info');
    });

    submitAttendanceBtn.addEventListener('click', () => submitAttendanceData(false));

    // --- Cancel Session ---
    cancelSessionBtn.addEventListener('click', async function() {
        // Use custom overlay for confirmation
        showCustomOverlayMessage(
            "Are you sure you want to cancel this session? This will NOT save any attendance.",
            "info",
            {
                showCancelButton: true, // Show cancel button for this confirmation too
                onOk: async () => {
                    cancelSessionBtn.disabled = true;
                    showMessage('Cancelling session...', 'info'); // Use basic message box for brief feedback

                    try {
                        const response = await fetch('CancelAttendanceSessionServlet', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ sessionId: currentSession.sessionId })
                        });

                        const data = await response.json();
                        console.log(`Cancel Session Response: ${JSON.stringify(data)}`);

                        if (response.ok && data.status === 'success') {
                            showCustomOverlayMessage(data.message, 'success', {
                                onOk: () => {
                                    disableAllActions();
                                    clearInterval(countdownInterval);
                                    window.location.href = '<%= request.getContextPath() %>/facultydashboard';
                                }
                            });
                        } else {
                            showCustomOverlayMessage(data.message || 'Failed to cancel session.', 'error');
                            cancelSessionBtn.disabled = false;
                        }
                    } catch (error) {
                        console.error(`Error canceling session: ${error}`);
                        showCustomOverlayMessage('An error occurred while canceling the session.', 'error');
                        cancelSessionBtn.disabled = false;
                    }
                },
                onCancel: () => {
                    console.log("Session cancellation aborted by user.");
                    // Do nothing, just close the overlay
                }
            }
        );
    });
</script>
</body>
</html>
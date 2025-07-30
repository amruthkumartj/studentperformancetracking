<%@ page isELIgnored="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Performance Analytics</title>
    
    <link href='https://unpkg.com/boxicons@2.1.4/css/boxicons.min.css' rel='stylesheet'>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <style>
        :root {
            --primary-color: #6a5acd;
            --primary-color-light: rgba(106, 90, 205, 0.1);
            --primary-color-dark: #5a4aab;
            --success-color: #28a745;
            --danger-color: #dc3545;
            --warning-color: #ffc107;
            --light-color: #f8f9fa;
            --dark-color: #343a40;
            --text-color: #495057;
            --bg-color: #f4f7f9;
            --card-bg-color: rgba(255, 255, 255, 0.75);
            --border-radius: 12px;
            --transition-speed: 0.3s;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Poppins', sans-serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            padding: 1.5rem;
            line-height: 1.6;
        }
        .container { max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 1.5rem; }
        .card {
            background: var(--card-bg-color);
            border-radius: var(--border-radius);
            padding: 1.5rem 2rem;
            box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            transition: all var(--transition-speed) ease;
        }
        h2 { font-size: 1.75rem; color: var(--primary-color); margin-bottom: 1.5rem; padding-bottom: 0.75rem; border-bottom: 2px solid var(--primary-color-light); }
        .selection-controls { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; align-items: end; }
        .input-group label { display: block; font-weight: 600; margin-bottom: 0.5rem; color: var(--dark-color); }
        .styled-select, .styled-input {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 1px solid #ccc;
            border-radius: 8px;
            background-color: #fff;
            font-family: 'Poppins', sans-serif;
            font-size: 1rem;
            transition: all var(--transition-speed) ease;
        }
        .styled-select:focus, .styled-input:focus { outline: none; border-color: var(--primary-color); box-shadow: 0 0 0 3px var(--primary-color-light); }
        .styled-button {
            width: 100%;
            padding: 0.75rem 1rem;
            background: linear-gradient(45deg, var(--primary-color), var(--primary-color-dark));
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all var(--transition-speed) ease;
            box-shadow: 0 4px 15px rgba(106, 90, 205, 0.2);
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }
        .styled-button.view-single {
             padding: 0.5rem 1rem;
             font-size: 0.9rem;
        }
        .styled-button:hover:not(:disabled) { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(106, 90, 205, 0.3); }
        .styled-button:disabled { background: #ccc; cursor: not-allowed; opacity: 0.7; }
        .back-button { width: auto; align-self: flex-start; background: var(--text-color); }
        .view-mode-toggle { display: flex; background-color: var(--primary-color-light); border-radius: 8px; padding: 0.3rem; }
        .view-mode-toggle input[type="radio"] { display: none; }
        .view-mode-toggle label { flex: 1; padding: 0.6rem; text-align: center; border-radius: 6px; cursor: pointer; font-weight: 500; transition: all var(--transition-speed) ease; }
        .view-mode-toggle input[type="radio"]:checked + label { background-color: var(--primary-color); color: white; box-shadow: 0 2px 10px rgba(106, 90, 205, 0.3); }
        .loading-indicator { text-align: center; padding: 2rem; font-size: 1.2rem; color: var(--primary-color); }
        .loading-indicator .bx-spin { font-size: 3rem; }
        #messageBox { position: fixed; top: 20px; left: 50%; transform: translateX(-50%); padding: 1rem 1.5rem; border-radius: 8px; color: white; font-weight: 500; z-index: 9999; opacity: 0; transition: opacity var(--transition-speed) ease, transform var(--transition-speed) ease; }
        #messageBox.show { opacity: 1; transform: translateX(-50%) translateY(10px); }
        #messageBox.success { background-color: var(--success-color); }
        #messageBox.error { background-color: var(--danger-color); }
        #messageBox.info { background-color: var(--primary-color); }
        .table-responsive { overflow-x: auto; }
        .styled-table { width: 100%; border-collapse: collapse; margin-top: 1rem; }
        .styled-table th, .styled-table td { padding: 0.8rem 1rem; text-align: left; border-bottom: 1px solid #e0e0e0; }
        .styled-table th { background-color: var(--primary-color-light); font-weight: 600; color: var(--primary-color); }
        .styled-table tbody tr:hover { background-color: var(--primary-color-light); }
        #studentSummary { line-height: 1.8; border-bottom: 2px solid var(--primary-color-light); padding-bottom: 1.5rem; margin-bottom: 1.5rem;}
        #studentSummary div { font-size: 1.05rem; }
        .is-hidden { display: none !important; }
        @media (max-width: 768px) {
            body { padding: 1rem; }
            .card { padding: 1.5rem; }
            h2 { font-size: 1.5rem; }
            .selection-controls { grid-template-columns: 1fr; }
        }
        /* Container for the BI-style chart */
.chart-container {
    max-width: 900px; /* Controls the width of the chart */
    margin: 30px auto; /* Centers the chart container */
    padding: 25px;
    background-color: var(--sidebar-color, #fff); /* Uses your theme color */
    border-radius: 12px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.07);
    border: 1px solid var(--border-color, #eee);
}
/* --- NEW: Faculty Analysis Box --- */
.analysis-box {
    margin-top: 1.5rem;
    padding: 1.5rem;
    border-radius: var(--border-radius);
    border-left: 5px solid;
    display: flex;
    align-items: center;
    gap: 1rem;
    opacity: 0;
    transform: translateY(10px);
    animation: fadeInUp 0.5s 0.2s forwards;
}
@keyframes fadeInUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
.analysis-box i {
    font-size: 2.5rem;
    flex-shrink: 0;
}
.analysis-box p {
    margin: 0;
    font-size: 1.05rem;
    line-height: 1.6;
}
.analysis-box p strong {
    display: block;
    font-size: 1.2rem;
    margin-bottom: 0.25rem;
    font-family: 'Poppins', sans-serif;
}

/* Color Variants */
.analysis-box.success { border-color: var(--success-color); background-color: rgba(40, 167, 69, 0.1); }
.analysis-box.success i { color: var(--success-color); }
.analysis-box.info { border-color: var(--primary-color); background-color: var(--primary-color-light); }
.analysis-box.info i { color: var(--primary-color); }
.analysis-box.warning { border-color: var(--warning-color); background-color: rgba(255, 193, 7, 0.1); }
.analysis-box.warning i { color: var(--warning-color); }
.analysis-box.danger { border-color: var(--danger-color); background-color: rgba(220, 53, 69, 0.1); }
.analysis-box.danger i { color: var(--danger-color); }
/* --- NEW: Clear Button for Text Input --- */
        #clearStudentIdBtn {
            position: absolute;
            right: 12px;
            top: 41%;
            transform: translateY(50%);
            cursor: pointer;
            font-size: 1.5rem;
            color: var(--text-color);
            opacity: 0.6;
            transition: opacity 0.2s;
        }
        #clearStudentIdBtn:hover {
            opacity: 1;
        }
        .reset-button {
    background: var(--text-color);
}

.reset-button:hover:not(:disabled) {
    background: var(--dark-color);
}
    </style>
</head>
<body>

    <div class="container">
       <!-- REPLACE THIS: -->
        <!-- <a href="<%= request.getContextPath() %>/facultydashboard" class="styled-button back-button"> ... </a> -->

        <!-- WITH THIS: -->
        <button id="analyticsBackButton" class="styled-button back-button">
            <i class='bx bx-arrow-back'></i> Back
        </button>

        <div id="messageBox"></div>

        <div class="card">
            <h2>Student Performance Analytics</h2>
            
            <div id="initialChoiceView">
                <p style="margin-bottom: 1rem;">Select the type of analysis you want to perform:</p>
                <div class="selection-controls" style="grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));">
                    <div class="input-group">
                        <button id="specificStudentBtn" class="styled-button"><i class='bx bx-user'></i> Specific Student Performance</button>
                    </div>
                    <div class="input-group">
                        <button id="overallStudentBtn" class="styled-button"><i class='bx bx-group'></i> Overall Student Performance</button>
                    </div>
                </div>
            </div>
            
            <div id="specificPerformanceView" class="is-hidden">
                <div class="selection-controls">
                    <div class="input-group">
                        <label for="programSelect">Program</label>
                        <select id="programSelect" class="styled-select" disabled><option value="">Loading...</option></select>
                    </div>
                    <div class="input-group">
                        <label for="semesterSelect">Semester</label>
                        <select id="semesterSelect" class="styled-select" disabled><option value="">Select Program</option></select>
                    </div>
                     <div class="input-group">
                        <label for="examTypeSelect">Exam Type</label>
                        <select id="examTypeSelect" class="styled-select" disabled><option value="">Select Semester</option></select>
                    </div>
                   <div class="input-group" style="position: relative;">
                        <label for="studentIdInput">Student ID (Optional)</label>
                        <input type="text" id="studentIdInput" class="styled-input" placeholder="e.g., 210905" disabled>
                        <i class='bx bx-x' id="clearStudentIdBtn" style="display: none;"></i>
                    </div>
                    <div class="input-group">
                        <button id="searchButton" class="styled-button" disabled><i class='bx bx-search'></i> Search</button>
                    </div>
                    <div class="input-group">
    					<button id="resetButton" class="styled-button reset-button" disabled>
        				<i class='bx bx-reset'></i> Reset
    					</button>
					</div>
                </div>
            </div>
        </div>

        <div class="card is-hidden" id="resultsCard">
             <div id="studentSummary" class="is-hidden"></div>

             <div id="viewModeToggle" class="view-mode-toggle is-hidden">
                <input type="radio" id="visualViewRadio" name="viewMode" value="visual" checked>
                <label for="visualViewRadio"><i class='bx bxs-bar-chart-alt-2'></i> Visual View</label>
                <input type="radio" id="tableViewRadio" name="viewMode" value="table">
                <label for="tableViewRadio"><i class='bx bx-table'></i> Tabular View</label>
            </div>
            
            <div id="performanceDisplay">
                 <div class="loading-indicator is-hidden" id="loadingIndicator">
                    <i class='bx bx-loader-alt bx-spin'></i>
                    <p>Fetching Analytics...</p>
                </div>
            </div>
            <div id="facultyAnalysisBox"></div>
        </div>
    </div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const qs = (selector) => document.querySelector(selector);
    const qsa = (selector) => document.querySelectorAll(selector);
    
    const facultyAnalysisBox = qs('#facultyAnalysisBox'); 
    const analyticsBackButton = qs('#analyticsBackButton'); 
    const resetButton = qs('#resetButton');

    const programSelect = qs('#programSelect');
    const semesterSelect = qs('#semesterSelect');
    const studentIdInput = qs('#studentIdInput');
    const examTypeSelect = qs('#examTypeSelect');
    const searchButton = qs('#searchButton');
    const performanceDisplay = qs('#performanceDisplay');
    const loadingIndicator = qs('#loadingIndicator');
    const messageBox = qs('#messageBox');
    const resultsCard = qs('#resultsCard');
    const viewModeToggle = qs('#viewModeToggle');
    const initialChoiceView = qs('#initialChoiceView');
    const specificPerformanceView = qs('#specificPerformanceView');
    const specificStudentBtn = qs('#specificStudentBtn');
    const overallStudentBtn = qs('#overallStudentBtn');
    const studentSummary = qs('#studentSummary');
    const clearStudentIdBtn = qs('#clearStudentIdBtn');

    let studentCharts = {};
    let currentStudentData = null;
    const EXAM_TYPES = [
        { value: "Internal Assessment 1", text: "Internal Assessment 1" },
        { value: "Internal Assessment 2", text: "Internal Assessment 2" },
        { value: "SEE", text: "SEE (Semester End Exam)" }
    ];


    const showMessage = (message, type = 'info') => {
        messageBox.textContent = message;
        messageBox.className = `show ${type}`;
        setTimeout(() => messageBox.classList.remove('show'), 3500);
    };

    const populateDropdown = (selectEl, options, defaultText) => {
        selectEl.innerHTML = `<option value="" disabled selected>${defaultText}</option>`;
        if (options && options.length > 0) {
            options.forEach(opt => {
                selectEl.innerHTML += `<option value="${opt.value}">${opt.text}</option>`;
            });
            selectEl.disabled = false;
        } else {
            selectEl.disabled = true;
        }
    };
    const resetFilters = () => {
        programSelect.selectedIndex = 0;
        populateDropdown(semesterSelect, [], 'Select Program');
        populateDropdown(examTypeSelect, [], 'Select Semester');
        studentIdInput.value = '';
        studentIdInput.dispatchEvent(new Event('input')); // To hide the 'x'
        clearResults();
        updateControlStates();
        showMessage('Filters have been reset.', 'info');
    };
    
    const updateControlStates = () => {
        const isProgram = !!programSelect.value;
        const isSemester = !!semesterSelect.value;
        const isExam = !!examTypeSelect.value;
        const isStudentId = !!studentIdInput.value;

        semesterSelect.disabled = !isProgram;
        examTypeSelect.disabled = !isSemester;
        studentIdInput.disabled = !isSemester;
        searchButton.disabled = !(isProgram && isSemester && isExam);

        // This is the new line to control the reset button
        resetButton.disabled = !(isProgram || isSemester || isExam || isStudentId);
    };

    const clearResults = () => {
        performanceDisplay.innerHTML = '';
        studentSummary.innerHTML = '';
        studentSummary.classList.add('is-hidden');
        resultsCard.classList.add('is-hidden');
        viewModeToggle.classList.add('is-hidden');
        Object.values(studentCharts).forEach(chart => chart.destroy());
        studentCharts = {};
        currentStudentData = null;
    };

    const fetchData = async (endpoint, body = null) => {
        try {
            const options = body ? { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) } : {};
            const response = await fetch(endpoint, options);
            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: 'Server error with no details.' }));
                throw new Error(error.message || `HTTP error! Status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            showMessage(error.message, 'error');
            console.error('Fetch Error:', error);
            return null;
        }
    };

    const loadPrograms = async () => {
        const data = await fetchData('FetchProgramsServlet');
        if (data && data.status === 'success') {
            const programOptions = data.programs.map(p => ({ value: p.id, text: p.name }));
            populateDropdown(programSelect, programOptions, 'Select a Program');
        } else {
            populateDropdown(programSelect, [], 'Error loading programs');
        }
        updateControlStates();
    };
    
    const loadSemesters = async (programId) => {
        const data = await fetchData('FetchProgramSemestersServlet', { programId });
        if (data && data.status === 'success') {
            const semesterOptions = data.semesters.map(s => ({ value: s, text: `Semester ${s}` }));
            populateDropdown(semesterSelect, semesterOptions, 'Select a Semester');
        } else {
            populateDropdown(semesterSelect, [], 'No semesters found');
        }
        updateControlStates();
    };
    
    // --- RESTORED: This function renders the list of students ---
    const renderStudentList = (students) => {
        let tableHTML = `<div class="table-responsive"><table class="styled-table">
            <thead><tr><th>ID</th><th>Name</th><th>Program</th><th>Semester</th><th>Action</th></tr></thead><tbody>`;
        
        students.forEach(s => {
            // Assumes student object has studentId, fullName, programName, semester
            tableHTML += `<tr>
                <td>${s.studentId}</td>
                <td>${s.fullName || 'N/A'}</td>
                <td>${s.programName || 'N/A'}</td>
                <td>${s.semester}</td>
                <td><button class="styled-button view-single" data-student-id="${s.studentId}">View Performance</button></td>
            </tr>`;
        });
        tableHTML += `</tbody></table></div>`;
        performanceDisplay.innerHTML = tableHTML;
    };

 // ==================================================================
    //  REPLACE THIS ENTIRE FUNCTION
    // ==================================================================
const renderStudentSummary = (student, examType) => {
        const { studentId, studentName, programName, semester, coursePerformances } = student;

        // Helper function to get a performance remark based on percentage
        const getPerformanceRemark = (percentage) => {
            if (percentage === 100) return '<span style="color: #00c853;">Legendary</span>';
            if (percentage >= 90) return '<span style="color: #00e676;">Excellent</span>';
            if (percentage >= 80) return '<span style="color: #64dd17;">Great</span>';
            if (percentage >= 70) return '<span style="color: #aeea00;">Very Good</span>';
            if (percentage >= 60) return '<span style="color: #ffd600;">Good</span>';
            if (percentage >= 50) return '<span style="color: #ffab00;">Could Be Better</span>';
            if (percentage >= 40) return '<span style="color: #ff6d00;">Pass</span>';
            return '<span style="color: #d50000;">Fail</span>';
        };

        let summaryHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 0.75rem 1.5rem;">
                <div><strong>ID:</strong> ${studentId} (${studentName})</div>
                <div><strong>Program:</strong> ${programName}</div>
                <div><strong>Semester:</strong> ${semester}</div>
            </div>`;

        let performanceHTML = '';

        // --- Logic for Individual CIE Views (CIE-1 or CIE-2) ---
        if (examType === 'Internal Assessment 1' || examType === 'Internal Assessment 2') {
            let totalMarks = 0;
            let totalMaxMarks = 0;
            const marksKey = (examType === 'Internal Assessment 1') ? 'ia1Marks' : 'ia2Marks';

            coursePerformances.forEach(c => {
                if (c[marksKey] != null) {
                    totalMarks += c[marksKey];
                    totalMaxMarks += 50; // Max marks for each CIE is 50
                }
            });

            if (totalMaxMarks > 0) {
                const percentage = (totalMarks / totalMaxMarks) * 100;
                const result = (percentage >= 40) ? '<span style="color: var(--success-color);">Pass</span>' : '<span style="color: var(--danger-color);">Fail</span>';
                performanceHTML = `<div><strong>Overall ${examType} Result:</strong><b> ${result} </b>(${percentage.toFixed(2)}%)</div>`;
            } else {
                performanceHTML = `<div><strong>Overall ${examType} Result:</strong> N/A</div>`;
            }
        }
        // --- Logic for Final SEE View ---
        else if (examType === 'SEE') {
            let totalCieObtained = 0;
            let totalCieMax = 0;
            let totalFinalObtained = 0;
            let totalFinalMax = 0;

            coursePerformances.forEach(c => {
                // Calculate for Overall CIE
                if (c.ia1Marks != null && c.ia2Marks != null) {
                    totalCieObtained += c.ia1Marks + c.ia2Marks;
                    totalCieMax += 100; // 50 for each CIE
                }
                // Calculate for Overall Final/SEE
                const combinedCieForFinal = (c.ia1Marks != null && c.ia2Marks != null) ? (c.ia1Marks + c.ia2Marks) / 5 : null;
                if (combinedCieForFinal != null && c.seeMarks != null) {
                    totalFinalObtained += combinedCieForFinal + c.seeMarks;
                    totalFinalMax += 100; // 20 from CIE + 80 from SEE
                }
            });

            let cieRemark = "N/A";
            if (totalCieMax > 0) {
                const ciePercentage = (totalCieObtained / totalCieMax) * 100;
                cieRemark = getPerformanceRemark(ciePercentage);
            }

            let seeRemark = "N/A";
            if (totalFinalMax > 0) {
                const seePercentage = (totalFinalObtained / totalFinalMax) * 100;
                seeRemark = getPerformanceRemark(seePercentage);
            }
            
            performanceHTML = `
                <div><strong>Overall CIE Performance:</strong><b> ${cieRemark}</b></div>
                <div><strong>Overall Final Performance:</strong><b> ${seeRemark}</b></div>
            `;
        }
        
        // Append the performance summary to the main summary
        studentSummary.innerHTML = summaryHTML.slice(0, -6) + performanceHTML + `</div>`; // Injects performance before closing div
        studentSummary.classList.remove('is-hidden');
    };

    // ==================================================================
    //  REPLACE THIS ENTIRE FUNCTION
    // ==================================================================
    const renderStudentChart = (student, examType) => {
        const chartWrapper = document.createElement('div');
        chartWrapper.className = 'chart-container';
        const canvas = document.createElement('canvas');
        chartWrapper.appendChild(canvas);
        performanceDisplay.innerHTML = '';
        performanceDisplay.appendChild(chartWrapper);

        const CHART_COLORS = {
            ia1: 'rgba(88, 86, 214, 0.8)',
            ia2: 'rgba(255, 69, 58, 0.8)',
            cie: 'rgba(48, 209, 88, 0.8)',
            see: 'rgba(0, 122, 255, 0.8)',
            attendanceLine: 'rgba(255, 159, 10, 1)',
            attendanceBg: 'rgba(255, 159, 10, 0.2)',
            grid: 'rgba(142, 142, 147, 0.2)'
        };

        const labels = student.coursePerformances.map(c => `${c.courseCode} (${c.subjectName})`);
        const attendance = student.coursePerformances.map(c => (c.totalClassesHeld > 0) ? (c.classesAttended / c.totalClassesHeld) * 100 : 0);
        let datasets = [];
        let yAxisMax = 100;
        let yAxisTitle = 'Marks';

        if (examType === "Internal Assessment 1") {
            yAxisMax = 50;
            yAxisTitle = 'IA 1 Marks (out of 50)';
            datasets.push({ label: 'IA 1 Marks', data: student.coursePerformances.map(c => c.ia1Marks), backgroundColor: CHART_COLORS.ia1, borderRadius: 6 });
        } else if (examType === "Internal Assessment 2") {
            yAxisMax = 50;
            yAxisTitle = 'IA 2 Marks (out of 50)';
            datasets.push({ label: 'IA 2 Marks', data: student.coursePerformances.map(c => c.ia2Marks), backgroundColor: CHART_COLORS.ia2, borderRadius: 6 });
        } else if (examType === "SEE") { // CORRECTED CHECK
            yAxisMax = 100;
            yAxisTitle = 'Marks';
            datasets.push({ label: 'CIE Marks (Avg)', data: student.coursePerformances.map(c => (c.ia1Marks != null && c.ia2Marks != null) ? (c.ia1Marks + c.ia2Marks) / 2 : null), backgroundColor: CHART_COLORS.cie, borderRadius: 4 });
            datasets.push({ label: 'SEE Marks (out of 80)', data: student.coursePerformances.map(c => c.seeMarks), backgroundColor: CHART_COLORS.see, borderRadius: 4 });
        }

        datasets.push({ 
            type: 'line', label: 'Attendance %', data: attendance,
            borderColor: CHART_COLORS.attendanceLine, backgroundColor: CHART_COLORS.attendanceBg,
            yAxisID: 'y1', tension: 0.4, pointBackgroundColor: CHART_COLORS.attendanceLine, fill: true
        });

        if(studentCharts['main']) studentCharts['main'].destroy();

        studentCharts['main'] = new Chart(canvas.getContext('2d'), {
            type: 'bar', 
            data: { labels, datasets },
            options: {
                responsive: true, maintainAspectRatio: true,
                plugins: {
                    legend: { position: 'top', align: 'end' },
                    title: { display: true, text: `Performance Analytics for ${student.studentName} - ${examType}`, align: 'start', font: { size: 18, weight: '600' } }
                },
                scales: {
                    y: { beginAtZero: true, max: yAxisMax, title: { display: true, text: yAxisTitle }, grid: { color: CHART_COLORS.grid } },
                    y1: { type: 'linear', display: true, position: 'right', beginAtZero: true, max: 100, title: { display: true, text: 'Attendance (%)' }, grid: { drawOnChartArea: false } },
                    x: { grid: { display: false } }
                }
            }
        });
    };
    
    // ==================================================================
    //  REPLACE THIS ENTIRE FUNCTION
    // ==================================================================
    const renderStudentTable = (student, examType) => {
        let headers = [];
        let tableHTML = '';

        if (examType === 'Internal Assessment 1' || examType === 'Internal Assessment 2') {
            headers = ['Course', 'Name', 'Max Marks', 'Obtained Marks', 'Attendance %'];
            tableHTML = `<div class="table-responsive"><table class="styled-table">
                <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>`;
            
            student.coursePerformances.forEach(c => {
                const marksField = (examType === 'Internal Assessment 1') ? c.ia1Marks : c.ia2Marks;
                const attendancePercentage = (c.totalClassesHeld > 0) ? (c.classesAttended / c.totalClassesHeld) * 100 : 0;
                tableHTML += `<tr>
                    <td>${c.courseCode}</td>
                    <td>${c.subjectName}</td>
                    <td>50</td>
                    <td>${marksField ?? 'N/A'}</td>
                    <td>${attendancePercentage.toFixed(2)}%</td>
                </tr>`;
            });

        } else if (examType === 'SEE') { // CORRECTED CHECK
            headers = ['Course', 'Name', 'Combined CIE', 'SEE Marks', 'Total', 'Percentage', 'Attendance %'];
            tableHTML = `<div class="table-responsive"><table class="styled-table">
                <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>`;

            student.coursePerformances.forEach(c => {
                const attendancePercentage = (c.totalClassesHeld > 0) ? (c.classesAttended / c.totalClassesHeld) * 100 : 0;
                const combinedCieMarks = (c.ia1Marks != null && c.ia2Marks != null) ? (c.ia1Marks + c.ia2Marks) / 2 : null;
                const combinedCieForFinal = (c.ia1Marks != null && c.ia2Marks != null) ? (c.ia1Marks + c.ia2Marks) / 5 : null;
                const finalTotalMarks = (combinedCieForFinal != null && c.seeMarks != null) ? combinedCieForFinal + c.seeMarks : null;
                const finalPercentage = (finalTotalMarks != null) ? (finalTotalMarks / 100) * 100 : null;

                tableHTML += `<tr>
                    <td>${c.courseCode}</td>
                    <td>${c.subjectName}</td>
                    <td>${combinedCieMarks?.toFixed(1) ?? 'N/A'} / 50</td>
                    <td>${c.seeMarks ?? 'N/A'} / 80</td>
                    <td>${finalTotalMarks?.toFixed(1) ?? 'N/A'} / 100</td>
                    <td>${finalPercentage?.toFixed(2) ?? 'N/A'}%</td>
                    <td>${attendancePercentage.toFixed(2)}%</td>
                </tr>`;
            });
        }
        
        tableHTML += '</tbody></table></div>';
        performanceDisplay.innerHTML = tableHTML;
        renderPerformanceAnalysis(student, examType);
    };

    // ==================================================================
    //  ADD THIS NEW FUNCTION AT THE END OF YOUR SCRIPT
    // ==================================================================
    const renderPerformanceAnalysis = (student, examType) => {
        const analysisBox = qs('#facultyAnalysisBox');
        if (!student || !student.coursePerformances || student.coursePerformances.length === 0) {
            analysisBox.innerHTML = '';
            return;
        }

        let totalMarks = 0, totalMaxMarks = 0, totalAttended = 0, totalHeld = 0;
        const marksKey = (examType === 'Internal Assessment 1') ? 'ia1Marks' : 'ia2Marks';

        student.coursePerformances.forEach(c => {
            if (examType.startsWith('Internal')) {
                if (c[marksKey] != null) {
                    totalMarks += c[marksKey];
                    totalMaxMarks += 50;
                }
            }
            totalAttended += c.classesAttended || 0;
            totalHeld += c.totalClassesHeld || 0;
        });

        const marksPercentage = (totalMaxMarks > 0) ? (totalMarks / totalMaxMarks) * 100 : 0;
        const attendancePercentage = (totalHeld > 0) ? (totalAttended / totalHeld) * 100 : 0;

        let message = '';
        let type = 'info';
        let icon = 'bx-info-circle';

        if (examType === 'SEE') {
            const finalPercentage = student.coursePerformances.reduce((acc, c) => {
                const combinedCieForFinal = (c.ia1Marks != null && c.ia2Marks != null) ? (c.ia1Marks + c.ia2Marks) / 5 : null;
                const finalTotalMarks = (combinedCieForFinal != null && c.seeMarks != null) ? combinedCieForFinal + c.seeMarks : null;
                return acc + (finalTotalMarks || 0);
            }, 0) / student.coursePerformances.length;

            if (finalPercentage >= 40) {
                type = 'success';
                icon = 'bx-party';
                message = `<strong>Congratulations on Passing!</strong> A strong performance in the final exams. Keep up the excellent work and momentum for the next semester.`;
            } else {
                type = 'warning';
                icon = 'bx-bulb';
                message = `<strong>Result Pending Improvement.</strong> The final exam scores indicate some challenges. Let's review the key subjects and prepare a strategy for the supplementary exams.`;
            }
        } else { // Handle CIE-1 and CIE-2
            const isGoodMarks = marksPercentage >= 60;
            const isGoodAttendance = attendancePercentage >= 75;
            const isLowAttendance = attendancePercentage < 50;

            if (isGoodMarks && isGoodAttendance) {
                type = 'success'; icon = 'bx-like';
                message = `<strong>Excellent Balance!</strong> The student demonstrates strong academic performance and consistent attendance. A great foundation for success.`;
            } else if (isGoodMarks && !isGoodAttendance) {
                type = 'info'; icon = 'bx-user-voice';
                message = `<strong>Academically Strong, but Attendance is a Concern.</strong> The student scores well but is missing classes. Regular attendance is key to maintaining high performance.`;
            } else if (!isGoodMarks && isGoodAttendance) {
                type = 'warning'; icon = 'bx-book-open';
                message = `<strong>Highly Attentive, Needs Academic Focus.</strong> The student is regular to class but struggling with scores. Suggest focusing on study techniques and clearing doubts.`;
            } else { // Low marks and low attendance
                if(isLowAttendance) {
                    type = 'danger'; icon = 'bx-error-alt';
                    message = `<strong>Critical Concern.</strong> Both attendance and marks are very low. This indicates a significant lack of engagement. Immediate intervention is recommended.`;
                } else {
                    type = 'warning'; icon = 'bx-error-circle';
                    message = `<strong>Needs Improvement on Both Fronts.</strong> Attendance is below the required 75%, and marks are not up to the standard. A discussion about focus and discipline is needed.`;
                }
            }
        }
        
        analysisBox.className = `analysis-box ${type}`;
        analysisBox.innerHTML = `<i class='bx ${icon}'></i><p>${message}</p>`;
    };
    
    
    const renderSingleStudent = (student, examType) => {
        currentStudentData = { student, examType };
        viewModeToggle.classList.remove('is-hidden');
        const view = qs('input[name="viewMode"]:checked').value;

        // Call the correct render function based on the toggle
        if (view === 'visual') {
            renderStudentChart(student, examType);
        } else {
            renderStudentTable(student, examType);
        }
        // This was missing before, so the analysis box only showed on chart view
        renderPerformanceAnalysis(student, examType);
    };

    const handleSearch = async () => {
        clearResults();
        resultsCard.classList.remove('is-hidden');
        loadingIndicator.classList.remove('is-hidden');

        const payload = {
            programId: programSelect.value,
            semester: semesterSelect.value,
            examType: examTypeSelect.value,
            studentId: studentIdInput.value.trim() || null
        };
        
        const data = await fetchData('FetchStudentPerformanceServlet', payload);
        loadingIndicator.classList.add('is-hidden');

        // --- CORRECTED LOGIC ---
        if (data && data.status === 'success') {
            if (data.students) { // Case for list of students
                renderStudentList(data.students);
                facultyAnalysisBox.style.display = 'none';
            
                
                
            
            } else if (data.student) { // Case for a single student
                renderStudentSummary(data.student, payload.examType);
                renderSingleStudent(data.student, payload.examType);
                facultyAnalysisBox.style.display = 'block';
                
            } else {
                performanceDisplay.innerHTML = '<p>No data found for the selected criteria.</p>';
            }
        } else {
            performanceDisplay.innerHTML = `<p>${data?.message || 'Failed to fetch data.'}</p>`;
        }
    };
    
    specificStudentBtn.addEventListener('click', () => {
        initialChoiceView.classList.add('is-hidden');
        specificPerformanceView.classList.remove('is-hidden');
        // FIX: Clear the analysis box when switching to this view
        if (facultyAnalysisBox) {
            facultyAnalysisBox.innerHTML = '';
           	
        }
        
    });

    // Listener for the new Back Button
let backClickCount = 0;
let backClickTimer = null;

if (analyticsBackButton) {
    analyticsBackButton.addEventListener('click', () => {
        backClickCount++;

        // Reset the click count after 1.5 seconds
        clearTimeout(backClickTimer);
        backClickTimer = setTimeout(() => {
            backClickCount = 0;
        }, 1500);

        // If results are visible, go back to the filters
        if (!resultsCard.classList.contains('is-hidden')) {
            resultsCard.classList.add('is-hidden');
            facultyAnalysisBox.innerHTML = '';
            facultyAnalysisBox.style.display = 'none';
            studentIdInput.value = '';

            // Only call handleSearch on the first click
            if (backClickCount === 1) {
                handleSearch(1);
            }
            return;
        }

        // If filters are visible, go back to the initial choice
        if (!specificPerformanceView.classList.contains('is-hidden')) {
            specificPerformanceView.classList.add('is-hidden');
            initialChoiceView.classList.remove('is-hidden');
            return;
        }

        // Otherwise, go back to the main faculty dashboard
        window.location.href = '<%= request.getContextPath() %>/facultydashboard';
    });
}


    // Listeners for the Clear Student ID Button
    if (studentIdInput && clearStudentIdBtn) {
    	studentIdInput.addEventListener('input', () => {
    	    clearStudentIdBtn.style.display = studentIdInput.value ? 'block' : 'none';
    	    updateControlStates(); // This ensures the reset button enables/disables correctly
    	});

        clearStudentIdBtn.addEventListener('click', () => {
            studentIdInput.value = '';
            clearStudentIdBtn.style.display = 'none';
            studentIdInput.focus();
        });
    }

    overallStudentBtn.addEventListener('click', () => {
        showMessage('Overall performance analytics for all students will be implemented soon!', 'info');
    });
    
    programSelect.addEventListener('change', () => {
        clearResults();
        populateDropdown(semesterSelect, [], 'Select Program');
        populateDropdown(examTypeSelect, [], 'Select Semester');
        loadSemesters(programSelect.value);
    });
    
    semesterSelect.addEventListener('change', () => {
        clearResults();
        populateDropdown(examTypeSelect, EXAM_TYPES, 'Select Exam Type');
        updateControlStates();
    });

    examTypeSelect.addEventListener('change', updateControlStates);
    searchButton.addEventListener('click', handleSearch);

    qsa('input[name="viewMode"]').forEach(radio => {
        radio.addEventListener('change', (e) => {
            if (currentStudentData) {
                performanceDisplay.innerHTML = '';
                Object.values(studentCharts).forEach(chart => chart.destroy());
                e.target.value === 'visual'
                    ? renderStudentChart(currentStudentData.student, currentStudentData.examType)
                    : renderStudentTable(currentStudentData.student, currentStudentData.examType);
            }
        });
    });

    // --- RESTORED: This handles the click on the "View Performance" button ---
   resultsCard.addEventListener('click', (e) => {
        if (e.target.classList.contains('view-single')) {
            studentIdInput.value = e.target.dataset.studentId;
            clearStudentIdBtn.style.display = 'block'; // Manually show the X button
            handleSearch();
        }
    });
   resetButton.addEventListener('click', resetFilters);


    loadPrograms();
    updateControlStates();
});
</script>
</body>
</html>
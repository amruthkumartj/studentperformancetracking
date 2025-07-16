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
    </style>
</head>
<body>

    <div class="container">
        <a href="<%= request.getContextPath() %>/facultydashboard" class="styled-button back-button">
            <i class='bx bx-arrow-back'></i> Back
        </a>

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
                    <div class="input-group">
                        <label for="studentIdInput">Student ID (Optional)</label>
                        <input type="text" id="studentIdInput" class="styled-input" placeholder="e.g., 210905" disabled>
                    </div>
                    <div class="input-group">
                        <button id="searchButton" class="styled-button" disabled><i class='bx bx-search'></i> Search</button>
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
        </div>
    </div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const qs = (selector) => document.querySelector(selector);
    const qsa = (selector) => document.querySelectorAll(selector);

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
    
    const updateControlStates = () => {
        const isProgram = !!programSelect.value;
        const isSemester = !!semesterSelect.value;
        const isExam = !!examTypeSelect.value;
        semesterSelect.disabled = !isProgram;
        examTypeSelect.disabled = !isSemester;
        studentIdInput.disabled = !isSemester;
        searchButton.disabled = !(isProgram && isSemester && isExam);
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

    const renderStudentSummary = (student, examType) => {
        const { studentId, studentName, programName, semester, coursePerformances, overallAnalysis } = student;
        const totalCourses = coursePerformances.length;
        let overallCie = 0, overallSee = 0;

        if (totalCourses > 0) {
            const sumCie = coursePerformances.reduce((acc, course) => acc + (course.combinedCieMarks || 0), 0);
            const sumSee = coursePerformances.reduce((acc, course) => acc + (course.seeMarks || 0), 0);
            overallCie = (sumCie / totalCourses);
            overallSee = (sumSee / totalCourses);
        }
        
        let summaryHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 0.75rem 1.5rem;">
                <div><strong>ID:</strong> ${studentId} (${studentName})</div>
                <div><strong>Program:</strong> ${programName}</div>
                <div><strong>Semester:</strong> ${semester}</div>
                <div><strong>Overall Performance:</strong> <span style="font-weight: bold; color: var(--primary-color);">${overallAnalysis || 'N/A'}</span></div>`;

        if (examType.startsWith("Internal")) {
             summaryHTML += `<div><strong>Overall CIE Result:</strong> ${overallCie.toFixed(2)} (Average Score)</div>`;
        } else if (examType === "SEE") {
             summaryHTML += `<div><strong>Overall CIE Result:</strong> ${overallCie.toFixed(2)} (Average Score)</div>`;
             summaryHTML += `<div><strong>Overall SEE Result:</strong> ${overallSee.toFixed(2)} (Average Score)</div>`;
        }
        
        summaryHTML += `</div>`;
        studentSummary.innerHTML = summaryHTML;
        studentSummary.classList.remove('is-hidden');
    };

    const renderStudentChart = (student, examType) => {
        const canvas = document.createElement('canvas');
        performanceDisplay.appendChild(canvas);

        const labels = student.coursePerformances.map(c => `${c.courseCode} (${c.subjectName})`);
        const attendance = student.coursePerformances.map(c => c.attendancePercentage);
        let chartTitle = `Performance Analytics for ${student.studentName}`;
        let datasets = [];

        if (examType === "Internal Assessment 1") {
            datasets.push({ label: 'IA 1 Marks (out of 50)', data: student.coursePerformances.map(c => c.ia1Marks), backgroundColor: 'rgba(106, 90, 205, 0.7)' });
        } else if (examType === "Internal Assessment 2") {
            datasets.push({ label: 'IA 2 Marks (out of 50)', data: student.coursePerformances.map(c => c.ia2Marks), backgroundColor: 'rgba(255, 99, 132, 0.7)' });
        } else if (examType === "SEE") {
            datasets.push({ label: 'CIE Marks (out of 50)', data: student.coursePerformances.map(c => c.combinedCieMarks), backgroundColor: 'rgba(106, 90, 205, 0.7)' });
            datasets.push({ label: 'SEE Marks (out of 100)', data: student.coursePerformances.map(c => c.seeMarks), backgroundColor: 'rgba(40, 167, 69, 0.7)' });
        }

        datasets.push({ 
            type: 'line', label: 'Attendance %', data: attendance,
            borderColor: 'rgba(255, 159, 64, 1)', backgroundColor: 'rgba(255, 159, 64, 0.2)',
            yAxisID: 'y1', tension: 0.3
        });

        studentCharts['main'] = new Chart(canvas.getContext('2d'), {
            type: 'bar', data: { labels, datasets },
            options: {
                responsive: true,
                plugins: { legend: { position: 'top' }, title: { display: true, text: `Performance for ${student.studentName} - ${examType}`, font: { size: 16 } } },
                scales: {
                    y: { beginAtZero: true, max: 100, title: { display: true, text: 'Marks' } },
                    y1: { type: 'linear', display: true, position: 'right', beginAtZero: true, max: 100, title: { display: true, text: 'Attendance (%)' }, grid: { drawOnChartArea: false } }
                }
            }
        });
    };
    
    const renderStudentTable = (student, examType) => {
        const isSeeExam = examType === 'SEE';
        const isIAExam = examType.startsWith('Internal');
        let headers = [];

        if (isIAExam) headers = ['Course', 'Name', 'Max Marks', 'Obtained Marks', 'Attendance %'];
        else if (isSeeExam) headers = ['Course', 'Name', 'CIE (50)', 'SEE (100)', 'Total (150)', 'Percentage', 'Attendance %'];

        let tableHTML = `<div class="table-responsive"><table class="styled-table">
            <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>`;

        student.coursePerformances.forEach(c => {
            tableHTML += `<tr><td>${c.courseCode}</td><td>${c.subjectName}</td>`;
            if (isIAExam) {
                const marksField = examType === 'Internal Assessment 1' ? c.ia1Marks : c.ia2Marks;
                tableHTML += `<td>50</td><td>${marksField ?? 'N/A'}</td><td>${c.attendancePercentage?.toFixed(2) ?? 'N/A'}%</td>`;
            } else if (isSeeExam) {
                tableHTML += `<td>${c.combinedCieMarks?.toFixed(2) ?? 'N/A'}</td><td>${c.seeMarks ?? 'N/A'}</td>
                              <td>${c.overallTotalMarks?.toFixed(2) ?? 'N/A'}</td><td>${c.overallPercentage?.toFixed(2) ?? 'N/A'}%</td>
                              <td>${c.attendancePercentage?.toFixed(2) ?? 'N/A'}%</td>`;
            }
            tableHTML += `</tr>`;
        });
        tableHTML += '</tbody></table></div>';
        performanceDisplay.innerHTML = tableHTML;
    };
    
    const renderSingleStudent = (student, examType) => {
        currentStudentData = { student, examType };
        viewModeToggle.classList.remove('is-hidden');
        const view = qs('input[name="viewMode"]:checked').value;
        view === 'visual' ? renderStudentChart(student, examType) : renderStudentTable(student, examType);
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
            } else if (data.student) { // Case for a single student
                renderStudentSummary(data.student, payload.examType);
                renderSingleStudent(data.student, payload.examType);
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
    });

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
            handleSearch();
        }
    });

    loadPrograms();
    updateControlStates();
});
</script>
</body>
</html>
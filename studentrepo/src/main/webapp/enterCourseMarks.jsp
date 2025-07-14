<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Enter Course Marks</title>

    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" integrity="sha512-SnH5WK+bZxgPHs44uWIX+LLJAJ9/2PkPKZ5QiAj6Ta86w+fsb2TkcmfRyVX3pBnMFcV7oQPJkl9QevSCWr3W6A==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    
    <style>
        /* CSS Variables - Ensure these are defined in sidebar.css or similar global stylesheet */
/* css/entermarks.css */

/* css/entermarks.css */

/* CSS Variables - Define them here since sidebar.css is removed */
:root {
    --body-color: #E4E9F7;
    --sidebar-color: #FFF; /* Kept for table header, etc., even if sidebar itself is gone */
    --primary-color: #007bff;
    --primary-color-light: #F6F5FF;
    --toggle-color: #DDD;
    --text-color: #383838; /* Main text color */
    --text-color-light: #666; /* Lighter text color, used for labels and placeholders */
    --border-color: #e0e0e0; /* General border color for rounded boxes */
    --input-background: #fdfdfd; /* General input background for rounded boxes */
    --panel-color: #ffffff;
    --box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    --dropdown-highlight-bg: rgba(var(--primary-color-rgb), 0.15);
    --dropdown-highlight-text: var(--text-color);
    --primary-color-rgb: 0, 123, 255;
    --text-color-rgb: 56, 56, 56; /* Added for SVG fill for light mode (though now removed for select) */
    --tran-03: all 0.3s ease;
    --tran-04: all 0.3s ease;
    --tran-05: all 0.5s ease;

    /* Specific for message boxes and buttons */
    --danger-color: #dc3545;
    --success-color: #28a745;
}

/* Dark Mode Variables */
body.dark {
    --body-color: #1a1a2e;
    --sidebar-color: #2c2c54;
    --primary-color: #6a8cdb;
    --primary-color-light: #4a6bbd;
    --toggle-color: #444;
    --text-color: #f8f9fa; /* Main text color in dark mode */
    --text-color-light: #ced4da; /* Lighter text color in dark mode, used for labels and placeholders */
    --border-color: #444; /* Dark mode border color */
    --input-background: #3a3a60; /* Dark mode input background */
    --panel-color: #2c2c54;
    --box-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
    --dropdown-highlight-bg: rgba(var(--primary-color-rgb), 0.3);
    --dropdown-highlight-text: var(--text-color);
    --primary-color-rgb: 106, 140, 219;
    --text-color-rgb: 248, 249, 250; /* Added for SVG fill for dark mode (though now removed for select) */
}

/* General Body and Container Styles */
body {
    min-height: 100vh;
    background-color: var(--body-color);
    transition: background-color var(--tran-05), color var(--tran-05);
    font-family: 'Poppins', sans-serif;
    margin: 0;
    padding: 20px; /* General padding for the whole page */
    display: flex;
    flex-direction: column;
    align-items: center; /* Center the container horizontally */
}

.container {
    width: 100%;
    max-width: 960px; /* Standard max width for content */
    margin: 20px auto; /* Centered with vertical margin */
    background: var(--panel-color);
    padding: 25px 30px; /* Adjusted padding */
    border-radius: 10px; /* Slightly more rounded corners for container */
    box-shadow: var(--box-shadow);
    box-sizing: border-box;
    transition: background-color var(--tran-03), box-shadow var(--tran-03);
}

/* Page Titles and General Text */
#pageTitle {
    font-size: 2.2rem; /* Larger title */
    font-weight: 600;
    margin-bottom: 15px;
    color: var(--text-color);
    text-align: center;
    transition: color var(--tran-03);
}
#courseDetails {
    text-align: center;
    font-size: 1rem;
    color: var(--text-color-light);
    margin-bottom: 25px;
}
h2, h3 { /* General headings within the container */
    color: var(--text-color);
    margin-bottom: 20px;
    font-size: 1.8rem;
    transition: color var(--tran-03);
}

/* Buttons */
.btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    padding: 0.75rem 1.5rem; /* Increased padding */
    font-size: 1rem;
    border-radius: 8px; /* More rounded buttons */
    transition: all var(--tran-03);
    text-decoration: none;
    justify-content: center;
    cursor: pointer;
    border: 1px solid transparent;
}
.btn-primary {
    color: white; /* Always white text on primary button */
    background-color: var(--primary-color);
    border-color: var(--primary-color);
    box-shadow: 0 4px 8px rgba(var(--primary-color-rgb), 0.2);
}
.btn-primary:hover {
    filter: brightness(1.1); /* Slightly brighter on hover */
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(var(--primary-color-rgb), 0.3);
}
.btn-success {
    color: #fff;
    background-color: var(--success-color);
    border-color: var(--success-color);
    box-shadow: 0 4px 8px rgba(40, 167, 69, 0.2);
}
.btn-success:hover {
    filter: brightness(1.1);
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(40, 167, 69, 0.3);
}
.btn-secondary {
    color: var(--text-color);
    background-color: var(--toggle-color); /* Use toggle-color for secondary buttons */
    border-color: var(--toggle-color);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}
body.dark .btn-secondary {
    background-color: var(--border-color); /* Darker neutral background */
    border-color: var(--border-color);
}
.btn-secondary:hover {
    filter: brightness(1.1);
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}
.btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none; /* No transform when disabled */
    box-shadow: none; /* No shadow when disabled */
}
.btn-back {
    margin-bottom: 20px;
    align-self: flex-start; /* Align to the left within the container */
}


/* Form Controls (input, select) - ROUNDED BOX DESIGN, NO ARROW */
.form-control,
.form-select {
    display: block;
    width: 100%;
    padding: 12px 15px; /* Consistent padding */
    font-size: 1rem;
    line-height: 1.5;
    color: var(--text-color);
    background-color: var(--input-background); /* Background color for the rounded box */
    border: 1px solid var(--border-color); /* Outer border for the rounded box */
    border-radius: 6px; /* Rounded corners for the box */
    transition: border-color var(--tran-03), box-shadow var(--tran-03), background-color var(--tran-03), color var(--tran-03);
    box-sizing: border-box;

    -webkit-appearance: none; /* Remove default arrow */
    -moz-appearance: none; /* Remove default arrow */
    appearance: none; /* Remove default arrow */
    /* Explicitly remove any custom background image for the arrow */
    background-image: none;
    padding-right: 15px; /* Adjust padding if no arrow space needed */
}

/* Dark mode specific for form controls */
body.dark .form-control,
body.dark .form-select {
    background-color: var(--input-background);
    border-color: var(--border-color);
}

.form-control:focus,
.form-select:focus {
    outline: none; /* Remove default browser outline */
    border-color: var(--primary-color); /* Change border color on focus */
    box-shadow: 0 0 0 3px rgba(var(--primary-color-rgb), 0.25); /* Soft glow on focus */
    background-color: var(--input-background); /* Keep consistent background on focus */
}

.form-control::placeholder {
    color: var(--text-color-light);
    opacity: 0.7;
}

/* Specific styling for dropdown options to ensure proper contrast/highlight */
.form-select option {
    /* These styles apply to the options *within* the dropdown list, not the select box itself */
    background-color: var(--panel-color); /* Background for individual options in the dropdown list */
    color: var(--text-color); /* Text color for individual options */
}
.form-select option:checked, /* The currently selected option in the opened dropdown list */
.form-select option:hover { /* When hovering over an option in the dropdown list */
    background-color: var(--dropdown-highlight-bg); /* Use the highlight background */
    color: var(--dropdown-highlight-text); /* Use the highlight text color */
}
/* Dark mode styles for options */
body.dark .form-select option {
    background-color: var(--panel-color); /* Darker background for options in dark mode */
    color: var(--text-color); /* Light text for options in dark mode */
}
body.dark .form-select option:checked,
body.dark .form-select option:hover {
    background-color: var(--dropdown-highlight-bg); /* Darker highlight for dark mode */
    color: var(--dropdown-highlight-text); /* Light text on highlight */
}

/* Ensure the disabled selected option's text is visible as a placeholder */
.form-select option[disabled][selected] {
    color: var(--text-color-light); /* Use a lighter text color for the placeholder */
    background-color: var(--panel-color); /* Ensure background is consistent with options */
}

/* For the placeholder specifically when nothing is selected (value is empty string) */
.form-select[value=""] {
    color: var(--text-color-light); /* Use lighter color for placeholder text */
}

/* When an actual value is selected (meaning value is not empty) */
.form-select:not([value=""]) {
    color: var(--text-color); /* Use primary text color for actual selection */
}

/* Form Layout */
.table-filters {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); /* Adjusted min-width for filters */
    gap: 20px; /* Increased gap */
    margin-bottom: 25px;
    padding: 20px; /* Increased padding */
    background-color: var(--panel-color);
    border-radius: 10px;
    box-shadow: var(--box-shadow);
    transition: background-color var(--tran-03), box-shadow var(--tran-03);
}
.form-group {
    display: flex;
    flex-direction: column;
    gap: 8px; /* Space between label and input */
}

/* IMPORTANT FIX: Ensure form-label color is always visible */
.form-label {
    font-weight: 500;
    color: var(--text-color); /* Use --text-color for a clear, readable label */
    margin-bottom: 0;
    transition: color var(--tran-03);
    opacity: 1 !important; /* Ensure opacity is always 1, override any potential JS/other CSS issues */
}


/* Message Box */
.message-box {
    padding: 15px;
    border-radius: 8px;
    border: 1px solid var(--border-color);
    margin-top: 20px;
    font-weight: 500;
    font-size: 0.95rem;
    display: flex;
    align-items: center;
    gap: 10px;
    opacity: 0;
    transform: translateY(-20px);
    transition: all 0.3s ease-out;
    display: none; /* Crucial for initial state */
}
.message-box.show {
    opacity: 1;
    transform: translateY(0);
    display: flex; /* Explicitly set to flex when 'show' class is added */
}
.message-box i {
    font-size: 1.3rem; /* Slightly larger icons */
}
.message-box.success {
    background: #d4edda;
    border-color: #28a745;
    color: #155724;
}
.message-box.error {
    background: #f8d7da;
    border-color: #dc3545;
    color: #721c24;
}
/* Dark mode message box colors */
body.dark .message-box.success {
    background: #1f3d2e; /* Darker green */
    border-color: #00a000;
    color: #00a000;
}
body.dark .message-box.error {
    background: #4d2f2f; /* Darker red */
    border-color: #ff4a4a;
    color: #ff4a4a;
}

.loading-message {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--text-color-light);
    font-weight: normal;
}
.spinner {
    border: 4px solid rgba(0, 0, 0, 0.1);
    border-left-color: var(--primary-color);
    border-radius: 50%;
    width: 24px; /* Larger spinner */
    height: 24px;
    animation: spin 1s linear infinite;
}
body.dark .spinner {
    border-color: rgba(255, 255, 255, 0.1);
    border-left-color: var(--primary-color);
}
@keyframes spin {
    to { transform: rotate(360deg); }
}

/* Table Styling */
.table-responsive {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    margin-top: 30px; /* More space above table */
    border-radius: 10px;
    border: 1px solid var(--border-color);
    box-shadow: var(--box-shadow); /* Table also gets shadow */
    background-color: var(--panel-color); /* Table background */
}
.table {
    width: 100%;
    min-width: 600px; /* Ensure a minimum width for readability */
    border-collapse: collapse;
    margin-bottom: 0;
    color: var(--text-color);
}
.table th, .table td {
    padding: 12px 15px; /* Consistent padding */
    vertical-align: middle;
    border: 1px solid var(--border-color);
    text-align: left;
    font-size: 0.95rem;
}
.table thead th {
    background-color: var(--primary-color-light); /* Header background */
    color: var(--text-color); /* Text color on header */
    font-weight: 600;
    white-space: nowrap;
}
body.dark .table thead th {
    background-color: var(--primary-color-light);
    color: var(--text-color);
}

.table-striped tbody tr:nth-of-type(odd) {
    background-color: var(--body-color); /* Lighter background for odd rows */
}
.table-striped tbody tr:nth-of-type(even) {
    background-color: var(--panel-color); /* Darker background for even rows */
}
.table-striped tbody tr:hover {
    background-color: rgba(var(--primary-color-rgb), 0.1);
}
body.dark .table-striped tbody tr:hover {
    background-color: rgba(var(--primary-color-rgb), 0.2); /* Slightly more prominent hover in dark mode */
}

/* Submission Dialog */
.submission-dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.3s ease, visibility 0.3s ease;
}
.submission-dialog-overlay.show {
    opacity: 1;
    visibility: visible;
}
.submission-dialog-content {
    background: var(--panel-color);
    padding: 30px;
    border-radius: 10px;
    box-shadow: var(--box-shadow);
    text-align: center;
    max-width: 400px;
    width: 90%;
    transform: translateY(-50px);
    opacity: 0;
    transition: transform 0.3s ease, opacity 0.3s ease;
}
.submission-dialog-overlay.show .submission-dialog-content {
    transform: translateY(0);
    opacity: 1;
}
.submission-dialog-content i {
    font-size: 3.5rem; /* Larger icons */
    margin-bottom: 15px;
}
.submission-dialog-content .success-icon {
    color: var(--success-color);
}
.submission-dialog-content .error-icon {
    color: var(--danger-color);
}
.submission-dialog-content h3 {
    margin-bottom: 10px;
    color: var(--text-color);
    font-size: 1.8rem;
}
.submission-dialog-content p {
    margin-bottom: 25px;
    color: var(--text-color-light);
}
.submission-dialog-content .btn {
    padding: 0.8rem 1.5rem;
    font-size: 1.05rem;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    body {
        padding: 10px;
    }
    .container {
        margin: 10px 0;
        padding: 15px;
        border-radius: 8px;
    }
    #pageTitle {
        font-size: 1.8rem;
        margin-bottom: 10px;
    }
    .btn-back {
        padding: 0.6rem 1rem;
        font-size: 0.9rem;
    }
    .table-filters {
        grid-template-columns: 1fr;
        gap: 15px;
        padding: 15px;
    }
    .filter-buttons {
        flex-direction: column; /* Stack buttons vertically on small screens */
        gap: 10px;
    }
    .filter-buttons .btn {
        width: 100%; /* Full width buttons */
        min-width: unset;
    }
    .table th, .table td {
        padding: 10px 12px;
        font-size: 0.85rem;
    }
    .table-responsive {
        margin-top: 20px;
        border-radius: 8px;
    }
    .submission-dialog-content {
        padding: 20px;
    }
    .submission-dialog-content h3 {
        font-size: 1.5rem;
    }
}

/* ADDED/UPDATED CSS for .filter-buttons to make buttons inline */
.filter-buttons {
    display: flex; /* Make it a flex container */
    gap: 15px;    /* Add space between buttons */
    justify-content: flex-start; /* Align buttons to the start of the container */
    align-items: center; /* Vertically align items in the middle if they have different heights */
}

    </style>
</head>
<body>
    <script>
        // Make facultyId available globally for faculty.js
       window.currentFacultyId = "<c:out value='${sessionScope.user.id}'/>";

        // Theme management
        function applyTheme() {
            const savedTheme = localStorage.getItem('theme');
            if (savedTheme) {
                document.body.classList.add(savedTheme);
            } else {
                // Default to light if no theme saved, or if you prefer dark as a default:
                // document.body.classList.add('dark');
                document.body.classList.remove('dark'); // Default to light if nothing saved
            }
        }

        // --- FIX: Apply theme as early as possible after body is available to prevent FOUC ---
        applyTheme();
    </script>

    <div class="container">
        <button onclick="window.history.back()" class="btn btn-primary btn-back">
            <i class='bx bx-arrow-back'></i> Back to Marks Options
        </button>

        <h2 id="pageTitle">Enter Marks</h2>
        <p id="courseDetails" style="display:none;">Loading details...</p>

        <div class="table-filters">
            <div class="form-group">
                <label for="marksProgramFilterDropdown" class="form-label">Program:</label>
                <select class="form-select" id="marksProgramFilterDropdown">
                    <option value="" disabled selected>Select Program</option>
                </select>
            </div>
            <div class="form-group">
                <label for="marksSemesterFilterDropdown" class="form-label">Semester:</label>
                <select class="form-select" id="marksSemesterFilterDropdown" disabled>
                    <option value="" disabled selected>Select Semester</option>
                </select>
            </div>
            <div class="form-group">
                <label for="marksSubjectFilterDropdown" class="form-label">Subject/Course:</label>
                <select class="form-select" id="marksSubjectFilterDropdown" disabled>
                    <option value="" disabled selected>Select Subject</option>
                </select>
            </div>
            <div class="form-group">
                <label for="marksExamTypeDropdown" class="form-label">Exam Type:</label>
                <select class="form-select" id="marksExamTypeDropdown" disabled>
                    <option value="" disabled selected>Select Exam Type</option>
                    <option value="Internal Assessment 1">Internal Assessment 1</option>
                    <option value="Internal Assessment 2">Internal Assessment 2</option>
                    <option value="SEE (Semester End Examination)">SEE (Semester End Examination)</option>
                </select>
            </div>
            <div class="filter-buttons">
                <button id="loadStudentsForMarksBtn" class="btn btn-primary" disabled>Load Students</button>
                <button id="resetMarksFiltersBtn" class="btn btn-secondary"><i class='bx bx-reset'></i> Reset Filters</button>
            </div>
        </div>
        <div id="marksEntryMessage" class="message-box"></div>


        <div id="marksEntryTableContainer" class="table-responsive" style="display:none;">
            <form id="marksEntryForm">
                <table class="table table-bordered table-striped">
                    <thead class="table-dark">
                        <tr>
                            <th>Student ID</th>
                            <th>Student Name</th>
                            <th>Marks Obtained (0-100)</th>
                        </tr>
                    </thead>
                    <tbody id="marksStudentsTableBody">
                        <tr><td colspan="3" style="text-align:center;padding:20px;"><div class="loading-message"><div class="spinner"></div> Loading students...</div></td></tr>
                    </tbody>
                </table>
                <button type="submit" id="submitMarksBtn" class="btn btn-success mt-3">Submit Marks</button>
            </form>
        </div>
    </div>

    <div id="submissionDialogOverlay" class="submission-dialog-overlay">
        <div class="submission-dialog-content">
            <div id="dialogIcon"></div>
            <h3 id="dialogTitle"></h3>
            <p id="dialogMessage"></p>
            <button id="dialogCloseBtn" class="btn btn-primary">Close</button>
        </div>
    </div>


<script>
        // Helper functions
function qs(selector) { return document.querySelector(selector); }

        // Corrected: Removed setTimeout for display: none here.
        // The responsibility of delaying 'none' is now solely with the calling function (showMessage).
        function show(el, display) {
            if (el) {
                if (display) {
                    el.style.display = 'block';
                    el.offsetWidth; // Force reflow
                    el.classList.add('show');
                } else {
                    el.classList.remove('show');
                    // Immediately set display to none. The *delay* before calling show(false)
                    // will be handled by the caller (showMessage/showSubmissionDialog).
                    el.style.display = 'none';
                }
            }
        }

        // Corrected: Removed setTimeout for display: none here.
        // The responsibility of delaying 'none' is now solely with the calling function (showMessage).
        function showFlex(el, display) {
            if (el) {
                if (display) {
                    el.style.display = 'flex';
                    el.offsetWidth; // Force reflow
                    el.classList.add('show');
                } else {
                    el.classList.remove('show');
                    // Immediately set display to none. The *delay* before calling showFlex(false)
                    // will be handled by the caller (showMessage/showSubmissionDialog).
                    el.style.display = 'none';
                }
            }
        }

        function disable(el, state) {
            if (el) el.disabled = state;
        }

        // Modified fillSelect to always include a disabled selected option
        function fillSelect(selectElement, options, defaultText) {
            selectElement.innerHTML = `<option value="" disabled selected>\${defaultText}</option>`;
            options.forEach(option => {
                const opt = document.createElement('option');
                opt.value = option.value;
                opt.textContent = option.text;
                selectElement.appendChild(opt);
            });
            // Reset to default selected option after filling
            selectElement.value = "";
        }

        function showMessage(element, message, isSuccess) {
            if (element) {
                const iconClass = isSuccess ? 'bx bx-check-circle' : 'bx bx-x-circle';
                // Only add icon if message exists. Message can be HTML for spinner.
                const messageHtml = message.trim() ? `<i class='${iconClass}'></i> \${message}` : '';
                element.innerHTML = messageHtml; // Set content first

                element.className = 'message-box ' + (isSuccess ? 'success' : 'error');

                // Explicitly manage display property based on message content
                if (message.trim() !== '' || message.includes('spinner')) {
                    showFlex(element, true); // Make visible

                    if (message.includes('spinner')) {
                        // Hide spinner message after 3 seconds
                        setTimeout(() => {
                            showFlex(element, false); // Hide after 3 seconds
                            element.innerHTML = ''; // Clear content when hiding spinner
                        }, 3000); // 3 seconds for spinner
                    } else {
                        // Hide non-loading (success/error) message after 5 seconds
                        setTimeout(() => {
                            showFlex(element, false); // Hide after 5 seconds
                            element.innerHTML = ''; // Clear content when hiding non-loading message
                        }, 5000); // 5 seconds for success/error messages
                    }
                } else {
                    // If message is empty, hide immediately
                    showFlex(element, false);
                    element.innerHTML = ''; // Clear content immediately
                }
            }
        }

        function showSubmissionDialog(isSuccess, message) {
            const overlay = qs('#submissionDialogOverlay');
            const dialogContent = qs('.submission-dialog-content');
            const dialogIcon = qs('#dialogIcon');
            const dialogTitle = qs('#dialogTitle');
            const dialogMessage = qs('#dialogMessage');
            const dialogCloseBtn = qs('#dialogCloseBtn');

            dialogIcon.innerHTML = isSuccess ? '<i class="bx bx-check-circle success-icon"></i>' : '<i class="bx bx-x-circle error-icon"></i>';
            dialogTitle.textContent = isSuccess ? 'Success!' : 'Error!';
            dialogMessage.textContent = message;

            overlay.classList.add('show');
            dialogContent.classList.add('show');

            dialogCloseBtn.onclick = () => {
                overlay.classList.remove('show');
                dialogContent.classList.remove('show');
                // The dialogCloseBtn click also clears the main message box
                // This call effectively hides the message box immediately when dialog is closed.
                showMessage(marksEntryMessage, "", true); // Clear and hide the main message box
            };
        }

        // Global references for Marks Entry on this page
        const marksProgramSelect = qs('#marksProgramFilterDropdown');
        const marksSemesterSelect = qs('#marksSemesterFilterDropdown');
        const marksSubjectSelect = qs('#marksSubjectFilterDropdown');
        const marksExamTypeSelect = qs('#marksExamTypeDropdown');
        const loadStudentsForMarksBtn = qs('#loadStudentsForMarksBtn');
        const resetMarksFiltersBtn = qs('#resetMarksFiltersBtn'); // New button reference

        const marksEntryTableContainer = qs('#marksEntryTableContainer');
        const marksStudentsTableBody = qs('#marksStudentsTableBody');
        const submitMarksBtn = qs('#submitMarksBtn');
        const marksEntryMessage = qs('#marksEntryMessage');

        document.addEventListener('DOMContentLoaded', function() {
            // Ensure message box is hidden on initial load
            showFlex(marksEntryMessage, false);

            // Initial load of programs for the dropdown
            loadProgramsForMarksEntry();

            // Event listeners for dropdowns
            if (marksProgramSelect) {
                marksProgramSelect.addEventListener('change', function() {
                    const programId = this.value;
                    // Clear and disable dependent dropdowns
                    fillSelect(marksSemesterSelect, [], 'Select Semester');
                    disable(marksSemesterSelect, true);
                    fillSelect(marksSubjectSelect, [], 'Select Subject');
                    disable(marksSubjectSelect, true);
                    fillSelect(marksExamTypeSelect, [], 'Select Exam Type');
                    disable(marksExamTypeSelect, true);
                    disable(loadStudentsForMarksBtn, true);
                    show(marksEntryTableContainer, false);
                    showMessage(marksEntryMessage, "", true); // Clear message box content and hide it

                    if (programId) {
                        disable(marksSemesterSelect, false);
                        fillSelect(marksSemesterSelect, marksSemesters, 'Select Semester');
                    }
                });
            }

            if (marksSemesterSelect) {
                marksSemesterSelect.addEventListener('change', function() {
                    const programId = marksProgramSelect.value;
                    const semester = this.value;

                    // Clear and disable dependent dropdowns
                    fillSelect(marksSubjectSelect, [], 'Select Subject');
                    disable(marksSubjectSelect, true);
                    fillSelect(marksExamTypeSelect, [], 'Select Exam Type');
                    disable(marksExamTypeSelect, true);
                    disable(loadStudentsForMarksBtn, true);
                    show(marksEntryTableContainer, false);
                    showMessage(marksEntryMessage, "", true);

                    if (programId && semester) {
                        fetchSubjectsForMarksEntry(programId, semester);
                    }
                });
            }

            if (marksSubjectSelect) {
                marksSubjectSelect.addEventListener('change', function() {
                    const programId = marksProgramSelect.value;
                    const semester = marksSemesterSelect.value;
                    const subjectId = this.value;

                    // Clear and disable dependent dropdowns
                    fillSelect(marksExamTypeSelect, [], 'Select Exam Type');
                    disable(marksExamTypeSelect, true);
                    disable(loadStudentsForMarksBtn, true);
                    show(marksEntryTableContainer, false);
                    showMessage(marksEntryMessage, "", true);

                    if (programId && semester && subjectId) {
                        disable(marksExamTypeSelect, false);
                        fillSelect(marksExamTypeSelect, examTypes, 'Select Exam Type');
                    }
                });
            }

            if (marksExamTypeSelect) {
                marksExamTypeSelect.addEventListener('change', function() {
                    const programId = marksProgramSelect.value;
                    const semester = marksSemesterSelect.value;
                    const subjectId = marksSubjectSelect.value;
                    const examType = this.value;

                    // Enable Load Students button only when all four are selected
                    if (programId && semester && subjectId && examType) {
                        disable(loadStudentsForMarksBtn, false);
                    } else {
                        disable(loadStudentsForMarksBtn, true);
                    }
                    show(marksEntryTableContainer, false);
                    showMessage(marksEntryMessage, "", true);
                });
            }

            if (loadStudentsForMarksBtn) {
                loadStudentsForMarksBtn.addEventListener('click', function() {
                    const programId = marksProgramSelect.value;
                    const semester = marksSemesterSelect.value;
                    const courseId = marksSubjectSelect.value;
                    const examType = marksExamTypeSelect.value;

                    if (!programId || !semester || !courseId || !examType) {
                        showMessage(marksEntryMessage, `<i class="bx bx-info-circle"></i> Please select Program, Semester, Subject, and Exam Type.`, false);
                        return;
                    }
                    loadStudentsAndMarks(programId, semester, courseId, examType);
                });
            }

            // --- New: Event listener for Reset button ---
            if (resetMarksFiltersBtn) {
                resetMarksFiltersBtn.addEventListener('click', resetMarksFilters);
            }

            // Event listener for form submission
            if (qs('#marksEntryForm')) {
                qs('#marksEntryForm').addEventListener('submit', function(e) {
                    e.preventDefault();
                    const programId = marksProgramSelect.value;
                    const semester = marksSemesterSelect.value;
                    const courseId = marksSubjectSelect.value;
                    const examType = marksExamTypeSelect.value;
                    submitMarks(programId, semester, courseId, examType);
                });
            }
        });

        // --- New: Reset Filters function ---
        function resetMarksFilters() {
            fillSelect(marksProgramSelect, [], 'Select Program'); // Will re-add the default option
            loadProgramsForMarksEntry(); // Reload programs to ensure it's populated for next selection

            fillSelect(marksSemesterSelect, [], 'Select Semester');
            disable(marksSemesterSelect, true);

            fillSelect(marksSubjectSelect, [], 'Select Subject');
            disable(marksSubjectSelect, true);

            fillSelect(marksExamTypeSelect, [], 'Select Exam Type');
            disable(marksExamTypeSelect, true);

            disable(loadStudentsForMarksBtn, true);
            show(marksEntryTableContainer, false); // Hide the table
            marksStudentsTableBody.innerHTML = ''; // Clear table body
            showMessage(marksEntryMessage, "", true); // Clear and hide any messages
        }


        // Data for dropdowns (could be fetched from backend too)
        const marksSemesters = ['1', '2', '3', '4', '5', '6'].map(s => ({ value: s, text: 'Semester ' + s }));
        const examTypes = [
            { value: 'Internal Assessment 1', text: 'Internal Assessment 1' },
            { value: 'Internal Assessment 2', text: 'Internal Assessment 2' },
            { value: 'SEE (Semester End Examination)', text: 'SEE (Semester End Examination)' }
        ];

        function loadProgramsForMarksEntry() {
            fetch('<%= request.getContextPath() %>/GetProgramsServlet')
                .then(r => {
                    if (!r.ok) throw new Error('HTTP ' + r.status);
                    return r.json();
                })
                .then(data => {
                    const formattedPrograms = data.map(p => ({ value: p[0], text: p[1] }));
                    fillSelect(marksProgramSelect, formattedPrograms, 'Select Program');
                })
                .catch(error => {
                    console.error("Error loading programs for Marks Entry:", error);
                    showMessage(marksEntryMessage, `<i class="bx bx-error-circle"></i> Failed to load programs for marks entry.`, false);
                });
        }

        function fetchSubjectsForMarksEntry(programId, semester) {
            fetch('<%= request.getContextPath() %>/GetCoursesByProgramAndSemesterServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'programId=' + encodeURIComponent(programId) + '&semester=' + encodeURIComponent(semester)
            })
            .then(r => {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(data => {
                const formattedSubjects = data.map(s => ({ value: s.courseId, text: s.courseName }));
                fillSelect(marksSubjectSelect, formattedSubjects, 'Select Subject');
                disable(marksSubjectSelect, false);
            })
            .catch(error => {
                console.error("Error fetching subjects for Marks Entry:", error);
                showMessage(marksEntryMessage, `<i class="bx bx-error-circle"></i> Failed to load subjects for marks entry. Please try again.`, false);
                disable(marksSubjectSelect, true);
                fillSelect(marksSubjectSelect, [], 'Select Subject');
            });
        }

        async function loadStudentsAndMarks(programId, semester, courseId, examType) {
            showMessage(marksEntryMessage, "", true); // Clear message

            marksStudentsTableBody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:20px;"><div class="loading-message"><div class="spinner"></div> Loading students...</div></td></tr>'; // Increased colspan
            disable(submitMarksBtn, true);
            show(marksEntryTableContainer, true);
            showMessage(marksEntryMessage, `<div class="loading-message"><div class="spinner"></div> Fetching student data...</div>`, true);

            try {
                const finalServletUrl = '<%= request.getContextPath() %>/GetStudForMarks';
                console.log("Fetching students from:", finalServletUrl);

                const response = await fetch(finalServletUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        programId: parseInt(programId),
                        semester: parseInt(semester),
                        courseId: courseId,
                        examType: examType
                    })
                });

                if (!response.ok) {
                    const errorBody = await response.text();
                    throw new Error(`HTTP status ${response.status}: ${response.statusText}. Response: ${errorBody}`);
                }
                const students = await response.json();

                marksStudentsTableBody.innerHTML = ''; // Clear loading message

                if (students.length === 0) {
                    marksStudentsTableBody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:20px;">No students found for these criteria or no existing marks.</td></tr>'; // Increased colspan
                    showMessage(marksEntryMessage, `<i class="bx bx-info-circle"></i> No students found or no marks to display. If students should be here, ensure they are enrolled in this course and semester.`, false);
                    disable(submitMarksBtn, true);
                    return;
                }
                
                // Update table header for the new "Actions" column if not already present
                const tableHeaderRow = qs('#marksEntryTableContainer thead tr');
                if (tableHeaderRow.children.length === 3) { 
                    const thActions = document.createElement('th');
                    thActions.textContent = 'Actions';
                    tableHeaderRow.appendChild(thActions);
                }


                students.forEach(student => {
                    const tr = document.createElement('tr');
                    tr.setAttribute('data-student-id', student.studentId);
                    tr.setAttribute('data-enrollment-id', student.enrollmentId);

                    const tdId = document.createElement('td');
                    tdId.textContent = student.studentId;
                    tr.appendChild(tdId);

                    const tdName = document.createElement('td');
                    tdName.textContent = student.fullName;
                    tr.appendChild(tdName);

                    const tdMarks = document.createElement('td');
                    const marksInput = document.createElement('input');
                    marksInput.type = 'number';
                    marksInput.className = 'form-control marks-input';
                    marksInput.min = '0';
                    marksInput.max = '100';
                    marksInput.placeholder = 'Enter marks';
                    // Use marksObtained from DTO, set empty string for null
                    marksInput.value = student.marksObtained !== null ? student.marksObtained : ''; 

                    // Determine initial readOnly state
                    const isExistingMark = student.marksObtained !== null; // True if mark exists in DB
                    marksInput.readOnly = isExistingMark; // If existing, make read-only
                    marksInput.setAttribute('data-initial-marks', marksInput.value); // Store initial value

                    tdMarks.appendChild(marksInput);
                    tr.appendChild(tdMarks);

                    // --- Conditional Actions column and Buttons ---
                    const tdActions = document.createElement('td');
                    
                    if (isExistingMark) {
                        // Mark exists: show Edit button, hide Save/Cancel
                        const editButton = document.createElement('button');
                        editButton.className = 'btn btn-secondary btn-sm edit-mark-btn';
                        editButton.innerHTML = '<i class="bx bx-edit"></i> Edit';
                        editButton.type = 'button'; // Prevent form submission
                        editButton.style.marginRight = '5px'; // Small space between buttons

                        const saveButton = document.createElement('button');
                        saveButton.className = 'btn btn-success btn-sm save-mark-btn';
                        saveButton.innerHTML = '<i class="bx bx-check"></i> Save';
                        saveButton.type = 'button';
                        saveButton.style.display = 'none'; // Initially hidden
                        saveButton.style.marginRight = '5px';

                        const cancelButton = document.createElement('button');
                        cancelButton.className = 'btn btn-secondary btn-sm cancel-mark-btn';
                        cancelButton.innerHTML = '<i class="bx bx-x"></i> Cancel';
                        cancelButton.type = 'button';
                        cancelButton.style.display = 'none'; // Initially hidden

                        tdActions.appendChild(editButton);
                        tdActions.appendChild(saveButton);
                        tdActions.appendChild(cancelButton);

                        // Attach event listeners for each row's buttons
                        editButton.addEventListener('click', () => {
                            marksInput.readOnly = false;
                            editButton.style.display = 'none';
                            saveButton.style.display = 'inline-flex'; // Use flex to match btn styles
                            cancelButton.style.display = 'inline-flex';
                            marksInput.focus();
                            marksInput.select(); // Select current text for easy editing
                            disable(submitMarksBtn, true); // Disable global submit during inline edit
                        });

                        cancelButton.addEventListener('click', () => {
                            marksInput.readOnly = true;
                            marksInput.value = marksInput.getAttribute('data-initial-marks'); // Revert to initial value
                            marksInput.style.borderColor = ''; // Clear any validation borders
                            editButton.style.display = 'inline-flex';
                            saveButton.style.display = 'none';
                            cancelButton.style.display = 'none';
                            disable(submitMarksBtn, false); // Re-enable global submit
                        });

                        saveButton.addEventListener('click', async () => {
                            const value = marksInput.value.trim();
                            let marksToSave = null;

                            if (value !== '') {
                                const parsedMarks = parseFloat(value);
                                if (isNaN(parsedMarks) || parsedMarks < 0 || parsedMarks > 100) {
                                    marksInput.style.borderColor = 'var(--danger-color)';
                                    showMessage(marksEntryMessage, `<i class="bx bx-error-circle"></i> Invalid marks for student ID ${student.studentId}. Marks must be between 0 and 100.`, false);
                                    return;
                                }
                                marksToSave = parsedMarks;
                            }

                            // Prepare single mark entry for submission
                            const singleMarksEntry = [{
                                studentId: student.studentId,
                                enrollmentId: student.enrollmentId,
                                courseId: courseId, // From filter
                                examType: examType, // From filter
                                marksObtained: marksToSave,
                                facultyId: parseInt(window.currentFacultyId),
                                programId: parseInt(programId), // From filter
                                semester: parseInt(semester) // From filter
                            }];

                            showMessage(marksEntryMessage, '<div class="loading-message"><div class="spinner"></div> Saving mark...</div>', true);
                            disable(saveButton, true);
                            disable(cancelButton, true);
                            
                            try {
                                const response = await fetch('<%= request.getContextPath() %>/SaveMarksServlet', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify(singleMarksEntry)
                                });

                                const result = await response.json();

                                if (response.ok && result.status === "success") {
                                    showMessage(marksEntryMessage, result.message || 'Mark saved successfully!', true);
                                    marksInput.readOnly = true;
                                    marksInput.style.borderColor = ''; // Clear border
                                    marksInput.setAttribute('data-initial-marks', marksInput.value); // Update initial value
                                    editButton.style.display = 'inline-flex';
                                    saveButton.style.display = 'none';
                                    cancelButton.style.display = 'none';
                                } else {
                                    marksInput.style.borderColor = 'var(--danger-color)';
                                    showMessage(marksEntryMessage, result.message || 'Failed to save mark. Please try again.', false);
                                }
                            } catch (error) {
                                console.error('Error saving mark:', error);
                                marksInput.style.borderColor = 'var(--danger-color)';
                                showMessage(marksEntryMessage, 'An error occurred during mark saving: ' + error.message, false);
                            } finally {
                                disable(saveButton, false);
                                disable(cancelButton, false);
                                disable(submitMarksBtn, false); // Re-enable global submit
                            }
                        });
                    } else {
                        // Mark does NOT exist: input is editable, no edit/save/cancel buttons initially
                        // The global submit button will be used for initial entry.
                        // You could put a placeholder like:
                        // tdActions.innerHTML = '<span>New Entry</span>';
                        // Or just leave it empty.
                    }
                    tr.appendChild(tdActions); // Always append tdActions whether it has buttons or not

                    marksStudentsTableBody.appendChild(tr);

                    // Add event listeners for input validation after populating
                    marksInput.addEventListener('input', function() {
                        const value = this.value.trim();
                        // Only apply validation styling if not empty
                        if (value !== '') {
                            const marks = parseFloat(value);
                            if (isNaN(marks) || marks < 0 || marks > 100) {
                                this.style.borderColor = 'var(--danger-color)';
                            } else {
                                this.style.borderColor = '';
                            }
                        } else {
                            this.style.borderColor = ''; // Clear border if field is empty
                        }
                    });
                });
                disable(submitMarksBtn, false);
                showMessage(marksEntryMessage, `<i class="bx bx-check-circle"></i> Students loaded successfully. Enter/Edit marks and submit.`, true);

            } catch (error) {
                console.error("Error loading students for marks entry:", error);
                marksStudentsTableBody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:20px;color:var(--danger-color);">Failed to load students. ' + error.message + '</td></tr>'; // Increased colspan
                showMessage(marksEntryMessage, `<i class="bx bx-error-circle"></i> An error occurred: ${error.message}`, false);
                disable(submitMarksBtn, true);
            }
        }

        async function submitMarks(programId, semester, courseId, examType) {
            const facultyId = parseInt(window.currentFacultyId);

            if (isNaN(facultyId) || facultyId <= 0) {
                showSubmissionDialog(false, 'Error: Faculty ID is missing or invalid. Cannot submit marks.');
                return;
            }

            const marksEntries = [];
            let isValid = true;
            marksStudentsTableBody.querySelectorAll('tr[data-student-id]').forEach(row => {
                const studentId = row.getAttribute('data-student-id');
                const enrollmentId = row.getAttribute('data-enrollment-id');
                const marksInput = row.querySelector('.marks-input');
                let marks = marksInput.value.trim();

                // If an inline edit is active for any row (meaning the global submit is disabled),
                // we should prevent this global submission or ensure the inline save button is used.
                // However, the current logic disables the global submit button.
                // This 'if (!marksInput.readOnly || marks !== '')' might be overly complex.
                // A simpler approach: if the field is not read-only (meaning it's editable,
                // either because it's a new entry or an existing one being edited inline),
                // or if it HAS a value (even if read-only, indicating an existing mark), process it.
                // The backend UPSERT handles nulls for deletion.
                
                // For global submit, we gather all marks.
                // If marksInput.readOnly is true, it means it's an existing mark.
                // If it's not readOnly, it means it's either a new mark or one being edited inline.
                // The crucial part is to correctly interpret empty string.
                if (marks === '') {
                    marks = null; // Send null if the field is empty (user cleared it or never entered)
                } else {
                    marks = parseFloat(marks);
                    if (isNaN(marks) || marks < 0 || marks > 100) {
                        isValid = false;
                        marksInput.style.borderColor = 'var(--danger-color)'; // Highlight invalid input
                        showMessage(marksEntryMessage, `<i class="bx bx-error-circle"></i> Invalid marks for student ID ${studentId}. Marks must be between 0 and 100.`, false);
                        // Do not return here. Set isValid to false and let the loop continue
                        // so all invalid inputs can be highlighted. The check below will prevent submission.
                    } else {
                        marksInput.style.borderColor = ''; // Reset border
                    }
                }

                // Push all entries for batch UPSERT/DELETE
                marksEntries.push({
                    studentId: parseInt(studentId),
                    enrollmentId: parseInt(enrollmentId),
                    courseId: courseId, // From filter
                    examType: examType, // From filter
                    marksObtained: marks,
                    facultyId: facultyId,
                    programId: parseInt(programId),
                    semester: parseInt(semester)
                });
            });

            if (!isValid) {
                // If validation failed for any input, stop submission
                return; 
            }

            if (marksEntries.length === 0) {
                showSubmissionDialog(false, 'No students data found to submit marks for.');
                return;
            }

            disable(submitMarksBtn, true);
            showMessage(marksEntryMessage, '<div class="loading-message"><div class="spinner"></div> Submitting marks...</div>', true);

            try {
                const finalSaveServletUrl = '<%= request.getContextPath() %>/SaveMarksServlet';
                console.log("Submitting marks to:", finalSaveServletUrl);

                const response = await fetch(finalSaveServletUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(marksEntries)
                });

                const result = await response.json();

                if (response.ok && result.status === "success") {
                    showSubmissionDialog(true, result.message || 'Marks submitted successfully!');
                    // Reload students and marks to reflect updated state (read-only/initial-marks)
                    // This is crucial to make fields read-only after global submit
                    loadStudentsAndMarks(programId, semester, courseId, examType);
                } else {
                    showSubmissionDialog(false, result.message || 'Failed to submit marks. Please check input and try again.');
                }
            } catch (error) {
                console.error('Error submitting marks:', error);
                showSubmissionDialog(false, 'An error occurred during submission: ' + error.message);
            } finally {
                // The submit button will be re-enabled by loadStudentsAndMarks if successful,
                // or if an error occurs, we re-enable it here.
                disable(submitMarksBtn, false); 
            }
        }
    </script>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>Faculty Dashboard</title>

    <!-- Boxicons for icons -->
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" integrity="sha512-SnH5WK+bZxgPHs44uWIX+LLJAJ9/2PkPKZ5QiAj6Ta86w+fsb2TkcmfRyVX3pBnMFcV7oQPJkl9QevSCWr3W6A==" crossorigin="anonymous" referrerpolicy="no-referrer" />

    <!-- Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/css/sidebar.css" />

</head>

<body>
<!-- ===== Sidebar ===== -->
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
                    <a href="<c:url value='/attendance.jsp' />">
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
    
    <div style="overflow-x: auto; border-radius: 20px; max-width: 100%;"> 
   <table id="studentsTable"  style="width: 100%; border-collapse: collapse; border-radius: 20px; background: white; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
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

</div>
<script>
    // GLOBAL FUNCTION FOR FEATURE NAVIGATION
    document.addEventListener('DOMContentLoaded', () => {
        const sidebar = document.querySelector('nav.sidebar');
        const toggleBtn = document.querySelector('.toggle');
        toggleBtn?.addEventListener('click', () => sidebar.classList.toggle('close'));

        // Dropdown toggle
        document.querySelectorAll('.has-dropdown > a').forEach(dropdownToggle => {
            dropdownToggle.addEventListener('click', function(e) {
                e.preventDefault();
                const parentLi = this.closest('.has-dropdown');
                document.querySelectorAll('.has-dropdown').forEach(item => {
                    if (item !== parentLi) item.classList.remove('active');
                });
                parentLi.classList.toggle('active');
            });
        });

        const dashboardLink = document.querySelector('a[href="facultyDashboard.jsp"]');
        const dashSection = document.getElementById('dashboardSection');
        const featuresWrap = document.getElementById('studentFeatures');
        const manageLink = document.getElementById('manageStudentsLink');

        dashboardLink?.addEventListener('click', e => {
            e.preventDefault();
            featuresWrap.style.display = 'none';
            dashSection.style.display = 'block';
        });

        manageLink?.addEventListener('click', e => {
            e.preventDefault();
            dashSection.style.display = 'none';
            featuresWrap.style.display = 'block';
            showStudentFeature('');
            const parentDropdown = manageLink.closest('.has-dropdown');
            parentDropdown?.classList.remove('active');
            if (!sidebar.classList.contains('close')) sidebar.classList.add('close');
        });

        // --- GLOBAL REFERENCES FOR FILTERING/SORTING/DELETION ---
        // Moved these declarations to the top so they are accessible by loadStudentData
        const studentSearchInput = document.getElementById('studentSearchInput');
        const courseFilterDropdown = document.getElementById('courseFilterDropdown');
        const resetFiltersBtn = document.getElementById('resetFiltersBtn');
        const studentsTableBody = document.getElementById('studentsTableBody'); // Declared once here
        const sortableHeaders = document.querySelectorAll('#studentsTable th[data-sort-col]');


        // --- showStudentFeature function (Corrected Logic) ---
        window.showStudentFeature = function(which) {
            const featureCards = document.querySelector('.feature-card-wrap');
            const addFormCard = document.getElementById('addStudentCard');
            const viewSection = document.getElementById('viewStudentSection');
            const todo = document.getElementById('todoPlaceholder');

            // Hide all feature sections first
            featureCards.style.display = 'none';
            addFormCard.style.display = 'none';
            viewSection.style.display = 'none';
            todo.style.display = 'none';

            switch (which) {
                case 'add':
                    addFormCard.style.display = 'block';
                    break; // Break here!
                case 'view':
                    viewSection.style.display = 'block';
                    loadStudentData(); // Load data specifically for view
                    break; // Break here!
                case 'edit':
                case 'delete':
                case 'search':
                    viewSection.style.display = 'block'; // If you want edit/delete/search to show the table
                    loadStudentData(); // And load the table data
                    break; // Break here!
                default:
                    featureCards.style.display = 'flex'; // Show the main feature cards
            }
        };

        const pickerAdd = document.getElementById('coursePickerAdd');
        const fieldsBlock = document.getElementById('studentFields');
        pickerAdd?.addEventListener('change', () => {
            fieldsBlock.style.display = pickerAdd.value ? 'flex' : 'none';
        });

        const cancelBtn = document.getElementById('cancelAddBtn');
        const addStudentForm = document.getElementById('addStudentForm');
        const messageContainer = document.getElementById('addStudentMessage');
        cancelBtn?.addEventListener('click', () => {
            addStudentForm.reset();
            pickerAdd.selectedIndex = 0;
            fieldsBlock.style.display = 'none';
            messageContainer.style.display = 'none';
            showStudentFeature('');
        });

        const backToFeaturesBtn = document.getElementById('backToFeaturesBtn');
        backToFeaturesBtn?.addEventListener('click', () => showStudentFeature(''));

        function validateStudentForm() {
            const studentId = document.getElementById('studentId').value.trim();
            const semester = document.getElementById('semester').value.trim();

            if (!/^\d+$/.test(studentId)) {
                alert('Student ID must be numeric');
                return false;
            }

            // Corrected semester validation to match HTML max="6"
            if (!/^\d+$/.test(semester) || semester < 1 || semester > 6) {
                alert('Semester must be a number between 1 and 6');
                return false;
            }

            return true;
        }

        // ✅ Corrected submit handler for Add Student
        addStudentForm.addEventListener('submit', async e => {
            e.preventDefault();
            if (!validateStudentForm()) return;

            messageContainer.innerHTML =
                `<div class="loading-message"><div class="spinner"></div> Saving student…</div>`;
            messageContainer.style.display = 'block';
            const t0 = Date.now();

            let text;
            try {
                const resp = await fetch('AddStudServlet', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams(new FormData(addStudentForm))
                });
                text = (await resp.text()).trim();
            } catch {
                text = 'Network / server error. Try again.';
            }

            const wait = Math.max(0, 1500 - (Date.now() - t0));

            setTimeout(() => {
                const ok = text.toLowerCase().includes('success');

                messageContainer.innerHTML = ''; // Clear previous content

                const messageDiv = document.createElement('div');
                const iconSpan = document.createElement('span');
                const textNode = document.createTextNode(text);

                const isError = !ok;

                messageDiv.style.display = 'flex';
                messageDiv.style.alignItems = 'center';
                messageDiv.style.gap = '12px';
                messageDiv.style.fontWeight = 'bold';
                messageDiv.style.padding = '10px';
                messageDiv.style.borderRadius = '6px';
                messageDiv.style.color = isError ? '#d8000c' : '#008000';
                messageDiv.style.border = `1px solid ${isError ? '#d8000c' : '#008000'}`;
                messageDiv.style.background = isError ? '#ffe6e6' : '#e7f9ed';

                iconSpan.style.fontSize = '24px';
                iconSpan.textContent = isError ? '❌' : '✅';

                messageDiv.appendChild(iconSpan);
                messageDiv.appendChild(textNode);
                messageContainer.appendChild(messageDiv);
                messageContainer.style.display = 'block';

                if (ok) {
                    addStudentForm.reset();
                    pickerAdd.selectedIndex = 0;
                    fieldsBlock.style.display = 'none';
                    setTimeout(() => messageContainer.style.display = 'none', 5000);
                }
            }, wait);
        });

        // --- loadStudentData function (MODIFIED for filter/sort integration) ---
        async function loadStudentData() {
            console.log("loadStudentData called.");

            // Clear filter inputs when loading new data
            if (studentSearchInput) studentSearchInput.value = '';
            if (courseFilterDropdown) courseFilterDropdown.value = '';

            studentsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px;">Loading...</td></tr>`;

            try {
                console.log("Fetching data with POST to GetStudentsServlet...");
                const response = await fetch('GetStudentsServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({})
                });

                console.log("Response status:", response.status);
                if (!response.ok) {
                    const errorText = await response.text();
                    console.error("Server error response text:", errorText);
                    throw new Error(`Server error: ${response.status} - ${response.statusText}`);
                }
                const students = await response.json();
                console.log("Students data received:", students);

                // Clear the loading message
                studentsTableBody.innerHTML = '';

                if (students.length === 0) {
                    studentsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px;">No students found.</td></tr>`;
                    return;
                }

                students.forEach(student => {
                    console.log("Adding student to table programmatically:", student);

                    const tr = document.createElement('tr');

                    let tdId = document.createElement('td');
                    tdId.textContent = student.studentId;
                    tr.appendChild(tdId);

                    let tdName = document.createElement('td');
                    tdName.textContent = student.fullName;
                    tr.appendChild(tdName);

                    let tdCourse = document.createElement('td');
                    tdCourse.textContent = student.course;
                    tr.appendChild(tdCourse);

                    let tdSemester = document.createElement('td');
                    tdSemester.textContent = student.semester;
                    tr.appendChild(tdSemester);

                    let tdEmail = document.createElement('td');
                    tdEmail.textContent = student.email;
                    tr.appendChild(tdEmail);

                    let tdPhone = document.createElement('td');
                    tdPhone.textContent = student.phone;
                    tr.appendChild(tdPhone);

                    let tdActions = document.createElement('td'); // Ensure this is declared here or above
                    const editButton = document.createElement('button');
                    editButton.className = 'btn-action-edit';
                    editButton.dataset.id = student.studentId;
                    editButton.textContent = 'Edit';
                    editButton.style.marginRight = '8px';
                    tdActions.appendChild(editButton);

                    const deleteButton = document.createElement('button');
                    deleteButton.className = 'btn-action-delete';
                    deleteButton.dataset.id = student.studentId; // Corrected: Directly use 'student' from the outer loop
                  
                    deleteButton.textContent = 'Delete';
                    tdActions.appendChild(deleteButton);

                    // The event listener is attached to THIS SPECIFIC deleteButton
                    deleteButton.addEventListener('click', async () => {
                        const studentIdToDelete = deleteButton.dataset.id; // This will now get the correct ID
                        const rowToDelete = deleteButton.closest('tr');
                        
                        if (confirm(`Are you sure you want to delete student with ID: \${studentIdToDelete}?`)) {
                            try {
                                const response = await fetch('DeleteStudentServlet', {
                                    method: 'POST',
                                    headers: {
                                        'Content-Type': 'application/x-www-form-urlencoded'
                                    },
                                    body: `studentId=\${encodeURIComponent(studentIdToDelete)}`
                                });

                                const result = await response.json();

                                if (response.ok && result.status === 'success') {
                                    alert(result.message);
                                    if (rowToDelete) {
                                        rowToDelete.remove();
                                    }
                                    applyTableFilters(); // Re-apply filters to update counts and messages
                                } else {
                                    alert(`Error deleting student: ${result.message || 'Unknown error'}`);
                                    console.error('Delete error:', result.message);
                                }
                            } catch (error) {
                                alert('Network error or server unreachable. Could not delete student.');
                                console.error('Fetch error during delete:', error);
                            }
                        }
                    });

                    tr.appendChild(tdActions); // This should be outside the deleted loop
                    studentsTableBody.appendChild(tr); // This should be outside the deleted loop
                }); // This is the closing brace for the ORIGINAL, OUTER students.forEach loop

                // AFTER the table is populated, apply filters and then sort
                applyTableFilters();
                sortTable(0, 'asc');

                // Set the initial sort arrow for Student ID column visually
                const initialSortHeader = document.querySelector('#studentsTable th[data-sort-col="0"]');
                if (initialSortHeader) {
                    // Reset all header icons to neutral and remove sort classes
                    sortableHeaders.forEach(h => {
                        h.classList.remove('sort-asc', 'sort-desc'); // Remove TH classes
                        const iconSpan = h.querySelector('.sort-icon'); // Get icon span
                        if (iconSpan) {
                            iconSpan.classList.remove('fa-sort-up', 'fa-sort-down'); // Clear specific icons
                            iconSpan.classList.add('fa-sort'); // Set neutral icon
                        }
                    });

                    // Set initial sort class and icon for the first column (Student ID)
                    initialSortHeader.classList.add('sort-asc');
                    const initialIconSpan = initialSortHeader.querySelector('.sort-icon');
                    if (initialIconSpan) {
                        initialIconSpan.classList.remove('fa-sort'); // Remove neutral
                        initialIconSpan.classList.add('fa-sort-up'); // Set initial up arrow
                    }
                }
            } catch (error) {
                console.error("Failed to load student data:", error);
                studentsTableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px; color:red;">Failed to load data. Please check the console.</td></tr>`;
            }
        }

        // --- Filtering Logic ---
        function applyTableFilters() {
            const searchText = studentSearchInput.value.toLowerCase().trim();
            const selectedCourse = courseFilterDropdown.value.toLowerCase();

            const rows = studentsTableBody.querySelectorAll('tr');
            let visibleRowCount = 0;

            rows.forEach(row => {
                const isDataRow = row.querySelector('.btn-action-edit') || row.querySelectorAll('td').length === 7;

                if (!isDataRow) {
                    row.style.display = 'none';
                    return;
                }

                const cells = row.querySelectorAll('td');
                const studentId = cells[0].textContent.toLowerCase();
                const fullName = cells[1].textContent.toLowerCase();
                const course = cells[2].textContent.toLowerCase();
                const email = cells[4].textContent.toLowerCase();
                const phone = cells[5].textContent.toLowerCase();

                let matchesSearch = true;
                if (searchText) {
                    if (!(studentId.includes(searchText) ||
                          fullName.includes(searchText) ||
                          email.includes(searchText) ||
                          phone.includes(searchText))) {
                        matchesSearch = false;
                    }
                }

                let matchesCourse = true;
                if (selectedCourse && selectedCourse !== "all courses") {
                    if (course !== selectedCourse) {
                        matchesCourse = false;
                    }
                }

                if (matchesSearch && matchesCourse) {
                    row.style.display = 'table-row';
                    visibleRowCount++;
                } else {
                    row.style.display = 'none';
                }
            });

            const noStudentsMessageId = 'noStudentsFilteredMessage';
            let noStudentsRow = document.getElementById(noStudentsMessageId);

            if (visibleRowCount === 0) {
                if (!noStudentsRow) {
                    noStudentsRow = document.createElement('tr');
                    noStudentsRow.id = noStudentsMessageId;
                    noStudentsRow.innerHTML = `<td colspan="7" style="text-align:center; padding:20px; color: grey;">No matching students found.</td>`;
                    studentsTableBody.appendChild(noStudentsRow);
                } else {
                    noStudentsRow.style.display = 'table-row';
                }
            } else {
                if (noStudentsRow) {
                    noStudentsRow.style.display = 'none';
                }
            }
        }

        // Attach event listeners for filters
        studentSearchInput.addEventListener('input', applyTableFilters);
        courseFilterDropdown.addEventListener('change', applyTableFilters);
        resetFiltersBtn.addEventListener('click', () => {
            studentSearchInput.value = '';
            courseFilterDropdown.value = '';
            applyTableFilters();
        });

        // --- Table Sorting Logic ---
    // --- Table Sorting Logic ---
sortableHeaders.forEach(header => {
    header.addEventListener('click', () => {
        const column = parseInt(header.dataset.sortCol);
        let direction = header.dataset.sortDir;

        direction = (direction === 'asc') ? 'desc' : 'asc';
        header.dataset.sortDir = direction;

        // --- START REPLACEMENT HERE ---
        // Reset classes and Font Awesome icons for ALL headers (including the one just clicked)
        sortableHeaders.forEach(h => {
            h.classList.remove('sort-asc', 'sort-desc'); // Remove TH sorting classes
            const iconSpan = h.querySelector('.sort-icon'); // Get the Font Awesome icon span
            if (iconSpan) {
                iconSpan.classList.remove('fa-sort-up', 'fa-sort-down'); // Remove specific direction icons
                iconSpan.classList.add('fa-sort'); // Set to neutral sort icon
            }
        });
        // --- END REPLACEMENT HERE ---

        // Set the active sorting class and Font Awesome icon for the clicked header
        header.classList.add(`sort-${direction}`); // Re-add the class for the active header
        const clickedIconSpan = header.querySelector('.sort-icon');
        if (clickedIconSpan) {
            clickedIconSpan.classList.remove('fa-sort'); // Remove neutral icon
            if (direction === 'asc') {
                clickedIconSpan.classList.add('fa-sort-up'); // Add ascending arrow
            } else {
                clickedIconSpan.classList.add('fa-sort-down'); // Add descending arrow
            }
        }

        sortTable(column, direction);
    });
});

        function sortTable(column, direction) {
            const rows = Array.from(studentsTableBody.querySelectorAll('tr'));

            const dataRows = rows.filter(row => {
                const cells = row.querySelectorAll('td');
                return cells.length === 7 || row.querySelector('.btn-action-edit');
            });

            dataRows.sort((rowA, rowB) => {
                const cellA = rowA.querySelectorAll('td')[column];
                const cellB = rowB.querySelectorAll('td')[column];

                if (!cellA || !cellB) return 0;

                let valA = cellA.textContent.trim();
                let valB = cellB.textContent.trim();

                if (column === 0 || column === 3) {
                    valA = parseInt(valA) || 0;
                    valB = parseInt(valB) || 0;
                } else {
                    valA = valA.toLowerCase();
                    valB = valB.toLowerCase();
                }

                if (direction === 'asc') {
                    return (valA > valB) ? 1 : (valA < valB) ? -1 : 0;
                } else {
                    return (valA < valB) ? 1 : (valA > valB) ? -1 : 0;
                }
            });

            dataRows.forEach(row => studentsTableBody.appendChild(row));
            applyTableFilters(); // Crucial to re-apply filters after sorting
        }

        // --- Delete Student Functionality (Corrected Position) ---
       
        // ✅ Dark mode toggle placed correctly within DOMContentLoaded (at the very end)
        const modeSwitch = document.querySelector('.toggle-switch');
        const modeText = document.querySelector('.mode-text');
        if (modeSwitch && modeText) {
            modeSwitch.addEventListener('click', () => {
                document.body.classList.toggle('dark');
                modeText.textContent = document.body.classList.contains('dark') ?
                    'Light mode' : 'Dark mode';
            });
        }
    }); // This is the ONLY closing brace for document.addEventListener('DOMContentLoaded', ...);
</script>


</body>
</html>
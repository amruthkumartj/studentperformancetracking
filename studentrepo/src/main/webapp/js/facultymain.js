document.addEventListener('DOMContentLoaded', function () {
    'use strict';



    // --- UTILITY FUNCTIONS ---
    function qs(selector) { return document.querySelector(selector); }
    function qsa(selector) { return document.querySelectorAll(selector); }
    function show(el, display) { if (el) el.style.display = display ? 'block' : 'none'; }
    function showFlex(el, display) { if (el) el.style.display = display ? 'flex' : 'none'; }
    function disable(sel, yes) {
        if (sel) {
            sel.disabled = yes;
            if (yes) sel.selectedIndex = 0;
        }
    }
    function debounce(func, delay) {
        var timeout;
        return function() {
            var context = this;
            var args = arguments;
            clearTimeout(timeout);
            timeout = setTimeout(function() {
                func.apply(context, args);
            }, delay);
        };
    }
	
    
    // --- GENERAL UI LISTENERS ---
    document.addEventListener('click', function(e) {
        var activeDropdown = qs('.has-dropdown.active');
        if (activeDropdown && !activeDropdown.contains(e.target)) {
            activeDropdown.classList.remove('active');
        }
        if (e.target.closest('.dropdown-menu a')) {
            if (activeDropdown) {
                activeDropdown.classList.remove('active');
            }
        }
		
    });

    /* ───────── UI ELEMENT REFERENCES ───────── */
    var sidebar = qs('nav.sidebar');
    var toggleBtn = qs('.toggle');
    var modeSwitch = qs('.toggle-switch');
    var modeText = qs('.mode-text');

    /* ───────── SIDEBAR & DARK MODE LOGIC ───────── */
    if (toggleBtn) {
        toggleBtn.addEventListener('click', function() {
            sidebar.classList.toggle('close');
            sidebar.classList.toggle('open', !sidebar.classList.contains('close'));
        });
    }

  

    Array.prototype.forEach.call(qsa('.has-dropdown > a'), function(a) {
        a.addEventListener('click', function(e) {
            e.preventDefault();
            var parentLi = a.closest('.has-dropdown');
            var isActive = parentLi.classList.contains('active');
            Array.prototype.forEach.call(qsa('.has-dropdown'), function(li) {
                if (li !== parentLi) li.classList.remove('active');
            });
            parentLi.classList.toggle('active');
        });
    });

    document.addEventListener('click', function(e) {
        if (!sidebar.contains(e.target) && !e.target.matches('.toggle')) {
            Array.prototype.forEach.call(qsa('.has-dropdown'), function(li) {
                li.classList.remove('active');
            });
        }
    });

    /* ───────── MAIN NAVIGATION ROUTING ───────── */
	var mainSections = {
	        dashboard: qs('#dashboardSection'),
	        students: qs('#studentFeatures'),
	        attendance: qs('#attendanceManagementSection'),
	        marks: qs('#marksManagementSection'),
	        profile: qs('#profileSection'),
	        scheduleLanding: qs('#scheduleLandingSection'),
	        addSchedule: qs('#dailyClassScheduleSection'),
	        viewSchedule: qs('#viewTimetableSection'),
	        eventManagement: qs('#eventManagementSection'),
			aiAssistant: qs('#aiAssistantSection')
			
	    };

	    var navMapping = {
	        'dashboardNavLink': { section: mainSections.dashboard, init: null },
	        'manageStudentsLink': { section: mainSections.students, init: window.initStudentFeature },
	        'attendanceNavLink': { section: mainSections.attendance, init: window.initAttendanceFeature },
	        'enterMarksNavLink': { section: mainSections.marks, init: window.initMarksFeature },
	        'profileNavLink': { section: mainSections.profile, init: window.initProfileFeature },
	        'manageScheduleLink': { section: mainSections.scheduleLanding, init: window.initScheduleLanding },
	        'hostEventsLink': { section: mainSections.eventManagement, init: window.initEventManagement },
			'aiAssistantNavLink': { 
			    section: mainSections.aiAssistant, 
			    init: function() {
			        // This function runs when the AI Assistant link is clicked.
			        // It finds the settings button on the main dashboard card and makes it visible.
			        const settingsBtn = document.querySelector('#ai-assistant-card .ai-settings-btn');
			        if (settingsBtn) {
			            settingsBtn.style.display = 'block'; // Or 'inline-block' if needed
			        }
			    } 
			}
	    };

	    function hideAllSections() {
	        for (var key in mainSections) {
	            if (mainSections.hasOwnProperty(key)) {
	                if (mainSections[key]) {
	                    mainSections[key].style.display = 'none';
	                }
	            }
	        }
	    }

	    function showOnly(sectionToShow, initFunc) {
	        hideAllSections();
	        if (sectionToShow) {
	            sectionToShow.style.display = 'block';
	        }
	        if (typeof initFunc === 'function') {
	            initFunc();
	        }
	    }

	    for (var linkId in navMapping) {
	        if (navMapping.hasOwnProperty(linkId)) {
	            (function() {
	                var currentLinkId = linkId;
	                var section = navMapping[currentLinkId].section;
	                var init = navMapping[currentLinkId].init;
	                var linkElement = document.getElementById(currentLinkId);

	                if (linkElement && section) {
	                    linkElement.addEventListener('click', function(e) {
	                        e.preventDefault();
	                        showOnly(section, init);
	                        Array.prototype.forEach.call(qsa('.menu-links a'), function(a) { a.classList.remove('active-nav'); });
	                        linkElement.classList.add('active-nav');
	                        var parentDropdown = linkElement.closest('.has-dropdown');
	                        if (parentDropdown) { parentDropdown.querySelector('a').classList.add('active-nav'); }
	                        if (window.innerWidth <= 768 && sidebar) { sidebar.classList.add('close'); sidebar.classList.remove('open'); }
	                    });
	                }
	            })();
	        }
	    }

		/* =====================================================
		   ==========  PROFILE & PASSWORD LOGIC  ===============
		   ===================================================== */
		   window.initProfileFeature = function() {
		         // Find the container with the embedded data
		         var profileContainer = qs('#profileSection .home-content');
		         if (!profileContainer) {
		             console.error("Profile section container not found!");
		             return;
		         }

		         // Read the data directly from the data-* attributes
		         var username = profileContainer.dataset.username;
		         var email = profileContainer.dataset.email;
		         var role = profileContainer.dataset.role;
		         var facultyId = profileContainer.dataset.id;
		         var phone = profileContainer.dataset.phone;

		         // Populate Profile Data
		         qs('#profileFacultyName').textContent = username || 'N/A';
		         qs('#profileFacultyId').textContent = facultyId || 'N/A';
		         qs('#profileFacultyEmail').textContent = email || 'N/A';
		         qs('#profileFacultyRole').textContent = role || 'N/A';
		         
		         // As requested, display "Not Set" if the phone number is missing
		         qs('#profileFacultyPhone').textContent = phone || 'Not Set';

		         // --- Password change logic ---
		         var openBtn = qs('#openChangePasswordBtn');
		         var saveBtn = qs('#savePasswordBtn');
		         var passwordForm = qs('#changePasswordFormContainer');
		         var messageEl = qs('#passwordChangeMessage');

		         // Add listener to show/hide the password form
		         if (openBtn && !openBtn.hasClickListener) {
		             openBtn.addEventListener('click', function() {
		                 passwordForm.classList.toggle('is-visible');
		             });
		             openBtn.hasClickListener = true;
		         }

		         // Add listener to save the new password
		         if (saveBtn && !saveBtn.hasClickListener) {
		             saveBtn.addEventListener('click', function() {
		                 var newPassword = qs('#newPassword').value;
		                 var confirmPassword = qs('#confirmPassword').value;
		                 messageEl.textContent = '';

		                 // Validation checks
		                 if (!newPassword || !confirmPassword) {
		                     messageEl.style.color = 'red';
		                     messageEl.textContent = 'Please fill in both password fields.';
		                     return;
		                 }
		                 if (newPassword !== confirmPassword) {
		                     messageEl.style.color = 'red';
		                     messageEl.textContent = 'Passwords do not match.';
		                     return;
		                 }
		                 if (newPassword.length < 6) {
		                     messageEl.style.color = 'red';
		                     messageEl.textContent = 'Password must be at least 6 characters long.';
		                     return;
		                 }

		                 messageEl.style.color = 'grey';
		                 messageEl.textContent = 'Saving...';
		                 saveBtn.disabled = true;

		                 fetch('change-password-servlet', {
		                     method: 'POST',
		                     headers: { 'Content-Type': 'application/json' },
		                     body: JSON.stringify({ newPassword: newPassword, facultyId: facultyId })
		                 })
		                 .then(function(response) { return response.json(); })
		                 .then(function(result) {
		                     if (result.success) {
		                         messageEl.style.color = 'green';
		                         messageEl.textContent = result.message || 'Password changed successfully!';
		                         setTimeout(function() {
		                             passwordForm.classList.remove('is-visible');
		                             messageEl.textContent = '';
		                             qs('#newPassword').value = '';
		                             qs('#confirmPassword').value = '';
		                         }, 2000);
		                     } else {
		                         messageEl.style.color = 'red';
		                         messageEl.textContent = result.message || 'An error occurred.';
		                     }
		                 })
		                 .catch(function(error) {
		                     console.error('Password change error:', error);
		                     messageEl.style.color = 'red';
		                     messageEl.textContent = 'Request failed. Please try again.';
		                 })
		                 .finally(function() {
		                     saveBtn.disabled = false;
		                 });
		             });
		             saveBtn.hasClickListener = true;
		         }
		     };

	/* =====================================================
	   ==========  COMMAND PALETTE SCRIPT  =================
	   ===================================================== */
	var searchModal = qs('#searchModal');
	var modalInput = qs('#modalSearchInput');
	var modalClearBtn = qs('#modalClearBtn');
	var modalResults = qs('#modalSearchResults');

	var commands = [
	    { name: 'Dashboard', hint: 'Go to the main dashboard', icon: 'bx-home-alt', action: function() { qs('#dashboardNavLink').click(); } },
	    { name: 'Add Student', hint: 'Go to student management', icon: 'bx-user-plus', action: function() { qs('#manageStudentsLink').click(); window.showStudentFeature('add'); } },
	    { name: 'View Students', hint: 'Go to student management', icon: 'bx-list-ul', action: function() { qs('#manageStudentsLink').click(); window.showStudentFeature('view'); } },
	    { name: 'Take Attendance', hint: 'Go to attendance', icon: 'bx-check-square', action: function() { qs('#attendanceNavLink').click(); window.showAttendanceAction('take'); } },
	    { name: 'Enter Marks', hint: 'Go to marks entry', icon: 'bx-pencil', action: function() { qs('#enterMarksNavLink').click(); } },
	    { name: 'View Profile', hint: 'View your faculty profile', icon: 'bx-user', action: function() { qs('#profileNavLink').click(); } }
	];

	// Function to open the search modal
	function openSearchModal() {
	    const searchModal = document.getElementById('searchModal');
	    if (searchModal) {
	        searchModal.style.display = 'flex';
	        setTimeout(() => {
	            searchModal.classList.add('visible');
	            document.getElementById('modalSearchInput').focus();
	        }, 10);
	    }
	}

	// Function to close the search modal
	function closeSearchModal() {
	    const searchModal = document.getElementById('searchModal');
	    if (searchModal) {
	        searchModal.classList.remove('visible');
	        setTimeout(() => {
	            searchModal.style.display = 'none';
	        }, 300);
	    }
	}

	if (qs('#openSearchModalBtn')) {
	    qs('#openSearchModalBtn').addEventListener('click', openSearchModal);
	}
	if (searchModal) {
	    searchModal.addEventListener('click', function(e) { if (e.target === searchModal) closeSearchModal(); });
	}
	if (modalClearBtn) {
	    modalClearBtn.addEventListener('click', function() { modalInput.value = ''; modalInput.focus(); handleModalSearch(); });
	}

	function displayModalResults(commands, students) {
	    if (!modalResults) return;
	    modalResults.innerHTML = '';
	    
	    commands.forEach(function(cmd) {
	        var item = document.createElement('div');
	        item.className = 'search-result-item';
	        item.innerHTML = '<div class="command-info"><i class="bx ' + cmd.icon + ' result-icon"></i><div><span class="command-name">' + cmd.name + '</span><span class="command-hint">' + cmd.hint + '</span></div></div>';
	        item.addEventListener('click', function() {
	            cmd.action();
	            closeSearchModal();
	        });
	        modalResults.appendChild(item);
	    });

	    if (students && students.length > 0) {
	        students.forEach(function(s) {
	            var item = document.createElement('div');
	            item.className = 'search-result-item';
	            item.innerHTML = '<div class="student-info"><i class="bx bxs-user-circle result-icon"></i>' +
	                             '<div><span class="student-name">' + s.fullName + '</span><span class="student-details">' + s.programName + ' - Sem ' + s.semester + '</span></div></div>' +
	                             '<div class="student-actions">' +
	                                '<button class="action-btn" title="View Performance" data-action="viewPerformance" data-id="' + s.studentId + '">📊</button>' +
	                             '</div>';
	            modalResults.appendChild(item);
	        });
	    }
	}

	var handleModalSearch = debounce(function() {
	    if (!modalInput) return;
	    var searchTerm = modalInput.value.toLowerCase().trim();
	    if (modalClearBtn) modalClearBtn.style.display = searchTerm.length > 0 ? 'block' : 'none';
	    
	    var filteredCommands = commands.filter(function(cmd) {
	        return cmd.name.toLowerCase().indexOf(searchTerm) !== -1;
	    });

	    if (searchTerm.length >= 2) {
	        fetch('GetStudentsServlet', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify({ searchTerm: searchTerm })
	        })
	        .then(function(r) { return r.json(); })
	        .then(function(students) {
	            displayModalResults(filteredCommands, students);
	        })
	        .catch(function(err) {
	            console.error("Student search fetch failed:", err);
	            displayModalResults(filteredCommands, []);
	        });
	    } else {
	        displayModalResults(filteredCommands, []);
	    }
	}, 300);

	if (modalResults) {
	    modalResults.addEventListener('click', function(e) {
	        var studentActionButton = e.target.closest('.action-btn[data-action="viewPerformance"]');
	        if (studentActionButton) {
	            var studentId = studentActionButton.dataset.id;
	            window.location.href = 'viewStudentPerformance.jsp?studentId=' + studentId;
	            closeSearchModal();
	        }
	    });
	}

	if (modalInput) {
	    modalInput.addEventListener('keyup', function(e) {
	        if (e.key === 'Escape' || e.key === 'Esc') return closeSearchModal();
	        handleModalSearch();
	    });
	}
    
    /* =====================================================
	   =======  SCHEDULE & EVENTS LOGIC (MERGED)  ==========
	   ===================================================== */
    var scheduleLandingSection = qs('#scheduleLandingSection');
    var dailyClassScheduleSection = qs('#dailyClassScheduleSection');
    var viewTimetableSection = qs('#viewTimetableSection');
    var dailyClassForm = qs('#dailyClassForm');
    var scheduleProgramSelect = qs('#scheduleProgramSelect');
    var scheduleSemesterSelect = qs('#scheduleSemesterSelect');
    var scheduleDateInput = qs('#scheduleDateInput');
    var scheduleDayDisplay = qs('#scheduleDayDisplay');
    var scheduleRecurrenceCheckbox = qs('#scheduleRecurrenceCheckbox');
    var recurrenceOptions = qs('#recurrenceOptions');
    var regularTimetableBody = qs('#regularTimetableBody');
    var extraClassesTableBody = qs('#extraClassesTableBody');
    var timetableProgramSelect = qs('#timetableProgramSelect');
    var timetableSemesterSelect = qs('#timetableSemesterSelect');
    var timetableDisplay = qs('#timetableDisplay');
    
    // === NEW: Event Management Selectors (Added Back) ===
    var eventFeatureCards = qs('#eventFeatureCards');
    var viewAndManageEventsSection = qs('#viewAndManageEventsSection');
    var eventsTableBody = qs('#eventsTableBody');
    var noEventsMessage = qs('#noEventsMessage');
    var eventSearchInput = qs('#eventSearchInput');
    var eventTypeFilter = qs('#eventTypeFilter');
    var resetEventFiltersBtn = qs('#resetEventFiltersBtn');

    window.lastFetchedSchedules = null;

    window.showScheduleSection = function(sectionToShow) {
        hideAllSections();
        switch(sectionToShow) {
            case 'landing': show(scheduleLandingSection, true); break;
            case 'addExtraClass': show(dailyClassScheduleSection, true); initAddExtraClass(); break;
            case 'viewTimetable': show(viewTimetableSection, true); initTimetableView(); break;
            // REMOVED Obsolete 'hostEvents' case
            default: show(scheduleLandingSection, true); break;
        }
    };
    
    window.initScheduleLanding = function() {
        showOnly(mainSections.scheduleLanding, null);
    };

    function initAddExtraClass() {
        populateScheduleProgramSelect();
        if (scheduleDateInput) { scheduleDateInput.min = new Date().toISOString().split("T")[0]; }
        var initialMessage = '<tr><td colspan="4" style="text-align:center;">Select program, semester, and date to view schedule.</td></tr>';
        if(regularTimetableBody) regularTimetableBody.innerHTML = initialMessage;
        if(extraClassesTableBody) extraClassesTableBody.innerHTML = initialMessage;
        
        // Back button is now handled by inline onclick in JSP
        var cancelBtn = qs('#cancelDailyScheduleBtn');
        if(cancelBtn) cancelBtn.addEventListener('click', () => window.showScheduleSection('landing'));
        
        if (scheduleProgramSelect) { scheduleProgramSelect.addEventListener('change', function() { disable(scheduleSemesterSelect, false); scheduleSemesterSelect.value = ''; loadCurrentDailySchedules(); }); }
        if (scheduleSemesterSelect) { scheduleSemesterSelect.addEventListener('change', loadCurrentDailySchedules); }
        if (scheduleDateInput) { scheduleDateInput.addEventListener('change', function() { if (this.value) { var date = new Date(this.value); var userTimezoneOffset = date.getTimezoneOffset() * 60000; var correctedDate = new Date(date.getTime() + userTimezoneOffset); if (scheduleDayDisplay) scheduleDayDisplay.value = correctedDate.toLocaleDateString('en-US', { weekday: 'long' }); } else { if (scheduleDayDisplay) scheduleDayDisplay.value = ''; } loadCurrentDailySchedules(); }); }
        if (scheduleRecurrenceCheckbox) { scheduleRecurrenceCheckbox.addEventListener('change', function() { if (recurrenceOptions) show(recurrenceOptions, this.checked); }); }
        if (dailyClassForm) {
            dailyClassForm.addEventListener('submit', function(e) { e.preventDefault(); var formData = new FormData(dailyClassForm); var data = {}; formData.forEach(function(value, key) { data[key] = value; }); fetch('AddDailyScheduleServlet', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) }).then(function(response) { return response.json().then(function(json) { return { ok: response.ok, json: json, data: data }; }); }).then(function(result) { switch(result.json.status) { case 'success': alert(result.json.message); dailyClassForm.reset(); loadCurrentDailySchedules(); break; case 'error': alert(result.json.message); break; case 'warning': showConflictWarning(result.json, result.data); break; default: throw new Error(result.json.message || 'Unknown server response.'); } }).catch(function(error) { console.error('Scheduling Error:', error); alert("An error occurred: " + error.message); }); });
        }
    }

    function resetTimetableFilters() {
        if (timetableProgramSelect) timetableProgramSelect.value = '';
        if (timetableSemesterSelect) {
            timetableSemesterSelect.value = '';
            disable(timetableSemesterSelect, true);
        }
        if (timetableDisplay) timetableDisplay.innerHTML = '<p class="initial-message">Please select a program and semester.</p>';
    }

    window.initTimetableView = function() {
        resetTimetableFilters();
        if (!timetableProgramSelect) return;
        if (timetableProgramSelect.options.length <= 1 && window.allProgramsData) {
            window.allProgramsData.forEach(function(program) {
                var option = document.createElement('option');
                option.value = program.programId;
                option.textContent = program.programName;
                timetableProgramSelect.appendChild(option);
            });
        }
        // Back button is now handled by inline onclick in JSP
        var resetBtn = qs('#resetTimetableFiltersBtn');
        if (resetBtn) resetBtn.addEventListener('click', resetTimetableFilters);
        if (timetableProgramSelect) { timetableProgramSelect.addEventListener('change', function() { disable(timetableSemesterSelect, false); timetableSemesterSelect.value = ''; timetableDisplay.innerHTML = '<p class="initial-message">Please select a semester.</p>'; }); }
        if (timetableSemesterSelect) { timetableSemesterSelect.addEventListener('change', fetchAndDisplayTimetable); }
    };
    
    function populateScheduleProgramSelect() {
        if (!scheduleProgramSelect || !window.allProgramsData) return;
        while (scheduleProgramSelect.options.length > 1) { scheduleProgramSelect.remove(1); }
        window.allProgramsData.forEach(function(program) {
            var option = document.createElement('option');
            option.value = program.programId;
            option.textContent = program.programName;
            scheduleProgramSelect.appendChild(option);
        });
    }

    function loadCurrentDailySchedules() {
        var programId = scheduleProgramSelect.value;
        var semester = scheduleSemesterSelect.value;
        var classDate = scheduleDateInput.value;
        if (!programId || !semester || !classDate) return;
        var loadingMessage = '<tr><td colspan="4" style="text-align:center;">Loading...</td></tr>';
        if (regularTimetableBody) regularTimetableBody.innerHTML = loadingMessage;
        if (extraClassesTableBody) extraClassesTableBody.innerHTML = loadingMessage;
        var fetchUrl = 'FetchDailySchedulesServlet?programId=' + programId + '&semester=' + semester + '&date=' + classDate;
        fetch(fetchUrl).then(function(response) { return response.ok ? response.json() : Promise.reject('Failed to load'); }).then(function(data) {
            if (data.status !== 'success') { throw new Error(data.message || 'Server error.'); }
            if (regularTimetableBody) regularTimetableBody.innerHTML = '';
            if (extraClassesTableBody) extraClassesTableBody.innerHTML = '';
            var regularCount = 0;
            var extraCount = 0;
            data.schedules.forEach(function(schedule) {
                if (schedule.type === 'regular') {
                    regularCount++;
                    var row = '<tr><td>' + (schedule.time || 'N/A') + '</td><td>' + (schedule.subjectName || 'N/A') + '</td><td>' + (schedule.location || 'N/A') + '</td></tr>';
                    if (regularTimetableBody) regularTimetableBody.insertAdjacentHTML('beforeend', row);
                } else if (schedule.type === 'extra') {
                    extraCount++;
                    var actionCell = '<td><button onclick="window.deleteDailySchedule(' + schedule.id + ')" class="btn-danger">Delete</button></td>';
                    var row = '<tr><td>' + (schedule.time || 'N/A') + '</td><td>' + (schedule.subjectName || 'N/A') + '</td><td>' + (schedule.location || 'N/A') + '</td>' + actionCell + '</tr>';
                    if (extraClassesTableBody) extraClassesTableBody.insertAdjacentHTML('beforeend', row);
                }
            });
            if (regularCount === 0 && regularTimetableBody) { regularTimetableBody.innerHTML = '<tr><td colspan="3" style="text-align:center;">No regular classes scheduled.</td></tr>'; }
            if (extraCount === 0 && extraClassesTableBody) { extraClassesTableBody.innerHTML = '<tr><td colspan="4" style="text-align:center;">No extra classes scheduled.</td></tr>'; }
        }).catch(function(error) {
            var errorMessage = '<tr><td colspan="4" style="text-align:center;color:red;">Error loading schedules.</td></tr>';
            if (regularTimetableBody) regularTimetableBody.innerHTML = errorMessage;
            if (extraClassesTableBody) extraClassesTableBody.innerHTML = errorMessage;
        });
    }

    function showConflictWarning(result, dataToConfirm) {
        var overlay = document.createElement('div');
        overlay.className = 'confirm-overlay';
        var dialog = document.createElement('div');
        dialog.className = 'confirm-dialog';
        var message = document.createElement('p');
        message.textContent = result.message;
        var buttonContainer = document.createElement('div');
        buttonContainer.className = 'buttons';
        var proceedBtn = document.createElement('button');
        proceedBtn.textContent = 'Save Non-Conflicting Dates';
        proceedBtn.className = 'btn-primary';
        proceedBtn.onclick = function() {
            dataToConfirm.forceConfirmation = true;
            fetch('AddDailyScheduleServlet', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dataToConfirm) }).then(function(response) { return response.json(); }).then(function(finalResult) { alert(finalResult.message); if(finalResult.status === 'success') { document.querySelector('#dailyClassForm').reset(); loadCurrentDailySchedules(); } }).catch(function() { alert("An error occurred during confirmation."); }).finally(function() { document.body.removeChild(overlay); });
        };
        var cancelBtn = document.createElement('button');
        cancelBtn.textContent = 'Cancel';
        cancelBtn.className = 'btn-secondary';
        cancelBtn.onclick = function() { document.body.removeChild(overlay); };
        buttonContainer.appendChild(proceedBtn);
        buttonContainer.appendChild(cancelBtn);
        dialog.appendChild(message);
        dialog.appendChild(buttonContainer);
        overlay.appendChild(dialog);
        document.body.appendChild(overlay);
    }
    
    window.deleteDailySchedule = function(id) {
        if (confirm('Are you sure you want to delete this schedule?')) {
            fetch('DeleteDailyScheduleServlet', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: 'id=' + id }).then(response => response.json()).then(data => { if (data.status === 'success') { alert('Schedule deleted successfully.'); loadCurrentDailySchedules(); } else { alert('Failed to delete schedule: ' + (data.message || 'Unknown error.')); } }).catch(error => { console.error('Delete schedule error:', error); alert('An error occurred while deleting the schedule.'); });
        }
    };
    
    window.initScheduleFeature = window.initScheduleLanding;

    function fetchAndDisplayTimetable() {
        var programId = timetableProgramSelect.value;
        var semester = timetableSemesterSelect.value;
        if (!programId || !semester) return;
        timetableDisplay.innerHTML = '<div class="spinner"></div>';
        
        fetch('FetchAllSchedulesServlet?programId=' + programId + '&semester=' + semester)
            .then(response => {
                var contentType = response.headers.get("content-type");
                if (contentType && contentType.indexOf("application/json") !== -1) {
                    return response.json();
                } else {
                    return response.text().then(function(text) {
                        throw new Error('Server returned non-JSON response. Please check server logs for errors. Response content: ' + text);
                    });
                }
            })
            .then(data => {
                if (data.status !== 'success') { throw new Error(data.message || 'Server error.'); }
                window.lastFetchedSchedules = data.schedules;
                timetableDisplay.innerHTML = '';
                var daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
                daysOfWeek.forEach(function(day) {
                    var daySchedule = data.schedules[day] || [];
                    var dayColumn = document.createElement('div');
                    dayColumn.className = 'day-column';
                    var dayTitle = document.createElement('h3');
                    dayTitle.textContent = day.charAt(0) + day.slice(1).toLowerCase();
                    dayColumn.appendChild(dayTitle);
                    if (daySchedule.length > 0) {
                        daySchedule.forEach(function(schedule) {
                            if (schedule.type === 'extra' && isScheduleExpired(schedule)) { return; }
                            var card = document.createElement('div');
                            card.className = 'schedule-card ' + (schedule.type === 'extra' ? 'extra-class' : '');
                            var cardContent = '<div class="time">' + (schedule.time || 'N/A') + '</div>';
                            if (schedule.type === 'extra') {
                                cardContent += '<div class="subject"><span class="extra-class-label">Extra Class</span><span class="extra-class-subject"><br>' + (schedule.subjectName || 'N/A') + '</span>';
                                if (schedule.isRecurring) {
                                    cardContent += '<small class="extra-class-date">&nbsp;&nbsp;[Recurring: ' + schedule.classDate + ' to ' + schedule.recurrenceEndDate + ']</small>';
                                } else {
                                    cardContent += '<small class="extra-class-date">&nbsp;&nbsp;[Scheduled Class For Current Day]</small>';
                                }
                                cardContent += '</div>';
                            } else {
                                cardContent += '<div class="subject">' + (schedule.subjectName || 'N/A') + '</div>';
                            }
                            cardContent += '<div class="location">' + (schedule.location || 'N/A') + '</div>';
                            card.innerHTML = cardContent;
                            dayColumn.appendChild(card);
                        });
                    } else {
                        var noClassCard = document.createElement('div');
                        noClassCard.className = 'schedule-card';
                        noClassCard.innerHTML = '<div class="location">No classes scheduled.</div>';
                        dayColumn.appendChild(noClassCard);
                    }
                    timetableDisplay.appendChild(dayColumn);
                });
            })
            .catch(function(error) {
                console.error("Error loading timetable:", error);
                timetableDisplay.innerHTML = '<p style="color:red; text-align:center;">Error loading timetable: ' + error.message + '</p>';
            });
    }

    function isScheduleExpired(schedule) {
        var today = new Date().toISOString().split('T')[0];
        if (!schedule.isRecurring && schedule.classDate < today) { return true; }
        if (schedule.isRecurring && schedule.recurrenceEndDate && schedule.recurrenceEndDate < today) { return true; }
        return false;
    }

    var scheduleLandingSection = qs('#scheduleLandingSection');
    if(scheduleLandingSection) {
        scheduleLandingSection.addEventListener('click', function(e) {
            var card = e.target.closest('.feature-card');
            if (card) {
                if (card.id === 'addExtraClassFeatureLink') {
                    window.showScheduleSection('addExtraClass');
                } else if (card.id === 'viewWeeklyTimetableFeatureLink') {
                    window.showScheduleSection('viewTimetable');
                }
                // REMOVED Obsolete hostEventsFeatureLink case
            }
        });
    }

    // === NEW: Event Management Logic (Carefully Added Back) ===
    
    /**
     * Initializes the Event Management section and attaches listeners.
     */
	// In facultymain.js, replace the code from this point to the end of the file

	    /**
	     * Initializes the Event Management section.
	     */
		// In facultymain.js, replace the entire initProfileFeature function

		window.initProfileFeature = function() {
		    // Find the container with the embedded data
		    var profileContainer = qs('#profileSection .home-content');
		    if (!profileContainer) return;

		    // Read the data directly from the data-* attributes
		    var username = profileContainer.dataset.username;
		    var email = profileContainer.dataset.email;
		    var role = profileContainer.dataset.role;
		    var facultyId = profileContainer.dataset.id;
		    var phone = profileContainer.dataset.phone;

		    // Populate Profile Data
		    qs('#profileFacultyName').textContent = username || 'N/A';
		    qs('#profileFacultyId').textContent = facultyId || 'N/A';
		    qs('#profileFacultyEmail').textContent = email || 'N/A';
		    qs('#profileFacultyRole').textContent = role || 'N/A';
		    qs('#profileFacultyPhone').textContent = phone || 'Not Set';

		    // --- Password change logic ---
		    var openBtn = qs('#openChangePasswordBtn');
		    var saveBtn = qs('#savePasswordBtn');
		    var passwordForm = qs('#changePasswordFormContainer');
		    var messageEl = qs('#passwordChangeMessage');

		    if (openBtn && !openBtn.hasClickListener) {
		        openBtn.addEventListener('click', function() {
		            var isVisible = passwordForm.classList.contains('is-visible');
		            passwordForm.classList.toggle('is-visible');
		        });
		        openBtn.hasClickListener = true;
		    }

		    if (saveBtn && !saveBtn.hasClickListener) {
		        saveBtn.addEventListener('click', function() {
		            // ... (The save password logic you already have is correct)
		        });
		        saveBtn.hasClickListener = true;
		    }
		};

	    /**
	     * Shows a specific feature within the event management section.
	     * This is called by the feature cards and back button in the JSP.
	     */
	    window.showEventManagementFeature = function(feature) {
	        var eventFeatureCards = qs('#eventFeatureCards');
	        var viewAndManageEventsSection = qs('#viewAndManageEventsSection');
	        if (feature === 'view') {
	            showFlex(eventFeatureCards, false);
	            show(viewAndManageEventsSection, true);
	            fetchAndDisplayManageableEvents();
	        } else {
	            showFlex(eventFeatureCards, true);
	            show(viewAndManageEventsSection, false);
	        }
	    };

	    /**
	     * Fetches events from the server and then filters them on the client-side.
	     */
	    function fetchAndDisplayManageableEvents() {
	        var isAdmin = (window.currentUserRole === 'ADMIN');
	        var colspan = isAdmin ? 5 : 4; 
	        var eventsTableBody = qs('#eventsTableBody');
	        var noEventsMessage = qs('#noEventsMessage');

	        eventsTableBody.innerHTML = '<tr><td colspan="' + colspan + '" style="text-align:center;">Loading events...</td></tr>';
	        show(noEventsMessage, false);

	        fetch('fetch-faculty-events')
	            .then(function(response) {
	                if (!response.ok) throw new Error('Network response was not ok');
	                return response.json();
	            })
	            .then(function(events) {
	                var searchTerm = qs('#eventSearchInput').value.toLowerCase();
	                var selectedType = qs('#eventTypeFilter').value;

	                var filteredEvents = events.filter(function(event) {
	                    var matchesSearch = event.eventName.toLowerCase().indexOf(searchTerm) !== -1;
	                    var matchesType = !selectedType || event.eventCategory === selectedType;
	                    return matchesSearch && matchesType;
	                });
	                
	                populateEventsTable(filteredEvents);
	            })
	            .catch(function(error) {
	                console.error('Error fetching events:', error);
	                eventsTableBody.innerHTML = '<tr><td colspan="' + colspan + '" style="text-align:center; color:red;">Failed to load events.</td></tr>';
	            });
	    }

	    /**
	     * Populates the management table with event data, adjusting for Admin/Faculty roles.
	     */
	    function populateEventsTable(events) {
	        var eventsTableBody = qs('#eventsTableBody');
	        var noEventsMessage = qs('#noEventsMessage');
	        eventsTableBody.innerHTML = '';
	        
	        var isAdmin = (window.currentUserRole === 'ADMIN');
	        var tableHeader = qs('#eventsTable thead tr');
	        var adminHeader = qs('#eventsTable th.admin-col');

	        if (isAdmin && !adminHeader) {
	            var th = document.createElement('th');
	            th.className = 'admin-col';
	            th.textContent = 'Hosted By';
	            tableHeader.insertBefore(th, tableHeader.lastElementChild);
	        } else if (!isAdmin && adminHeader) {
	            adminHeader.remove();
	        }

	        if (events.length === 0) {
	            show(noEventsMessage, true);
	            return;
	        }
	        show(noEventsMessage, false);

	        events.forEach(function(event) {
	            var eventDate = new Date(event.eventDate).toLocaleDateString('en-GB', {
	                day: '2-digit', month: 'short', year: 'numeric'
	            });
	            var row = document.createElement('tr');
	            var rowHtml =
	                '<td>' + (event.eventName || 'N/A') + '</td>' +
	                '<td>' + (event.eventCategory || 'N/A') + '</td>' +
	                '<td>' + eventDate + '</td>';
	            
	            if (isAdmin) {
	                rowHtml += '<td class="admin-col">' + (event.facultyName || 'N/A') + '</td>';
	            }
	            
	            rowHtml +=
	                '<td class="actions">' +
	                    '<button class="btn-action edit" onclick="alert(\'Edit event ID: ' + event.eventId + '\')" title="Edit Event"><i class="bx bx-pencil"></i></button>' +
	                    '<button class="btn-action delete" onclick="alert(\'Delete event ID: ' + event.eventId + '\')" title="Delete Event"><i class="bx bx-trash"></i></button>' +
	                '</td>';
	            
	            row.innerHTML = rowHtml;
	            eventsTableBody.appendChild(row);
	        });
	    }

	    // === FIXED: Event Listeners for filters are now correctly placed ===
	    if (qs('#eventSearchInput')) {
	        qs('#eventSearchInput').addEventListener('input', debounce(fetchAndDisplayManageableEvents, 300));
	    }
	    if (qs('#eventTypeFilter')) {
	        qs('#eventTypeFilter').addEventListener('change', fetchAndDisplayManageableEvents);
	    }
	    if (qs('#resetEventFiltersBtn')) {
	        qs('#resetEventFiltersBtn').addEventListener('click', function() {
	            qs('#eventSearchInput').value = '';
	            qs('#eventTypeFilter').value = '';
	            fetchAndDisplayManageableEvents();
	        });
	    }

	    // Default view on page load
	    if (qs('#dashboardNavLink')) {
	        qs('#dashboardNavLink').classList.add('active-nav');
	    }
	    showOnly(mainSections.dashboard);
		// ====================================================================
		// FINAL JAVASCRIPT FOR AI ASSISTANT & GLOBAL SETTINGS (ES5 COMPATIBLE)
		// ====================================================================

		// --- Element References ---
		var aiAssistantCard = document.getElementById('ai-assistant-card');
		var aiLaunchBtn = document.querySelector('.ai-launch-btn-circle');
		var aiSettingsBtn = document.querySelector('.ai-settings-btn');
		var aiSettingsCloseBtn = document.querySelector('.ai-settings-close-btn');
		var globalAiFab = document.getElementById('global-ai-fab');
		var aiSearchModal = document.getElementById('ai-search-modal');
		var aiModalInput = document.getElementById('ai-modal-input');
		var aiModalCloseBtn = document.getElementById('ai-modal-close-btn');
		var aiModalSendBtn = document.getElementById('ai-modal-send-btn');
		var aiModalResults = document.getElementById('ai-modal-results');
		var enableGlobalAiToggle = document.getElementById('enable-global-ai-toggle');
		var GLOBAL_AI_ENABLED_KEY = 'globalAiAssistantEnabled';

		// --- References for Main AI Section ---
		var aiChatInput = document.getElementById('ai-chat-input');
		var aiChatSendBtn = document.getElementById('ai-chat-send-btn');
		var aiChatHistory = document.getElementById('ai-chat-history');
		var aiSectionSettingsBtn = document.querySelector('.ai-section-settings-btn');


		// --- Chat History Storage ---
		var chatHistory = [];


		// --- AI Card Logic (Dashboard Widget) ---
		if (aiLaunchBtn) {
		    aiLaunchBtn.addEventListener('click', function() {
		        showOnly(mainSections.aiAssistant, null);
		        displayMessages();
		    });
		}
		if (aiSettingsBtn) {
		    aiSettingsBtn.addEventListener('click', function() {
		        if (aiAssistantCard) {
		            aiAssistantCard.classList.add('is-settings-open');
		        }
		    });
		}
		if (aiSettingsCloseBtn) {
		    aiSettingsCloseBtn.addEventListener('click', function() {
		        if (aiAssistantCard) {
		            aiAssistantCard.classList.remove('is-settings-open');
		        }
		    });
		}

		// --- Floating Action Button (FAB) Logic ---
		function applyFabPreference() {
		    if (localStorage.getItem(GLOBAL_AI_ENABLED_KEY) === 'true') {
		        document.body.classList.add('global-ai-fab-enabled');
		        if (enableGlobalAiToggle) {
		            enableGlobalAiToggle.checked = true;
		        }
		    } else {
		        document.body.classList.remove('global-ai-fab-enabled');
		        if (enableGlobalAiToggle) {
		            enableGlobalAiToggle.checked = false;
		        }
		    }
		}

		if (enableGlobalAiToggle) {
		    enableGlobalAiToggle.addEventListener('change', function() {
		        localStorage.setItem(GLOBAL_AI_ENABLED_KEY, this.checked);
		        applyFabPreference();
		    });
		}

		// --- Global AI Modal Logic ---
		function openAiModal() {
		    if (aiSearchModal) {
		        aiSearchModal.style.display = 'flex';
		        setTimeout(function() {
		            aiSearchModal.classList.add('is-visible');
		            if (aiModalInput) {
		                aiModalInput.focus();
		            }
		            displayMessages();
		        }, 10);
		    }
		}

		function closeAiModal() {
		    if (aiSearchModal) {
		        aiSearchModal.classList.remove('is-visible');
		        setTimeout(function() {
		            aiSearchModal.style.display = 'none';
		            if (aiModalInput) {
		                aiModalInput.value = '';
		            }
		        }, 300);
		    }
		}

		if (globalAiFab) {
		    globalAiFab.addEventListener('click', openAiModal);
		}
		if (aiModalCloseBtn) {
		    aiModalCloseBtn.addEventListener('click', closeAiModal);
		}
		if (aiSearchModal) {
		    aiSearchModal.addEventListener('click', function(e) {
		        if (e.target === aiSearchModal) {
		            closeAiModal();
		        }
		    });
		}
		document.addEventListener('keydown', function(e) {
		    if ((e.key === "Escape" || e.keyCode === 27) && aiSearchModal && aiSearchModal.classList.contains('is-visible')) {
		        closeAiModal();
		    }
		});


		// --- Central AI Query Handler ---
		function handleAiQuery(userQuery) {
		    chatHistory.push({ role: 'user', text: userQuery });
		    displayMessages();

		    if (aiChatHistory) {
		        var thinkingDivMain = document.createElement('div');
		        thinkingDivMain.className = 'message assistant';
		        thinkingDivMain.innerHTML = '<i class="bx bxs-brain"></i><p>Thinking...</p>';
		        aiChatHistory.appendChild(thinkingDivMain);
		        aiChatHistory.scrollTop = aiChatHistory.scrollHeight;
		    }
		    if (aiModalResults) {
		        var thinkingDivModal = document.createElement('div');
		        thinkingDivModal.className = 'ai-response-block-wrapper';
		        thinkingDivModal.innerHTML = '<i class="bx bxs-brain"></i><div class="ai-response-block assistant">Thinking...</div>';
		        aiModalResults.appendChild(thinkingDivModal);
		        aiModalResults.scrollTop = aiModalResults.scrollHeight;
		    }

		    fetch('AssistantServlet', {
		            method: 'POST',
		            headers: { 'Content-Type': 'application/json' },
		            body: JSON.stringify({
		                history: chatHistory,
		                question: userQuery
		            })
		        })
		        .then(function(response) {
		            if (!response.ok) {
		                return response.text().then(function(text) {
		                    throw new Error('Network error: ' + text);
		                });
		            }
		            return response.text();
		        })
		        .then(function(aiResponseText) {
		            chatHistory.push({ role: 'assistant', text: aiResponseText });
		            displayMessages();
		        })
		        .catch(function(error) {
		            var errorMessage = 'Sorry, an error occurred. ' + error.message;
		            chatHistory.push({ role: 'assistant', text: errorMessage, isError: true });
		            displayMessages();
		        });
		}

		// --- NEW HELPER: Sanitize and format text for display ---
		function formatMessageText(text) {
		    var sanitizedText = text.replace(/</g, "&lt;").replace(/>/g, "&gt;");
		    var formattedText = sanitizedText.replace(/```([\s\S]*?)```/g, function(match, code) {
		        var trimmedCode = code.trim();
		        return '<pre><code>' + trimmedCode + '</code></pre>';
		    });
		    return formattedText;
		}


		// --- Central Display Function ---
		function displayMessages() {
		    if (chatHistory.length === 0) {
		        var welcomeMessage = '<div class="placeholder"><i class="bx bxs-brain"></i><p>How can I help you today?</p></div>';
		        if (aiChatHistory) aiChatHistory.innerHTML = welcomeMessage;
		        if (aiModalResults) aiModalResults.innerHTML = welcomeMessage;
		        return;
		    }

		    if (aiChatHistory) {
		        aiChatHistory.innerHTML = '';
		        chatHistory.forEach(function(message) {
		            var messageDiv = document.createElement('div');
		            messageDiv.className = 'message ' + message.role;
		            var iconClass = message.role === 'assistant' ? 'bxs-brain' : 'bxs-user';
		            var messageContent = '<i class="bx ' + iconClass + '"></i><p>' + formatMessageText(message.text) + '</p>';
		            if (message.isError) {
		                messageContent = '<i class="bx bxs-error-circle"></i><p class="error">' + formatMessageText(message.text) + '</p>';
		            }
		            messageDiv.innerHTML = messageContent;
		            aiChatHistory.appendChild(messageDiv);
		        });
		        aiChatHistory.scrollTop = aiChatHistory.scrollHeight;
		    }

		    if (aiModalResults) {
		        aiModalResults.innerHTML = '';
		        chatHistory.forEach(function(message) {
		            var messageDiv = document.createElement('div');
		            messageDiv.className = 'ai-response-block-wrapper ' + message.role;
		            var iconClass = message.role === 'assistant' ? 'bxs-brain' : 'bxs-user';
		            var messageContent = '<i class="bx ' + iconClass + '"></i><div class="ai-response-block">' + formatMessageText(message.text) + '</div>';
		            if (message.isError) {
		                iconClass = 'bxs-error-circle';
		                messageContent = '<i class="bx ' + iconClass + '"></i><div class="ai-response-block error">' + formatMessageText(message.text) + '</div>';
		            }
		            messageDiv.innerHTML = messageContent;
		            aiModalResults.appendChild(messageDiv);
		        });
		        aiModalResults.scrollTop = aiModalResults.scrollHeight;
		    }
		}

		// --- NEW FUNCTION: Clear Chat History ---
		function clearChatHistory() {
		    chatHistory = [];
		    displayMessages();
		    // Hide the floating clear button if it's visible
		    var floatingClearBtn = document.getElementById('ai-section-clear-btn');
		    if (floatingClearBtn) {
		        floatingClearBtn.classList.remove('visible');
		    }
		}


		// --- Event Listeners for BOTH UIs ---
		if (aiModalSendBtn) {
		    aiModalSendBtn.addEventListener('click', function() {
		        var userQuery = aiModalInput.value.trim();
		        if (userQuery) {
		            aiModalInput.value = '';
		            handleAiQuery(userQuery);
		        }
		    });
		}
		if (aiModalInput) {
		    aiModalInput.addEventListener('keydown', function(e) {
		        if (e.key === 'Enter' || e.keyCode === 13) {
		            e.preventDefault();
		            var userQuery = aiModalInput.value.trim();
		            if (userQuery) {
		                aiModalInput.value = '';
		                handleAiQuery(userQuery);
		            }
		        }
		    });
		}

		if (aiChatSendBtn) {
		    aiChatSendBtn.addEventListener('click', function() {
		        var userQuery = aiChatInput.value.trim();
		        if (userQuery) {
		            aiChatInput.value = '';
		            handleAiQuery(userQuery);
		        }
		    });
		}
		if (aiChatInput) {
		    aiChatInput.addEventListener('keydown', function(e) {
		        if (e.key === 'Enter' || e.keyCode === 13) {
		            e.preventDefault();
		            var userQuery = aiChatInput.value.trim();
		            if (userQuery) {
		                aiChatInput.value = '';
		                handleAiQuery(userQuery);
		            }
		        }
		    });
		}


		// --- Initial Setup on Page Load ---
		if (document.querySelector('#dashboardNavLink')) {
		    document.querySelector('#dashboardNavLink').classList.add('active-nav');
		}
		showOnly(mainSections.dashboard);
		applyFabPreference();
		displayMessages();

		// --- THE FIX: Dynamically add UI elements and their listeners ---
		(function setupAiExtras() {
		    // Add Dustbin icon to Modal
		    var modalHeaderActions = document.querySelector('.ai-modal-header-actions');
		    if (modalHeaderActions && !modalHeaderActions.querySelector('.ai-modal-clear-btn')) {
		        var clearBtn = document.createElement('button');
		        clearBtn.className = 'ai-modal-clear-btn';
		        clearBtn.title = 'Clear Chat';
		        clearBtn.innerHTML = '<i class="bx bx-trash"></i>';
		        clearBtn.addEventListener('click', clearChatHistory);
		        // Add it before the close button
		        modalHeaderActions.insertBefore(clearBtn, modalHeaderActions.firstChild);
		    }

		    // Add Floating Clear Chat button for Main Section
		    var aiSection = document.getElementById('aiAssistantSection');
		    if (aiSection && !document.getElementById('ai-section-clear-btn')) {
		        var floatingClearBtn = document.createElement('button');
		        floatingClearBtn.id = 'ai-section-clear-btn';
		        floatingClearBtn.className = 'ai-section-clear-chat-btn';
		        floatingClearBtn.innerHTML = '<i class="bx bx-trash"></i> Clear Chat';
		        floatingClearBtn.addEventListener('click', clearChatHistory);
		        aiSection.appendChild(floatingClearBtn);

		        // Add listener to the settings button to toggle it
		        if (aiSectionSettingsBtn) {
		            aiSectionSettingsBtn.addEventListener('click', function() {
		                floatingClearBtn.classList.toggle('visible');
		            });
		        }
		    }

		    // Add disclaimers
		    var disclaimerText = 'Powered by Gemini 2.5 Flash. Responses may be inaccurate.';
		    var mainDisclaimer = document.createElement('p');
		    mainDisclaimer.className = 'ai-disclaimer';
		    mainDisclaimer.textContent = disclaimerText;
		    
		    var modalDisclaimer = document.createElement('p');
		    modalDisclaimer.className = 'ai-disclaimer';
		    modalDisclaimer.textContent = disclaimerText;

		    var mainChatArea = document.querySelector('.ai-chat-input-area');
		    if (mainChatArea && !mainChatArea.nextElementSibling) {
		        mainChatArea.parentNode.insertBefore(mainDisclaimer, mainChatArea.nextSibling);
		    }
		    
		    var modalPanel = document.querySelector('.ai-search-panel');
		    if (modalPanel && !modalPanel.querySelector('.ai-disclaimer')) {
		        modalPanel.appendChild(modalDisclaimer);
		    }
		})();
});
// Ensure window.allProgramsData and window.assignedProgramIdsData are available from JSP
// This script assumes they are populated by the JSP like so:
// window.allProgramsData = JSON.parse('<c:out value="${requestScope.allProgramsJson}" escapeXml="false" default="[]" />');
// window.assignedProgramIdsData = JSON.parse('<c:out value="${requestScope.assignedProgramIdsJson}" escapeXml="false" default="[]" />');


/* ───────── HELPER FUNCTIONS ───────── */

// General utility for querying DOM elements
function qs(selector) { return document.querySelector(selector); }
function qsa(selector) { return document.querySelectorAll(selector); }

// General utility for showing/hiding elements with 'display: block'
function show(el, display) {
    if (el) el.style.display = display ? 'block' : 'none';
}

// General utility for showing/hiding elements with 'display: flex'
function showFlex(el, display) {
    if (el) el.style.display = display ? 'flex' : 'none';
}

// General utility for enabling/disabling select elements and resetting their value
function disable(sel, yes) {
    if (sel) {
        sel.disabled = yes;
        if (yes) sel.selectedIndex = 0; // Reset selection to default when disabled
    }
}

// **MODIFIED fillSelect function**
// This function now expects 'dataArray' to be an array of objects,
// where each object has a 'value' property, a 'text' property,
// and an optional 'disabled' boolean property.
function fillSelect(selectElement, dataArray, defaultOptionText, valueKey = 'value', textKey = 'text') {
    if (!selectElement) return;
    selectElement.innerHTML = `<option value="" selected disabled>${defaultOptionText}</option>`; // Always include a disabled placeholder

    dataArray.forEach(function(item) {
        var option = document.createElement('option');
        option.value = item[valueKey];
        option.textContent = item[textKey];
        // Apply disabled property if present in the item object
        if (item.disabled) {
            option.disabled = true;
            option.style.color = '#999'; // Grey out disabled options
            option.style.fontStyle = 'italic';
        }
        selectElement.appendChild(option);
    });
}

// Helper debounce function
function debounce(func, delay) {
    let timeout;
    return function(...args) {
        const context = this;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), delay);
    };
}

// **MODIFIED populateAssignedProgramDropdown function**
// This function populates a dropdown with ALL programs, but disables
// those the faculty is NOT assigned to.
function populateAssignedProgramDropdown(dropdownId, defaultText = '-- choose program --') {
    var dropdown = document.getElementById(dropdownId);
    if (!dropdown) {
        console.warn(`Dropdown element with ID '${dropdownId}' not found.`);
        return;
    }
    if (!window.allProgramsData || !Array.isArray(window.allProgramsData) ||
        !window.assignedProgramIdsData || !Array.isArray(window.assignedProgramIdsData)) {
        console.warn('Program data or assigned program IDs not available from JSP.');
        dropdown.innerHTML = '<option value="">Error loading programs</option>';
        return;
    }

    // Convert assignedProgramIdsData array to a Set for faster lookup
    const assignedIdsSet = new Set(window.assignedProgramIdsData);

    // Prepare options for fillSelect, marking unassigned ones as disabled
    const options = window.allProgramsData.map(program => {
        const isAssigned = assignedIdsSet.has(program.programId);
        return {
            value: program.programId,
            text: program.programName + (isAssigned ? '' : ' (Not Assigned)'),
            disabled: !isAssigned
        };
    });

    fillSelect(dropdown, options, defaultText);
}

// Helper function to populate program dropdowns with ALL programs (no filtering/disabling)
function populateAllProgramDropdown(selectElementId, defaultOptionText = "All Programs") {
    const selectElement = document.getElementById(selectElementId);
    if (!selectElement) {
        console.warn(`Dropdown element with ID '${selectElementId}' not found.`);
        return;
    }

    // Clear existing options, but keep the default "All Programs" option
    selectElement.innerHTML = `<option value="">${defaultOptionText}</option>`;

    if (window.allProgramsData && Array.isArray(window.allProgramsData)) {
        window.allProgramsData.forEach(program => {
            const option = document.createElement('option');
            option.value = program.programId;
            option.textContent = program.programName;
            selectElement.appendChild(option);
        });
    } else {
        console.warn("All program data not available from JSP.");
    }
}


/* =====================================================
   ==========  NAVIGATION & UI LOGIC  ==================
   ===================================================== */
document.addEventListener('DOMContentLoaded', function () {

    var sidebar = qs('nav.sidebar');
    var toggleBtn = qs('.toggle');

    var dashSection = document.getElementById('dashboardSection');
    var studentSection = document.getElementById('studentFeatures');
    var attendanceSection = document.getElementById('attendanceManagementSection');
    var marksSection = document.getElementById('marksManagementSection'); // Reference to the Marks section

    var dashboardLink = document.getElementById('dashboardNavLink');
    var manageLink = document.getElementById('manageStudentsLink');
    var attendanceLink = document.getElementById('attendanceNavLink');
    var enterMarksLink = document.getElementById('enterMarksNavLink'); // Reference to the Marks nav link

    var topPanels = [dashSection, studentSection, attendanceSection, marksSection];

    function showOnly(sec) {
        // First, close all sidebar dropdowns
        Array.prototype.forEach.call(qsa('.has-dropdown'), function(dd) {
            dd.classList.remove('active');
        });

        topPanels.forEach(function(p) {
            if (p) {
                if (p === sec) {
                    p.style.display = 'block'; // Show the selected main section
                    // Trigger specific load/population functions when a section becomes active
                    if (p === studentSection) {
                        populateAssignedProgramDropdown('programIdAdd'); // For Add Student form
                        populateAllProgramDropdown('programFilterDropdown'); // For View Student filter
                        showStudentFeature(''); // Show initial feature cards for students
                    } else if (p === attendanceSection) {
                        populateAssignedProgramDropdown('programSelect'); // For Take Attendance form
                        populateAttendanceViewFilters(); // For View Attendance filters
                        showAttendanceAction(''); // Show initial feature cards for attendance
                    } else if (p === marksSection) {
                        // Correctly populate marks filters and show initial cards
                        populateMarksViewFilters(); // Populates program, semester, exam type filters
                        showMarksAction(''); // Show initial feature cards for marks
                    }
                } else {
                    p.style.display = 'none'; // Hide other main sections
                    // Reset logic for sections when they are hidden
                    if (p === studentSection) {
                        showFlex(qs('#studentFeatures .feature-card-wrap'), true);
                        show(document.getElementById('addStudentCard'), false);
                        show(document.getElementById('viewStudentSection'), false);
                        show(document.getElementById('todoPlaceholder'), false);
                        if (document.getElementById('addStudentForm')) document.getElementById('addStudentForm').reset();
                        if (document.getElementById('programIdAdd')) {
                             document.getElementById('programIdAdd').value = "";
                        }
                        if (document.getElementById('studentFields')) show(document.getElementById('studentFields'), false);
                        if (document.getElementById('addStudentMessage')) show(document.getElementById('addStudentMessage'), false);
                    } else if (p === attendanceSection) {
                        showFlex(qs('#attendanceFeatureCards'), true);
                        show(qs('#takeAttendanceSection'), false);
                        show(qs('#viewAttendanceSection'), false);
                        // Ensure progSel, semSel, subjSel are defined before using
                        if (progSel) progSel.value = "";
                        disable(semSel, true);
                        disable(subjSel, true);

                        var studentsWrap = qs('#studentsWrap'); // Ensure this element is correctly identified if it exists
                        var formContainer = qs('#takeAttendanceSection');
                        if (formContainer) {
                            var formParts = formContainer.querySelectorAll('.form-group, button');
                            formParts.forEach(function(el) {
                                // FIX: Add null check for studentsWrap before calling .contains()
                                if (el.id !== 'studentsWrap' && (studentsWrap && !studentsWrap.contains(el))) {
                                    show(el, true);
                                }
                            });
                        }
                    } else if (p === marksSection) {
                        // Reset marks entry section when hiding
						showFlex(qs('#marksFeatureCards'), true); // Show feature cards
						// Assuming these sections exist in your HTML for marks entry/view
						show(qs('#enterMarksCourseSection'), false); // Hide course-wise entry
						show(qs('#enterMarksStudentSection'), false); // Hide student-wise entry
						show(qs('#viewMarksRecordsContainer'), false); // Hide view records container

						// Reset filter dropdowns for marks section
						if (qs('#viewMarksProgramFilter')) qs('#viewMarksProgramFilter').value = '';
						if (qs('#viewMarksSemesterFilter')) qs('#viewMarksSemesterFilter').value = '';
						if (qs('#viewMarksSubjectFilter')) qs('#viewMarksSubjectFilter').value = '';
						if (qs('#viewMarksExamTypeFilter')) qs('#viewMarksExamTypeFilter').value = '';
						if (qs('#viewMarksStudentSearchInput')) qs('#viewMarksStudentSearchInput').value = '';

						disable(qs('#viewMarksSubjectFilter'), true); // Disable subject filter
						if (qs('#marksRecordsTableBody')) qs('#marksRecordsTableBody').innerHTML = ''; // Clear table
						if (qs('#marksEntryMessage')) show(qs('#marksEntryMessage'), false);
						if (qs('#marksEntryTableContainer')) show(qs('#marksEntryTableContainer'), false);
					}
				}
			}
		});
        // On mobile, ensure sidebar closes after any main section is selected
        if (window.innerWidth <= 768) {
            sidebar.classList.add('close');
            sidebar.classList.remove('open');
        }
    }


    /* — toggle sidebar open/close — */
    if (toggleBtn) {
        toggleBtn.addEventListener('click', function() {
            sidebar.classList.toggle('close');
            if (window.innerWidth <= 768) {
                if (!sidebar.classList.contains('close')) {
                    sidebar.classList.add('open');
                } else {
                    sidebar.classList.remove('open');
                }
            }
        });
    }

    /* — dropdown (<li class="has-dropdown">) handling — */
    Array.prototype.forEach.call(
        qsa('.has-dropdown > a'),
        function(a) {
            a.addEventListener('click', function(e) {
                e.preventDefault();
                var li = a.closest('.has-dropdown');
                // Close other dropdowns
                Array.prototype.forEach.call(
                    qsa('.has-dropdown'),
                    function(x) { if (x !== li) x.classList.remove('active'); }
                );
                // Toggle the clicked dropdown's active state
                li.classList.toggle('active');
            });
        }
    );
    // NEW: Mobile sidebar collapse on sub-feature-card click
    const studentFeatureCards = qsa('#studentFeatures .feature-card-wrap .feature-card');
    const attendanceFeatureCards = qsa('#attendanceManagementSection .feature-card-wrap .feature-card');
    // Ensure marksFeatureCards is defined correctly as per its ID in JSP
    const marksFeatureCardsElements = qsa('#marksFeatureCards .feature-card'); // Corrected selector

	[...studentFeatureCards, ...attendanceFeatureCards, ...marksFeatureCardsElements].forEach(card => {
	    card.addEventListener('click', function() {
	        if (window.innerWidth <= 768) {
	            sidebar.classList.add('close');
	            sidebar.classList.remove('open');
	        }
	    });
	});

    // NEW: Click outside to close dropdown in collapsed view
    document.addEventListener('click', function(e) {
        if (sidebar.classList.contains('close')) {
            var activeDropdown = qs('.has-dropdown.active');
            if (activeDropdown) {
                var isClickInsideSidebar = sidebar.contains(e.target);
                var isClickInsideDropdown = activeDropdown.contains(e.target);
                var isDropdownToggler = e.target.closest('.has-dropdown > a');

                if (!isClickInsideSidebar && !isDropdownToggler) {
                    activeDropdown.classList.remove('active');
                }
            }
        }
    });


    /* — dashboard / students / attendance / marks main nav — */

    if (dashboardLink) {
        dashboardLink.addEventListener('click', function(e) {
            e.preventDefault();
            showOnly(dashSection);
        });
    }
    if (manageLink) {
        manageLink.addEventListener('click', function(e) {
            e.preventDefault();
            showOnly(studentSection);
            if (window.innerWidth <= 768) {
                sidebar.classList.add('close');
                sidebar.classList.remove('open');
            }
        });
    }

    if (attendanceLink) {
        attendanceLink.addEventListener('click', function(e) {
            e.preventDefault();
            showOnly(attendanceSection);
            if (window.innerWidth <= 768) {
                sidebar.classList.add('close');
                sidebar.classList.remove('open');
            }
        });
    }

    if (enterMarksLink) {
        enterMarksLink.addEventListener('click', function(e) {
            e.preventDefault();
            showOnly(marksSection);
            if (window.innerWidth <= 768) {
                sidebar.classList.add('close');
                sidebar.classList.remove('open');
            }
        });
    }

	const profileNavLink = qs('#profileNavLink'); // Get the navigation link by its new ID
	    const profileSection = qs('#profileSection'); // Get the profile content section
	    const profileFacultyId = qs('#profileFacultyId');
	    const profileFacultyName = qs('#profileFacultyName');
	    const profileFacultyEmail = qs('#profileFacultyEmail');

	    // Make sure 'profileSection' is included in your 'topPanels' array
	    // (This should already be done if you used the previous full code, but double-check)
	    // var topPanels = [dashSection, studentSection, attendanceSection, marksSection, profileSection];


	// Add this event listener block along with other navigation link handlers:
	// For example, after the 'if (messagesLink) { ... }' block.

	    if (profileNavLink) {
	        profileNavLink.addEventListener('click', function(e) {
	            e.preventDefault(); // Prevent default link behavior (page reload)
	            showOnly(profileSection); // Call the showOnly function to display the profile section

	            // Populate profile details from window variables (set in JSP)
	            if (profileFacultyId && window.currentFacultyId) {
	                profileFacultyId.textContent = window.currentFacultyId;
	            }
	            if (profileFacultyName && window.currentFacultyName) {
	                profileFacultyName.textContent = window.currentFacultyName;
	            }
	            if (profileFacultyEmail && window.currentFacultyEmail) {
	                profileFacultyEmail.textContent = window.currentFacultyEmail;
	            }

	            // Close sidebar on mobile after clicking a navigation link
	            if (window.innerWidth <= 768) {
	                sidebar.classList.add('close');
	                sidebar.classList.remove('open');
	                sidebarOverlay.style.display = 'none'; // Hide overlay if you have one
	            }
	        });
	    }
    /* ───────── DARK‑MODE TOGGLE ───────── */
    var modeSwitch = qs('.toggle-switch');
    var modeText = qs('.mode-text');
    if (modeSwitch && modeText) {
        modeSwitch.addEventListener('click', function() {
            document.body.classList.toggle('dark');
            modeText.textContent = document.body.classList.contains('dark') ? 'Light mode' : 'Dark mode';
        });
    }


    /* =====================================================
       ==========  STUDENT‑MANAGEMENT  CODE  ==============
       ===================================================== */

    var studentSearchInput = document.getElementById('studentSearchInput');
    var programFilterDropdown = document.getElementById('programFilterDropdown');
    var resetFiltersBtn = document.getElementById('resetFiltersBtn');
    var studentsTableBody = document.getElementById('studentsTableBody');
    var sortableHeaders = qsa('#studentsTable th[data-sort-col]');

    window.showStudentFeature = function(which) {
        var cards = qs('#studentFeatures .feature-card-wrap');
        var addCard = document.getElementById('addStudentCard');
        var viewSec = document.getElementById('viewStudentSection');
        var todo = document.getElementById('todoPlaceholder');

        [addCard, viewSec, todo].forEach(function(el) { show(el, false); });
        showFlex(cards, false);

        switch (which) {
            case 'add':
                show(addCard, true);
                break;
            case 'view':
            case 'edit':
            case 'delete':
            case 'search':
                show(viewSec, true);
                loadStudentData(); // Reload student data when entering view mode
                break;
            default:
                showFlex(cards, true);
        }

        var backToFeaturesBtn = document.getElementById('backToFeaturesBtn');
        if (backToFeaturesBtn) {
            backToFeaturesBtn.onclick = function() {
                showStudentFeature('');
            };
        }
    };

    /* ----------  Add‑Student Form ---------- */
    var programIdAdd = document.getElementById('programIdAdd');
    var fieldsBlock = document.getElementById('studentFields');
    if (programIdAdd) {
        programIdAdd.addEventListener('change', function() {
            if (fieldsBlock) fieldsBlock.style.display = programIdAdd.value ? 'flex' : 'none';
        });
    }

    var cancelBtn = document.getElementById('cancelAddBtn');
    var addStudentForm = document.getElementById('addStudentForm');
    var messageContainer = document.getElementById('addStudentMessage');

    if (cancelBtn) {
        cancelBtn.addEventListener('click', function() {
            addStudentForm.reset();
            if (programIdAdd) programIdAdd.value = "";
            show(fieldsBlock, false);
            show(messageContainer, false);
            showStudentFeature('');
        });
    }

    function validateStudentForm() {
        var sid = document.getElementById('studentId').value.trim();
        var sem = document.getElementById('semester').value.trim();
        var programId = programIdAdd.value;

        if (!programId) { alert('Please select a Program.'); return false; }
        if (!/^\d+$/.test(sid)) { alert('Student ID must be numeric'); return false; }
        if (!/^\d+$/.test(sem) || sem < 1 || sem > 6) { alert('Semester must be 1‑6'); return false; }
        return true;
    }

	if (addStudentForm) {
	        addStudentForm.addEventListener('submit', function(e) {
	            e.preventDefault();
	            if (!validateStudentForm()) return;

	            if (messageContainer) {
	                messageContainer.innerHTML =
	                    '<div class="loading-message"><div class="spinner"></div> Saving student…</div>';
	                show(messageContainer, true);
	            }
	            var start = Date.now();

	            var formData = new FormData(addStudentForm);
	            formData.set('programId', parseInt(formData.get('programId')));

	            fetch('AddStudServlet', {
	                    method: 'POST',
	                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	                    body: new URLSearchParams(formData)
	                })
	                .then(function(r) {
	                    // --- MODIFIED: Parse response as JSON ---
	                    if (!r.ok) {
	                        // If response is not OK, try to parse JSON error or fall back to text
	                        return r.json().catch(() => r.text().then(text => ({ status: 'error', message: text || 'Unknown error' })));
	                    }
	                    return r.json(); // Expect JSON response
	                })
	                .catch(function(error) {
	                    console.error("Fetch error in AddStudServlet submission:", error);
	                    return { status: 'error', message: 'Network / server error. Try again.' }; // Return structured error
	                })
	                .then(function(responseJson) { // Renamed 'txt' to 'responseJson' for clarity
	                    var wait = Math.max(0, 1500 - (Date.now() - start));

	                    setTimeout(function() {
	                        // --- MODIFIED: Access properties from the parsed JSON object ---
	                        var ok = responseJson.status === 'success';
	                        var msg = responseJson.message || 'Operation completed.'; // Use message from JSON

	                        var col = ok ? '#008000' : '#d8000c';
	                        if (messageContainer) {
	                            messageContainer.innerHTML =
	                                '<div style="display:flex;align-items:center;gap:12px;font-weight:bold;' +
	                                'padding:10px;border-radius:6px;border:1px solid ' + col + ';' +
	                                'background:' + (ok ? '#e7f9ed' : '#ffe6e6') + ';color:' + col + '">' +
	                                '<span style="font-size:24px">' + (ok ? '✅' : '❌') + '</span>' + msg + // Use 'msg'
	                                '</div>';

	                            if (ok) { // This block only executes on SUCCESS
	                                addStudentForm.reset();
	                                if (programIdAdd) programIdAdd.value = ""; // Set to empty for placeholder
	                                show(fieldsBlock, false);
	                                loadStudentData(); // Load data immediately on success
	                            }

	                            // NEW/MODIFIED: This setTimeout now applies to ALL messages (success or error)
	                            // It will hide the messageContainer after 3 seconds.
	                            setTimeout(function() {
	                                show(messageContainer, false);
	                            }, 3000); // MESSAGE DISAPPEARS AFTER 3 SECONDS FOR BOTH SUCCESS AND ERROR

	                        }
	                    }, wait);
	                });
	        });
	    }

    /* ----------  Table load / filter / sort  ---------- */

	/* ----------  Table load / filter / sort  ---------- */
	   function loadStudentData() {
	       if (studentSearchInput) studentSearchInput.value = '';
	       if (programFilterDropdown) programFilterDropdown.value = "";

	       if (studentsTableBody) {
	           studentsTableBody.innerHTML =
	               '<tr><td colspan="7" style="text-align:center;padding:20px;">Loading…</td></tr>';
	       }

	       fetch('GetStudentsServlet', {
	               method: 'POST',
	               headers: { 'Content-Type': 'application/json' },
	               body: '{}'
	           })
	           .then(function(r) {
	               if (!r.ok) throw new Error('HTTP ' + r.status);
	               return r.json();
	           })
	           .then(function(responseJson) { // Changed 'data' to 'responseJson' for clarity
	               if (!studentsTableBody) return;

	               // FIX: Access the 'students' array from the response JSON
	               const students = responseJson.students; 

	               studentsTableBody.innerHTML = students.length ? '' :
	                   '<tr><td colspan="7" style="text-align:center;padding:20px;">No students found.</td></tr>';
	               
	               students.forEach(function(s) { // Iterate over the 'students' array
	                   var tr = document.createElement('tr');
	                   tr.setAttribute('data-program-id', s.programId);
	                   tr.innerHTML =
	                       '<td>' + s.studentId + '</td><td>' + s.fullName + '</td><td>' + s.programName + '</td>' +
	                       '<td>' + s.semester + '</td><td>' + s.email + '</td><td>' + s.phone + '</td>' +
	                       '<td><button class="btn-action-edit" data-id="' + s.studentId + '">Edit</button>' +
	                       '<button class="btn-action-delete" data-id="' + s.studentId + '">Delete</button></td>';
	                   var del = tr.querySelector('.btn-action-delete');
	                   del.addEventListener('click', function() {
	                       var id = this.dataset.id;
	                       if (!confirm('Delete student ' + id + '?')) return;
	                       fetch('DeleteStudentServlet', {
	                               method: 'POST',
	                               headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	                               body: 'studentId=' + encodeURIComponent(id)
	                           })
	                           .then(function(r) { return r.json(); })
	                           .then(function(res) {
	                               if (res.status === 'success') { alert(res.message); tr.remove(); applyTableFilters(); } else alert(res.message || 'Delete failed');
	                           }).catch(function() { alert('Network error'); });
	                   });
	                   studentsTableBody.appendChild(tr);
	               });
	               applyTableFilters();
	               sortTable(0, 'asc');
	           })
	           .catch(function(error) { // Catch the error object
	               console.error("Error loading student data:", error); // Log the actual error
	               if (studentsTableBody) {
	                   studentsTableBody.innerHTML =
	                       '<tr><td colspan="7" style="text-align:center;padding:20px;color:red">Failed to load students.</td></tr>';
	               }
	           });
	   }
    function applyTableFilters() {
        var q = (studentSearchInput ? studentSearchInput.value : '').toLowerCase().trim();
        var pId = (programFilterDropdown ? programFilterDropdown.value : '');
        var visible = 0;
        Array.prototype.forEach.call(studentsTableBody.querySelectorAll('tr'), function(row) {
            var tds = row.querySelectorAll('td');
            if (tds.length !== 7) { row.style.display = 'none'; return; }
            var matchSearch = !q || [0, 1, 4, 5].some(function(i) {
                return tds[i].textContent.toLowerCase().indexOf(q) !== -1;
            });
            var rowProgramId = row.getAttribute('data-program-id');
            var matchProgram = !pId || rowProgramId === pId;

            row.style.display = (matchSearch && matchProgram) ? 'table-row' : 'none';
            if (row.style.display === 'table-row') visible++;
        });
        var noRow = document.getElementById('noStudentsFilteredMessage');
        if (!visible) {
            if (!noRow) {
                noRow = document.createElement('tr');
                noRow.id = 'noStudentsFilteredMessage';
                noRow.innerHTML = '<td colspan="7" style="text-align:center;padding:20px;color:grey">No matching students found.</td>';
                studentsTableBody.appendChild(noRow);
            }
            noRow.style.display = 'table-row';
        } else if (noRow) noRow.style.display = 'none';
    }
    if (studentSearchInput) studentSearchInput.addEventListener('input', applyTableFilters);
    if (programFilterDropdown) programFilterDropdown.addEventListener('change', applyTableFilters);
    if (resetFiltersBtn) resetFiltersBtn.addEventListener('click', function() {
        if (studentSearchInput) studentSearchInput.value = '';
        if (programFilterDropdown) programFilterDropdown.value = "";
        applyTableFilters();
    });

    Array.prototype.forEach.call(sortableHeaders, function(th) {
        th.addEventListener('click', function() {
            var col = +th.dataset.sortCol;
            var dir = th.dataset.sortDir = (th.dataset.sortDir === 'asc' ? 'desc' : 'asc');
            Array.prototype.forEach.call(sortableHeaders, function(h) {
                h.classList.remove('sort-asc', 'sort-desc');
                var i = h.querySelector('.sort-icon');
                if (i) { i.classList.remove('fa-sort-up', 'fa-sort-down'); i.classList.add('fa-sort'); }
            });
            th.classList.add('sort-' + dir);
            var ic = th.querySelector('.sort-icon');
            if (ic) { ic.classList.remove('fa-sort'); ic.classList.add(dir === 'asc' ? 'fa-sort-up' : 'fa-sort-down'); }
            sortTable(col, dir);
        });
    });

    function sortTable(col, dir) {
        var rows = Array.prototype.slice.call(
            studentsTableBody.querySelectorAll('tr')).filter(function(r) {
                return r.querySelectorAll('td').length === 7;
            });
        rows.sort(function(a, b) {
            var A = a.querySelectorAll('td')[col].textContent.trim();
            var B = b.querySelectorAll('td')[col].textContent.trim();
            var num = (col === 0 || col === 3);
            var x = num ? +A || 0 : A.toLowerCase();
            var y = num ? +B || 0 : B.toLowerCase();
            return dir === 'asc' ? (x > y ? 1 : x < y ? -1 : 0) : (x < y ? 1 : x > y ? -1 : 0);
        }).forEach(function(r) { studentsTableBody.appendChild(r); });
        applyTableFilters();
    }


    /* =====================================================
       ==========  ATTENDANCE MANAGEMENT CODE  =============
       ===================================================== */
    var progSel = qs('#programSelect');
    var semSel = qs('#semesterSelect');
    var subjSel = qs('#subjectSelect');
    var attTimeIn = qs('#attDateTime');

    var semestersForFillSelect = ['1', '2', '3', '4', '5', '6'].map(s => ({ value: s, text: 'Semester ' + s }));


    if (progSel) progSel.addEventListener('change', function() {
        disable(semSel, false);
        fillSelect(semSel, semestersForFillSelect, '-- choose semester --');
        disable(subjSel, true);
    });

    if (semSel) semSel.addEventListener('change', function() {
        var selectedProgramId = progSel.value;
        var selectedSemester = this.value;
        if (selectedProgramId && selectedSemester) {
            fetchSubjectsForAttendance(selectedProgramId, selectedSemester);
        } else {
            disable(subjSel, true);
        }
    });

    function fetchSubjectsForAttendance(programId, semester) {
        console.log("Fetching subjects for Program ID:", programId, "Semester:", semester);
        fetch('GetCoursesByProgramAndSemesterServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'programId=' + encodeURIComponent(programId) + '&semester=' + encodeURIComponent(semester)
            })
            .then(function(r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(function(data) {
                const formattedSubjects = data.map(s => ({
                    value: s.courseId,
                    text: s.courseName
                }));
                fillSelect(subjSel, formattedSubjects, '-- choose subject --');
                disable(subjSel, false);
            })
            .catch(function(error) {
                console.error("Error fetching subjects:", error);
                alert('Failed to load subjects. Please try again.');
                disable(subjSel, true);
            });
    }

    function tick() { if (attTimeIn) attTimeIn.value = new Date().toLocaleString(); }
    tick();
    setInterval(tick, 1000);

    var attendCards = qs('#attendanceFeatureCards');
    var takeSec = qs('#takeAttendanceSection');
    var viewSec = qs('#viewAttendanceSection');

    window.showAttendanceAction = function(act) {
        [takeSec, viewSec].forEach(function(x) { show(x, false); });
        showFlex(attendCards, false);

        if (act === 'take') {
            show(takeSec, true);
        } else if (act === 'view') {
            show(viewSec, true);
            populateAttendanceViewFilters();
            loadAttendanceViewData();
        } else {
            showFlex(attendCards, true);
        }
    };

    var startAttendanceSessionBtn = qs('#startAttendanceSessionBtn');
    var cancelTakeAttendanceBtn = qs('#cancelTakeAttendanceBtn');

    if (startAttendanceSessionBtn) {
        startAttendanceSessionBtn.addEventListener('click', function() {
            if (!progSel.value || !semSel.value || !subjSel.value) {
                alert('Please choose Program, Semester, and Subject first.');
                return;
            }

            var programNameForDisplay = progSel.options[progSel.selectedIndex].text;
            var semesterTextForDisplay = semSel.value;

            var selectedCourseId = subjSel.value;
            var selectedTopic = subjSel.options[subjSel.selectedIndex].text;
            const facultyId = parseInt(window.currentFacultyId);

            var dateTime = attTimeIn.value;

            if (isNaN(facultyId) || facultyId <= 0) {
                alert('Error: Faculty ID is missing or invalid. Please refresh the page or log in again.');
                console.error('Faculty ID is missing or invalid:', window.currentFacultyId);
                return;
            }

            var confirmMsg = `Confirm Attendance Session Details:\n\n` +
                `Date & Time: ${dateTime}\n` +
                `Program: ${programNameForDisplay}\n` +
                `Semester: ${semesterTextForDisplay}\n` +
                `Subject (Topic): ${selectedTopic}\n\n` +
                `Click OK to start the 15-minute attendance session.`;

            if (confirm(confirmMsg)) {
                startAttendanceSessionBtn.disabled = true;
                if (cancelTakeAttendanceBtn) cancelTakeAttendanceBtn.disabled = true;

                var attendanceData = {
                    courseId: selectedCourseId,
                    topic: selectedTopic,
                    facultyId: facultyId,
                    clientDateTime: dateTime
                };

                fetch('StartAttendanceSessionServlet', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(attendanceData)
                    })
                    .then(function(response) {
                        if (!response.ok) {
                            return response.json().catch(() => response.text().then(text => ({ status: 'error', message: text || 'Unknown error starting session' })))
                                .then(errorData => { throw new Error(errorData.message); });
                        }
                        return response.json();
                    })
                    .then(function(data) {
                        if (data.status === 'success' && data.sessionId) {
                            alert(data.message);
                            window.location.href = 'takeAttendance.jsp?sessionId=' + data.sessionId;
                        } else {
                            alert('Failed to start attendance session: ' + (data.message || 'Unknown error.'));
                            startAttendanceSessionBtn.disabled = false;
                            if (cancelTakeAttendanceBtn) cancelTakeAttendanceBtn.disabled = false;
                        }
                    })
                    .catch(function(error) {
                        console.error('Error starting attendance session:', error);
                        alert('An error occurred while starting the attendance session. Please try again.');
                        startAttendanceSessionBtn.disabled = false;
                        if (cancelTakeAttendanceBtn) cancelTakeAttendanceBtn.disabled = false;
                    });
            }
        });
    }

    if (cancelTakeAttendanceBtn) {
        cancelTakeAttendanceBtn.addEventListener('click', function() {
            progSel.value = "";
            disable(semSel, true);
            disable(subjSel, true);
            showAttendanceAction('');
        });
    }

    var style = document.createElement('style');
    style.textContent =
        '.ios-switch{position:relative;display:inline-block;width:46px;height:26px}' +
        '.ios-switch input{opacity:0;width:0;height:0}' +
        '.slider{position:absolute;cursor:pointer;top:0;left:0;right:0;bottom:0;background:#ccc;transition:.3s;border-radius:26px}' +
        '.slider:before{position:absolute;content:"";height:22px;width:22px;left:2px;bottom:2px;background:#fff;transition:.3s;border-radius:50%}' +
        'input:checked+.slider{background:#4caf50}input:checked+.slider:before{transform:translateX(20px)}';
    document.head.appendChild(style);


    /* =====================================================
       ==========  ATTENDANCE VIEW RECORDS LOGIC  ==========
       ===================================================== */
	   const attProgramFilterDropdown = qs('#attProgramFilter');
	   const attSemesterFilterDropdown = qs('#attSemesterFilter');
	   const attSubjectFilterDropdown = qs('#attSubjectFilter');
	   const attDateFilterInput = qs('#attDateFilter');
	   const attStudentSearchInput = qs('#attStudentSearchInput');
	   const resetAttendanceFiltersBtn = qs('#resetAttendanceFiltersBtn');
	   const attendanceRecordsTableBody = qs('#attendanceRecordsTableBody');

	   function loadAttendanceViewData() {
	       if (attendanceRecordsTableBody) {
	           attendanceRecordsTableBody.innerHTML =
	               '<tr><td colspan="6" style="text-align:center;padding:20px;">Loading attendance records...</td></tr>';
	       }

	       const filters = {
	           programId: attProgramFilterDropdown && attProgramFilterDropdown.value !== "" ? parseInt(attProgramFilterDropdown.value) : -1,
	           semester: attSemesterFilterDropdown && attSemesterFilterDropdown.value !== "" ? parseInt(attSemesterFilterDropdown.value) : -1,
	           subjectId: attSubjectFilterDropdown && attSubjectFilterDropdown.value !== "" ? attSubjectFilterDropdown.value : null,
	           date: attDateFilterInput ? attDateFilterInput.value : null,
	           studentSearch: attStudentSearchInput ? attStudentSearchInput.value.trim() : null
	       };

	       fetch('GetAttendanceRecordsServlet', {
	               method: 'POST',
	               headers: { 'Content-Type': 'application/json' },
	               body: JSON.stringify(filters)
	           })
	           .then(r => {
	               if (!r.ok) throw new Error('HTTP status ' + r.status);
	               return r.json();
	           })
	           .then(data => {
	               if (!attendanceRecordsTableBody) return;

	               attendanceRecordsTableBody.innerHTML = '';

	               // FIX: Access the 'records' array from the data object
	               const records = data.records; 

	               if (!records || records.length === 0) { // Check if records array exists and is empty
	                   attendanceRecordsTableBody.innerHTML =
	                       '<tr><td colspan="6" style="text-align:center;padding:20px;">No attendance records found matching criteria.</td></tr>';
	                   return;
	               }

	               records.forEach(record => { // Iterate over the 'records' array
	                   const tr = document.createElement('tr');
	                   tr.innerHTML = `
	                       <td>${record.studentId}</td>
	                       <td>${record.studentName}</td>
	                       <td>${new Date(record.attendanceDate).toLocaleDateString()}</td>
	                       <td>${record.subjectName}</td>
	                       <td>${record.attendanceStatus === 'PRESENT' ? '<span class="status-present">Present</span>' : '<span class="status-absent">Absent</span>'}</td>
	                       <td>
	                           <button class="btn-action-edit" data-record-id="${record.recordId}">Edit</button>
	                           <button class="btn-action-delete" data-record-id="${record.recordId}">Delete</button>
	                       </td>
	                   `;
	                   attendanceRecordsTableBody.appendChild(tr);
	               });
	           })
	           .catch(error => {
	               console.error("Error loading attendance data:", error);
	               if (attendanceRecordsTableBody) {
	                   attendanceRecordsTableBody.innerHTML =
	                       '<tr><td colspan="6" style="text-align:center;padding:20px;color:red;">Failed to load attendance records. ' + error.message + '</td></tr>';
	               }
	           });
	   }

	   // Populate Filters for "View Attendance" - Programs will be ALL, Semesters are static
	   function populateAttendanceViewFilters() {
	       populateAllProgramDropdown('attProgramFilter', 'All Programs'); // Populate with ALL programs

	       if (attSemesterFilterDropdown) {
	           attSemesterFilterDropdown.innerHTML = '<option value="">All Semesters</option>';
	           for (let i = 1; i <= 8; i++) { // Assuming up to 8 semesters
	               const option = document.createElement('option');
	               option.value = i;
	               option.textContent = 'Semester ' + i;
	               attSemesterFilterDropdown.appendChild(option);
	           }
	       }
	   }

	   function populateSubjectsForFilter() {
	       const selectedProgramId = attProgramFilterDropdown.value;
	       const selectedSemester = attSemesterFilterDropdown.value;

	       if (!selectedProgramId || !selectedSemester) {
	           disable(attSubjectFilterDropdown, true);
	           fillSelect(attSubjectFilterDropdown, [], 'All Subjects');
	           return;
	       }

	       disable(attSubjectFilterDropdown, false);
	       attSubjectFilterDropdown.innerHTML = '<option value="">Loading Subjects...</option>';

	       fetch('GetCoursesByProgramAndSemesterServlet', {
	           method: 'POST',
	           headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	           body: 'programId=' + encodeURIComponent(selectedProgramId) +
	                 '&semester=' + encodeURIComponent(selectedSemester)
	       })
	       .then(r => {
	           if (!r.ok) throw new Error('HTTP status ' + r.status + ': ' + r.statusText);
	           return r.json();
	       })
	       .then(subjects => {
	           if (attSubjectFilterDropdown) {
	               const formattedSubjects = subjects.map(s => ({
	                   value: s.courseId,
	                   text: s.courseName
	               }));
	               fillSelect(attSubjectFilterDropdown, formattedSubjects, 'All Subjects');
	           }
	       })
	       .catch(error => {
	           console.error('Error loading subjects for filter:', error);
	           if (attSubjectFilterDropdown) {
	               attSubjectFilterDropdown.innerHTML = '<option value="">Error loading subjects</option>';
	               disable(attSubjectFilterDropdown, true);
	           }
	       })
	       .finally(() => {
	           // loadAttendanceViewData(); // REMOVED: No longer needed here as it's called on program/semester change
	       });
	   }

	   if (attProgramFilterDropdown) {
	       attProgramFilterDropdown.addEventListener('change', function() {
	           loadAttendanceViewData(); // Load data immediately when program changes
	           populateSubjectsForFilter(); // Then populate subjects based on the new program
	       });
	   }
	   if (attSemesterFilterDropdown) {
	       attSemesterFilterDropdown.addEventListener('change', populateSubjectsForFilter);
	       attSemesterFilterDropdown.addEventListener('change', loadAttendanceViewData); // Load data when semester changes
	   }
	   if (attSubjectFilterDropdown) {
	       attSubjectFilterDropdown.addEventListener('change', loadAttendanceViewData);
	   }
	   if (attDateFilterInput) attDateFilterInput.addEventListener('change', loadAttendanceViewData);
	   if (attStudentSearchInput) {
	       attStudentSearchInput.addEventListener('input', debounce(loadAttendanceViewData, 500));
	   }
	   if (resetAttendanceFiltersBtn) {
	       resetAttendanceFiltersBtn.addEventListener('click', function() {
	           if (attProgramFilterDropdown) attProgramFilterDropdown.value = '';
	           if (attSemesterFilterDropdown) attSemesterFilterDropdown.value = '';
	           if (attSubjectFilterDropdown) attSubjectFilterDropdown.value = '';
	           if (attDateFilterInput) attDateFilterInput.value = '';
	           if (attStudentSearchInput) attStudentSearchInput.value = '';
	           disable(attSubjectFilterDropdown, true);
	           loadAttendanceViewData();
	       });
	   }

    /* =====================================================
       ==========  MARKS ENTRY & VIEW CODE  ================
       ===================================================== */
	   const enterMarksStudentSection = qs('#enterMarksStudentSection');
	   // Corrected variable name to match HTML ID
	   var marksFeatureCards = qs('#marksFeatureCards'); // Changed from #marksEntrySection .feature-card-wrap


	   const viewMarksProgramFilter = qs('#viewMarksProgramFilter');
	   const viewMarksSemesterFilter = qs('#viewMarksSemesterFilter');
	   const viewMarksSubjectFilter = qs('#viewMarksSubjectFilter');
	   const viewMarksExamTypeFilter = qs('#viewMarksExamTypeFilter');
	   const viewMarksStudentSearchInput = qs('#viewMarksStudentSearchInput');
	   const resetViewMarksFiltersBtn = qs('#resetViewMarksFiltersBtn');
	   const marksRecordsTableBody = qs('#marksRecordsTableBody');
	   const viewMarksRecordsContainer = qs('#viewMarksRecordsContainer');


	   window.showMarksAction = function(action) {
	       // Ensure marksFeatureCards is properly referenced here
	       showFlex(marksFeatureCards, false); // Hide the initial feature cards
	       // Assuming these sections exist in your HTML for marks entry/view
	       show(qs('#enterMarksCourseSection'), false); // Hide course-wise entry (if it exists)
	       show(enterMarksStudentSection, false); // Hide student-wise entry form
	       show(viewMarksRecordsContainer, false); // Hide view records container

	       if (action !== 'view') {
	           if (viewMarksProgramFilter) viewMarksProgramFilter.value = '';
	           if (viewMarksSemesterFilter) viewMarksSemesterFilter.value = '';
	           if (viewMarksSubjectFilter) viewMarksSubjectFilter.value = '';
	           if (viewMarksExamTypeFilter) viewMarksExamTypeFilter.value = '';
	           if (viewMarksStudentSearchInput) viewMarksStudentSearchInput.value = '';
	           if (marksRecordsTableBody) marksRecordsTableBody.innerHTML = '';
	           disable(viewMarksSubjectFilter, true);
	       }

	       if (action === 'student') {
	           show(enterMarksStudentSection, true);
	       } else if (action === 'view') {
	           show(viewMarksRecordsContainer, true);
	           populateMarksViewFilters();
	           loadMarksViewData();
	       } else {
	           // This is the crucial part: show the feature cards when no specific action is selected
	           showFlex(marksFeatureCards, true);
	       }
	   };

	   const marksSemesters = ['1', '2', '3', '4', '5', '6', '7', '8'].map(s => ({ value: s, text: 'Semester ' + s }));
	   const examTypes = [
	       { value: 'Midterm', text: 'Midterm' },
	       { value: 'Final', text: 'Final' },
	       { value: 'Assignment', text: 'Assignment' },
	       { value: 'Quiz', text: 'Quiz' }
	   ];

	   function populateMarksViewFilters() {
	       populateAllProgramDropdown('viewMarksProgramFilter', 'All Programs');

	       if (viewMarksSemesterFilter) {
	           fillSelect(viewMarksSemesterFilter, marksSemesters, 'All Semesters');
	       }

	       if (viewMarksExamTypeFilter) {
	           fillSelect(viewMarksExamTypeFilter, examTypes, 'All Exam Types');
	       }
           disable(viewMarksSubjectFilter, true);
	   }

	   function populateSubjectsForViewMarksFilter() {
	       const selectedProgramId = viewMarksProgramFilter.value;
	       const selectedSemester = viewMarksSemesterFilter.value;

	       if (!selectedProgramId || !selectedSemester) {
	           disable(viewMarksSubjectFilter, true);
	           fillSelect(viewMarksSubjectFilter, [], 'All Subjects');
	           loadMarksViewData();
	           return;
	       }

	       disable(viewMarksSubjectFilter, false);
	       viewMarksSubjectFilter.innerHTML = '<option value="">Loading Subjects...</option>';

	       fetch('GetCoursesByProgramAndSemesterServlet', {
	           method: 'POST',
	           headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	           body: 'programId=' + encodeURIComponent(selectedProgramId) +
	                 '&semester=' + encodeURIComponent(selectedSemester)
	       })
	       .then(r => {
	           if (!r.ok) throw new Error('HTTP status ' + r.status + ': ' + r.statusText);
	           return r.json();
	       })
	       .then(subjects => {
	           if (viewMarksSubjectFilter) {
	               const formattedSubjects = subjects.map(s => ({ value: s.courseId, text: s.courseName }));
	               fillSelect(viewMarksSubjectFilter, formattedSubjects, 'All Subjects');
	           }
	       })
	       .catch(error => {
	           console.error('Error loading subjects for view marks filter:', error);
	           if (viewMarksSubjectFilter) {
	               viewMarksSubjectFilter.innerHTML = '<option value="">Error loading subjects</option>';
	               disable(viewMarksSubjectFilter, true);
	           }
	       })
	       .finally(() => {
	           loadMarksViewData();
	       });
	   }

	   function loadMarksViewData() {
	       if (marksRecordsTableBody) {
	           marksRecordsTableBody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;">Loading marks records...</td></tr>';
	       }

	       const filters = {
	           programId: viewMarksProgramFilter && viewMarksProgramFilter.value !== "" ? parseInt(viewMarksProgramFilter.value) : -1,
	           semester: viewMarksSemesterFilter && viewMarksSemesterFilter.value !== "" ? parseInt(viewMarksSemesterFilter.value) : -1,
	           courseId: viewMarksSubjectFilter && viewMarksSubjectFilter.value !== "" ? viewMarksSubjectFilter.value : null,
	           examType: viewMarksExamTypeFilter && viewMarksExamTypeFilter.value !== "" ? viewMarksExamTypeFilter.value : null,
	           studentSearch: viewMarksStudentSearchInput ? viewMarksStudentSearchInput.value.trim() : null,
	           facultyId: parseInt(window.currentFacultyId)
	       };

	       if (isNaN(filters.facultyId) || filters.facultyId <= 0) {
	           console.error('Faculty ID is missing or invalid for View Marks.');
	           if (marksRecordsTableBody) {
	               marksRecordsTableBody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;color:red;">Error: Faculty ID missing. Cannot load marks.</td></tr>';
	           }
	           return;
	       }

	       fetch('GetMarksRecordsServlet', {
	           method: 'POST',
	           headers: { 'Content-Type': 'application/json' },
	           body: JSON.stringify(filters)
	       })
	       .then(r => {
	           if (!r.ok) throw new Error('HTTP status ' + r.status);
	           return r.json();
	       })
	       .then(data => {
	           if (!marksRecordsTableBody) return;

	           marksRecordsTableBody.innerHTML = '';

	           if (data.length === 0) {
	               marksRecordsTableBody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;">No marks records found matching criteria.</td></tr>';
	               return;
	           }

	           data.forEach(record => {
	               const tr = document.createElement('tr');
	               tr.innerHTML = `
	                   <td>${record.studentId}</td>
	                   <td>${record.studentName}</td>
	                   <td>${record.programName}</td>
	                   <td>${record.semester}</td>
	                   <td>${record.subjectName}</td>
	                   <td>${record.examType}</td>
	                   <td>${record.marks !== null ? record.marks : 'N/A'}</td>
	                   `;
	               marksRecordsTableBody.appendChild(tr);
	           });
	       })
	       .catch(error => {
	           console.error("Error loading marks data:", error);
	           if (marksRecordsTableBody) {
	               marksRecordsTableBody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;color:red;">Failed to load marks records. ' + error.message + '</td></tr>';
	           }
	       });
	   }

	   if (viewMarksProgramFilter) {
	       viewMarksProgramFilter.addEventListener('change', populateSubjectsForViewMarksFilter);
	   }
	   if (viewMarksSemesterFilter) {
	       viewMarksSemesterFilter.addEventListener('change', populateSubjectsForViewMarksFilter);
	   }
	   if (viewMarksSubjectFilter) {
	       viewMarksSubjectFilter.addEventListener('change', loadMarksViewData);
	   }
	   if (viewMarksExamTypeFilter) {
	       viewMarksExamTypeFilter.addEventListener('change', loadMarksViewData);
	   }
	   if (viewMarksStudentSearchInput) {
	       viewMarksStudentSearchInput.addEventListener('input', debounce(loadMarksViewData, 500));
	   }
	   if (resetViewMarksFiltersBtn) {
	       resetViewMarksFiltersBtn.addEventListener('click', function() {
	           if (viewMarksProgramFilter) viewMarksProgramFilter.value = '';
	           if (viewMarksSemesterFilter) viewMarksSemesterFilter.value = '';
	           if (viewMarksSubjectFilter) viewMarksSubjectFilter.value = '';
	           if (viewMarksExamTypeFilter) viewMarksExamTypeFilter.value = '';
	           if (viewMarksStudentSearchInput) viewMarksStudentSearchInput.value = '';
	           disable(viewMarksSubjectFilter, true);
	           loadMarksViewData();
	       });
	   }

    /* =====================================================
       ==========  INITIAL PAGE LOAD LOGIC  ================
       ===================================================== */
    showOnly(dashSection); // This will initially show the dashboard and trigger its population.
    // When a user clicks on Student, Attendance, or Marks, showOnly will ensure the correct dropdowns are populated.

}); // End DOMContentLoaded

(function() {
    'use strict';

    /* ───────── HELPER FUNCTIONS ───────── */
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
    function fillSelect(selectElement, dataArray, defaultOptionText, valueKey, textKey) {
        if (!selectElement) return;
        valueKey = valueKey || 'value';
        textKey = textKey || 'text';
        selectElement.innerHTML = '<option value="" selected disabled>' + defaultOptionText + '</option>';
        dataArray.forEach(function(item) {
            var option = document.createElement('option');
            option.value = item[valueKey];
            option.textContent = item[textKey];
            if (item.disabled) {
                option.disabled = true;
                option.style.color = '#999';
                option.style.fontStyle = 'italic';
            }
            selectElement.appendChild(option);
        });
    }

    function showRedirectNotification(message, duration, onRedirect) {
        var notificationDiv = document.createElement('div');
        notificationDiv.style.cssText = 'position: fixed; top: 20px; left: 50%; transform: translateX(-50%); background-color: #2c3e50; color: white; padding: 15px 25px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); z-index: 9999; display: flex; align-items: center; gap: 20px; font-family: sans-serif;';
        notificationDiv.innerHTML =
            '<div class="spinner" style="border-top-color: #3498db;"></div>' +
            '<div style="display:flex; flex-direction:column; gap: 8px;">' +
                '<span>' + message + '</span>' +
                '<div style="display:flex; align-items:center; gap: 8px; font-size: 12px; color: #bdc3c7;">' +
                    '<input type="checkbox" id="dontAskAgainRedirect" style="margin:0;">' +
                    '<label for="dontAskAgainRedirect" style="cursor:pointer;">Do not redirect for this session</label>' +
                '</div>' +
            '</div>' +
            '<button id="cancelRedirectBtn" style="background: #e74c3c; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;">Cancel</button>';
        document.body.appendChild(notificationDiv);

        var redirectTimeout = setTimeout(function() {
            if (document.body.contains(notificationDiv)) {
                 document.body.removeChild(notificationDiv);
            }
            if (onRedirect) onRedirect();
        }, duration);

        document.getElementById('cancelRedirectBtn').addEventListener('click', function() {
            clearTimeout(redirectTimeout);
            if (document.getElementById('dontAskAgainRedirect').checked) {
                sessionStorage.setItem('skipStudentRedirect', 'true');
            }
            if (document.body.contains(notificationDiv)) {
                document.body.removeChild(notificationDiv);
            }
        });
    }

    /* ───────── DROPDOWN POPULATION LOGIC ───────── */
    function populateAssignedProgramDropdown(dropdownId, defaultText) {
        defaultText = defaultText || '-- choose program --';
        var dropdown = document.getElementById(dropdownId);
        if (!dropdown || !window.allProgramsData || !window.assignedProgramIdsData) return;
        
        var assignedIdsSet = new Set(window.assignedProgramIdsData);
        var options = window.allProgramsData.map(function(program) {
            return {
                value: program.programId,
                text: program.programName + (assignedIdsSet.has(program.programId) ? '' : ' (Not Assigned)'),
                disabled: !assignedIdsSet.has(program.programId)
            };
        });
        fillSelect(dropdown, options, defaultText);
    }
    function populateAllProgramDropdown(selectElementId, defaultOptionText) {
        defaultOptionText = defaultOptionText || "All Programs";
        var selectElement = document.getElementById(selectElementId);
        if (!selectElement) return;
        selectElement.innerHTML = '<option value="">' + defaultOptionText + '</option>';
        if (window.allProgramsData) {
            window.allProgramsData.forEach(function(program) {
                var option = document.createElement('option');
                option.value = program.programId;
                option.textContent = program.programName;
                selectElement.appendChild(option);
            });
        }
    }

	/* =====================================================
	   ==========  STUDENT-MANAGEMENT  CODE  ===============
	   ===================================================== */
	var studentUI = {
	    addCard: qs('#addStudentCard'),
	    viewSec: qs('#viewStudentSection'),
	    featureCards: qs('#studentFeatures .feature-card-wrap'),
	    form: qs('#addStudentForm'),
	    messageContainer: qs('#addStudentMessage'),
	    programIdAdd: qs('#programIdAdd'),
	    fieldsBlock: qs('#studentFields'),
	    searchInput: qs('#studentSearchInput'),
	    programFilter: qs('#programFilterDropdown'),
	    tableBody: qs('#studentsTableBody'),
	    actionHeader: qs('#studentsTable thead th:last-child'),
	    resetFiltersBtn: qs('#resetFiltersBtn')
	};

	var currentStudentAction = 'view';

	// --- NEW HELPER FUNCTIONS FOR IN-LINE EDITING ---

	/**
	 * Reverts an editing row back to its display state.
	 * @param {HTMLTableRowElement} tr - The table row to revert.
	 */
	function revertRowToDisplay(tr) {
	    if (!tr.classList.contains('is-editing')) return;

	    tr.classList.remove('is-editing');
	    const cells = tr.querySelectorAll('td');

	    // Restore original text content from stored data-attributes
	    cells[1].textContent = tr.dataset.originalName;
	    cells[3].textContent = tr.dataset.originalSemester;
	    cells[4].textContent = tr.dataset.originalEmail;
	    cells[5].textContent = tr.dataset.originalPhone;

	    // Restore the original "Edit" button
	    const studentId = cells[0].textContent;
	    cells[6].innerHTML = `<button class="btn-action-edit" data-id="${studentId}">Edit</button>`;
	    // Re-attach the main edit listener to the newly created button
	    cells[6].querySelector('.btn-action-edit').addEventListener('click', makeRowEditable);
	}

	/**
	 * Makes a table row editable by converting cells to inputs.
	 * @param {Event} event - The click event from the "Edit" button.
	 */
	function makeRowEditable(event) {
	    const editButton = event.currentTarget;
	    const tr = editButton.closest('tr');

	    // If another row is already being edited, revert it first.
	    const currentlyEditingRow = document.querySelector('tr.is-editing');
	    if (currentlyEditingRow) {
	        revertRowToDisplay(currentlyEditingRow);
	    }

	    tr.classList.add('is-editing');
	    const cells = tr.querySelectorAll('td');

	    // Store original values in dataset attributes for easy cancellation
	    tr.dataset.originalName = cells[1].textContent.trim();
	    tr.dataset.originalSemester = cells[3].textContent.trim();
	    tr.dataset.originalEmail = cells[4].textContent.trim();
	    tr.dataset.originalPhone = cells[5].textContent.trim();

	    // Replace cell content with input fields
	    cells[1].innerHTML = `<input type="text" class="edit-input" value="${tr.dataset.originalName}">`;
	    cells[3].innerHTML = `<input type="number" class="edit-input" value="${tr.dataset.originalSemester}" min="1" max="8">`;
	    cells[4].innerHTML = `<input type="email" class="edit-input" value="${tr.dataset.originalEmail}">`;
	    cells[5].innerHTML = `<input type="tel" class="edit-input" value="${tr.dataset.originalPhone}">`;

	    // Replace action button with Update and Cancel
	    const studentId = cells[0].textContent.trim();
	    cells[6].innerHTML = `
	        <button class="btn-action-update" data-id="${studentId}">Update</button>
	        <button class="btn-action-cancel">Cancel</button>
	    `;

	    // Add event listeners to the new buttons
	    cells[6].querySelector('.btn-action-update').addEventListener('click', handleUpdate);
	    cells[6].querySelector('.btn-action-cancel').addEventListener('click', () => revertRowToDisplay(tr));
	}

	/**
	 * Handles the "Update" button click, sends data to the server.
	 * @param {Event} event - The click event from the "Update" button.
	 */
	function handleUpdate(event) {
	    const updateButton = event.currentTarget;
	    const tr = updateButton.closest('tr');
	    const studentId = updateButton.dataset.id;
	    const inputs = tr.querySelectorAll('.edit-input');
	    const cells = tr.querySelectorAll('td');

	    const updatedData = {
	        studentId: studentId,
	        fullName: inputs[0].value,
	        semester: inputs[1].value,
	        email: inputs[2].value,
	        phone: inputs[3].value
	    };

	    updateButton.textContent = 'Saving...';
	    updateButton.disabled = true;

	    // YOU WILL NEED TO CREATE THIS 'UpdateStudentServlet'
	    fetch('UpdateStudentServlet', {
	        method: 'POST',
	        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	        body: new URLSearchParams(updatedData)
	    })
	    .then(response => response.json())
	    .then(result => {
	        if (result.status === 'success') {
	            // Update the display text in the table cells
	            cells[1].textContent = updatedData.fullName;
	            cells[3].textContent = updatedData.semester;
	            cells[4].textContent = updatedData.email;
	            cells[5].textContent = updatedData.phone;
	            alert('Student updated successfully!');
	            revertRowToDisplay(tr);
	        } else {
	            alert('Update failed: ' + (result.message || 'Unknown error.'));
	            updateButton.textContent = 'Update';
	            updateButton.disabled = false;
	        }
	    })
	    .catch(error => {
	        console.error('Update fetch error:', error);
	        alert('An error occurred. Please check the console.');
	        updateButton.textContent = 'Update';
	        updateButton.disabled = false;
	    });
	}


	// --- CORE STUDENT MANAGEMENT FUNCTIONS (MODIFIED) ---

	function showStudentFeature(which) {
	    // Revert any row that might be in edit mode when switching features
	    const editingRow = document.querySelector('tr.is-editing');
	    if(editingRow) revertRowToDisplay(editingRow);

	    show(studentUI.addCard, false);
	    show(studentUI.viewSec, false);
	    showFlex(studentUI.featureCards, false);

	    currentStudentAction = which || 'view';

	    switch (which) {
	        case 'add':
	            show(studentUI.addCard, true);
	            break;
	        case 'view':
	        case 'edit':
	        case 'delete':
	        case 'search':
	            show(studentUI.viewSec, true);
	            loadStudentData();
	            break;
	        default:
	            showFlex(studentUI.featureCards, true);
	            if (studentUI.searchInput) studentUI.searchInput.value = '';
	            if (studentUI.programFilter) studentUI.programFilter.value = "";
	    }
	}

	function loadStudentData() {
	    const showActions = (currentStudentAction === 'edit' || currentStudentAction === 'delete');
	    const colCount = showActions ? 7 : 6;

	    if (studentUI.actionHeader) {
	        studentUI.actionHeader.style.display = showActions ? '' : 'none';
	    }

	    if (studentUI.tableBody) {
	        studentUI.tableBody.innerHTML = `<tr><td colspan="${colCount}" style="text-align:center;padding:20px;">Loading…</td></tr>`;
	    }

	    fetch('GetStudentsServlet', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{}' })
	        .then(r => r.ok ? r.json() : Promise.reject(r))
	        .then(students => {
	            if (!studentUI.tableBody) return;
	            studentUI.tableBody.innerHTML = students.length ? '' : `<tr><td colspan="${colCount}" style="text-align:center;padding:20px;">No students found.</td></tr>`;
	            
	            students.forEach(s => {
	                const tr = document.createElement('tr');
	                tr.setAttribute('data-program-id', s.programId);

	                let rowHtml = `<td>${s.studentId}</td><td>${s.fullName}</td><td>${s.programName}</td><td>${s.semester}</td><td>${s.email}</td><td>${s.phone}</td>`;
	                
	                let actionCellHtml = '';
	                if (showActions) {
	                     if (currentStudentAction === 'edit') {
	                        actionCellHtml = `<td><button class="btn-action-edit" data-id="${s.studentId}">Edit</button></td>`;
	                    } else if (currentStudentAction === 'delete') {
	                        actionCellHtml = `<td><button class="btn-action-delete" data-id="${s.studentId}">Delete</button></td>`;
	                    }
	                }
	                tr.innerHTML = rowHtml + actionCellHtml;

	                const deleteBtn = tr.querySelector('.btn-action-delete');
	                if (deleteBtn) {
	                    deleteBtn.addEventListener('click', function() {
	                        const id = this.dataset.id;
	                        if (!confirm('Delete student ' + id + '?')) return;
	                        fetch('DeleteStudentServlet', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: 'studentId=' + encodeURIComponent(id) })
	                            .then(r => r.json())
	                            .then(res => {
	                                alert(res.message || 'Operation completed.');
	                                if (res.status === 'success') tr.remove();
	                            }).catch(() => alert('Network error'));
	                    });
	                }
	                
	                const editBtn = tr.querySelector('.btn-action-edit');
	                if (editBtn) {
	                    editBtn.addEventListener('click', makeRowEditable);
	                }
	                
	                studentUI.tableBody.appendChild(tr);
	            });
	            applyTableFilters();
	            sortTable(0, 'asc'); 
	        })
	        .catch(() => {
	            if (studentUI.tableBody) studentUI.tableBody.innerHTML = `<tr><td colspan="${colCount}" style="text-align:center;padding:20px;color:red">Failed to load students.</td></tr>`;
	        });
	}

	function applyTableFilters() {
	    const q = (studentUI.searchInput ? studentUI.searchInput.value : '').toLowerCase().trim();
	    const pId = (studentUI.programFilter ? studentUI.programFilter.value : '');
	    let visible = 0;

	    studentUI.tableBody.querySelectorAll('tr').forEach(row => {
	        const tds = row.querySelectorAll('td');
	        if (tds.length < 6 || row.classList.contains('is-editing')) {
	            row.style.display = ''; // Always show editing row
	            if (row.classList.contains('is-editing')) visible++;
	            return;
	        };

	        const matchSearch = !q || [0, 1, 4, 5].some(i => tds[i] && tds[i].textContent.toLowerCase().includes(q));
	        const matchProgram = !pId || row.getAttribute('data-program-id') === pId;

	        row.style.display = (matchSearch && matchProgram) ? '' : 'none';
	        if (row.style.display !== 'none') visible++;
	    });

	    const noRow = document.getElementById('noStudentsFilteredMessage');
	    if (noRow) show(noRow, visible === 0 && (q || pId));
	}

	function sortTable(col, dir) {
	    const rows = Array.from(studentUI.tableBody.querySelectorAll('tr[data-program-id]'));
	    
	    rows.sort((a, b) => {
	        if(a.classList.contains('is-editing')) return -1; // Keep editing row at top
	        if(b.classList.contains('is-editing')) return 1;

	        const A_cell = a.querySelectorAll('td')[col];
	        const B_cell = b.querySelectorAll('td')[col];
	        if (!A_cell || !B_cell) return 0;

	        const A = A_cell.textContent.trim();
	        const B = B_cell.textContent.trim();
	        const isNum = (col === 0 || col === 3);
	        const x = isNum ? parseFloat(A) || 0 : A.toLowerCase();
	        const y = isNum ? parseFloat(B) || 0 : B.toLowerCase();
	        
	        if (x < y) return dir === 'asc' ? -1 : 1;
	        if (x > y) return dir === 'asc' ? 1 : -1;
	        return 0;
	    }).forEach(r => studentUI.tableBody.appendChild(r));
	}

	window.initStudentFeature = function() {
	    populateAssignedProgramDropdown('programIdAdd');
	    populateAllProgramDropdown('programFilterDropdown');
	    showStudentFeature('');

	    if (studentUI.form && !studentUI.form.dataset.initialized) {
	        studentUI.form.dataset.initialized = 'true';

	        qsa('#studentFeatures .feature-card').forEach(card => {
	            card.addEventListener('click', function() {
	                const action = this.getAttribute('onclick').match(/'([^']+)'/)[1];
	                showStudentFeature(action);
	            });
	        });

	        // --- FILTER AND RESET LISTENERS ---
	        if (studentUI.searchInput) studentUI.searchInput.addEventListener('input', applyTableFilters);
	        if (studentUI.programFilter) studentUI.programFilter.addEventListener('change', applyTableFilters);
	        
	        if (studentUI.resetFiltersBtn) {
	            studentUI.resetFiltersBtn.addEventListener('click', () => {
	                if (studentUI.searchInput) studentUI.searchInput.value = '';
	                if (studentUI.programFilter) studentUI.programFilter.value = '';
	                applyTableFilters();
	            });
	        }
	        
	        // --- INTERACTIVE TABLE SORTING WITH FONT AWESOME ICONS ---
	        qsa('#studentsTable thead th[data-sort-col]').forEach(th => {
	            th.addEventListener('click', () => {
	                const colIndex = parseInt(th.dataset.sortCol, 10);
	                const currentDir = th.dataset.sortDir;
	                const newDir = currentDir === 'asc' ? 'desc' : 'asc';
	                
	                qsa('#studentsTable thead th[data-sort-col]').forEach(header => {
	                    header.removeAttribute('data-sort-dir');
	                    const icon = header.querySelector('span.fa-solid');
	                    if(icon) icon.className = 'fa-solid fa-sort sort-icon';
	                });

	                th.dataset.sortDir = newDir;
	                const currentIcon = th.querySelector('span.fa-solid');
	                if (currentIcon) {
	                    currentIcon.className = `fa-solid fa-sort-${newDir === 'asc' ? 'up' : 'down'} sort-icon`;
	                }
	                
	                sortTable(colIndex, newDir);
	            });
	        });
	        
	        // --- ADD STUDENT FORM LISTENERS ---
	        if (studentUI.programIdAdd) studentUI.programIdAdd.addEventListener('change', () => showFlex(studentUI.fieldsBlock, !!studentUI.programIdAdd.value));
	        const cancelBtn = qs('#cancelAddBtn');
	        if (cancelBtn) cancelBtn.addEventListener('click', () => {
	            if (studentUI.form) studentUI.form.reset();
	            if (qs('#programIdAdd')) qs('#programIdAdd').value = "";
	            showFlex(studentUI.fieldsBlock, false);
	            show(studentUI.messageContainer, false);
	            showStudentFeature('');
	        });
	        const backBtn = qs('#backToFeaturesBtn');
	        if (backBtn) backBtn.addEventListener('click', () => showStudentFeature(''));

	        studentUI.form.addEventListener('submit', e => {
	           e.preventDefault();
	           show(studentUI.messageContainer, true);
	           studentUI.messageContainer.innerHTML = '<div class="loading-message"><div class="spinner"></div> Saving student…</div>';
	           const startTime = Date.now();

	           const handleResponse = res => {
	               const ok = res && res.status === 'success';
	               const msg = (res && res.message) || 'Operation failed.';
	               const col = ok ? '#008000' : '#d8000c';
	               if (studentUI.messageContainer) {
	                   studentUI.messageContainer.innerHTML = `<div style="display:flex;align-items:center;gap:12px;font-weight:bold;padding:10px;border-radius:6px;border:1px solid ${col};background:${(ok ? '#e7f9ed' : '#ffe6e6')};color:${col}"><span style="font-size:24px">${(ok ? '✅' : '❌')}</span>${msg}</div>`;
	                   if (ok) {
	                       studentUI.form.reset();
	                       if (qs('#programIdAdd')) qs('#programIdAdd').value = "";
	                       showFlex(studentUI.fieldsBlock, false);
	                       if (sessionStorage.getItem('skipStudentRedirect') !== 'true') {
	                           showRedirectNotification('Redirecting to view students...', 4000, () => window.showStudentFeature('view'));
	                       }
	                   }
	                   setTimeout(() => show(studentUI.messageContainer, false), 5000);
	               }
	           };

	           fetch('AddStudServlet', { method: 'POST', body: new URLSearchParams(new FormData(e.target)) })
	               .then(res => res.json().then(data => !res.ok ? Promise.reject(data) : data))
	               .then(resJson => {
	                   const waitTime = Math.max(0, 2500 - (Date.now() - startTime));
	                   setTimeout(() => handleResponse(resJson), waitTime);
	               })
	               .catch(errJson => {
	                   const waitTime = Math.max(0, 2500 - (Date.now() - startTime));
	                   setTimeout(() => handleResponse({ status: 'error', message: errJson.message || 'Network error.' }), waitTime);
	               });
	       });
	    }
	};
	/* =====================================================
	   ==========  ATTENDANCE MANAGEMENT CODE  =============
	   ===================================================== */
	var progSel = qs('#programSelect');
	var semSel = qs('#semesterSelect');
	var subjSel = qs('#subjectSelect');
	var attTimeIn = qs('#attDateTime');

	var semestersForFillSelect = ['1', '2', '3', '4', '5', '6'].map(function(s) {
        return { value: s, text: 'Semester ' + s };
    });

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
	            var formattedSubjects = data.map(function(s) {
                    return { value: s.courseId, text: s.courseName };
                });
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

	function showAttendanceAction(act) {
	    [takeSec, viewSec].forEach(function(x) { if(x) show(x, false); });
	    if(attendCards) showFlex(attendCards, false);

	    if (act === 'take') {
	        if(takeSec) show(takeSec, true);
	    } else if (act === 'view') {
	        if(viewSec) show(viewSec, true);
	        populateAttendanceViewFilters();
	        loadAttendanceViewData();
	    } else {
	        if(attendCards) showFlex(attendCards, true);
	    }
	}
    window.showAttendanceAction = showAttendanceAction;

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
	        var facultyId = parseInt(window.currentFacultyId, 10);
	        var dateTime = attTimeIn.value;

	        if (isNaN(facultyId) || facultyId <= 0) {
	            alert('Error: Faculty ID is missing or invalid. Please refresh the page or log in again.');
	            return;
	        }

	        var confirmMsg = 'Confirm Attendance Session Details:\n\n' +
	            'Date & Time: ' + dateTime + '\n' +
	            'Program: ' + programNameForDisplay + '\n' +
	            'Semester: ' + semesterTextForDisplay + '\n' +
	            'Subject (Topic): ' + selectedTopic + '\n\n' +
	            'Click OK to start the 15-minute attendance session.';

	        if (confirm(confirmMsg)) {
	            startAttendanceSessionBtn.disabled = true;
	            if (cancelTakeAttendanceBtn) cancelTakeAttendanceBtn.disabled = true;

				var attendanceData = {
				    // ✅ ADD THESE TWO LINES
				    programId: parseInt(progSel.value, 10),
				    semester: parseInt(semSel.value, 10),
				    
				    // Your existing lines
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
	                        return response.json().catch(function() {
                                return response.text().then(function(text) {
                                    return { status: 'error', message: text || 'Unknown error starting session' };
                                });
                            }).then(function(errorData) {
                                throw new Error(errorData.message);
                            });
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


	/* =====================================================
	   ==========  ATTENDANCE VIEW RECORDS LOGIC  ==========
	   ===================================================== */
	var attProgramFilterDropdown = qs('#attProgramFilter');
	var attSemesterFilterDropdown = qs('#attSemesterFilter');
	var attSubjectFilterDropdown = qs('#attSubjectFilter');
	var attDateFilterInput = qs('#attDateFilter');
	var attStudentSearchInput = qs('#attStudentSearchInput');
	var resetAttendanceFiltersBtn = qs('#resetAttendanceFiltersBtn');
	var attendanceRecordsTableBody = qs('#attendanceRecordsTableBody');

	function loadAttendanceViewData() {
	    if (attendanceRecordsTableBody) {
	        attendanceRecordsTableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:20px;">Loading attendance records...</td></tr>';
	    }

	    var filters = {
	        programId: attProgramFilterDropdown && attProgramFilterDropdown.value !== "" ? parseInt(attProgramFilterDropdown.value, 10) : -1,
	        semester: attSemesterFilterDropdown && attSemesterFilterDropdown.value !== "" ? parseInt(attSemesterFilterDropdown.value, 10) : -1,
	        subjectId: attSubjectFilterDropdown && attSubjectFilterDropdown.value !== "" ? attSubjectFilterDropdown.value : null,
	        date: attDateFilterInput ? attDateFilterInput.value : null,
	        studentSearch: attStudentSearchInput ? attStudentSearchInput.value.trim() : null
	    };

	    fetch('GetAttendanceRecordsServlet', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify(filters)
	        })
	        .then(function(r) {
	            if (!r.ok) throw new Error('HTTP status ' + r.status);
	            return r.json();
	        })
	        .then(function(data) {
	            if (!attendanceRecordsTableBody) return;
	            attendanceRecordsTableBody.innerHTML = '';
	            var records = data.records;

	            if (!records || records.length === 0) {
	                attendanceRecordsTableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:20px;">No attendance records found matching criteria.</td></tr>';
	                return;
	            }

	            records.forEach(function(record) {
	                var tr = document.createElement('tr');
	                tr.innerHTML = '<td>' + record.studentId + '</td>' +
                        '<td>' + record.studentName + '</td>' +
                        '<td>' + new Date(record.attendanceDate).toLocaleDateString() + '</td>' +
                        '<td>' + record.subjectName + '</td>' +
                        '<td>' + (record.attendanceStatus === 'PRESENT' ? '<span class="status-present">Present</span>' : '<span class="status-absent">Absent</span>') + '</td>' +
                        '<td><button class="btn-action-edit" data-record-id="' + record.recordId + '">Edit</button>' +
                        '<button class="btn-action-delete" data-record-id="' + record.recordId + '">Delete</button></td>';
	                attendanceRecordsTableBody.appendChild(tr);
	            });
	        })
	        .catch(function(error) {
	            console.error("Error loading attendance data:", error);
	            if (attendanceRecordsTableBody) {
	                attendanceRecordsTableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:20px;color:red;">Failed to load attendance records. ' + error.message + '</td></tr>';
	            }
	        });
	}

	function populateAttendanceViewFilters() {
	    populateAllProgramDropdown('attProgramFilter', 'All Programs');

	    if (attSemesterFilterDropdown) {
	        attSemesterFilterDropdown.innerHTML = '<option value="">All Semesters</option>';
	        for (var i = 1; i <= 8; i++) {
	            var option = document.createElement('option');
	            option.value = i;
	            option.textContent = 'Semester ' + i;
	            attSemesterFilterDropdown.appendChild(option);
	        }
	    }
	}

	function populateSubjectsForFilter() {
	    var selectedProgramId = attProgramFilterDropdown.value;
	    var selectedSemester = attSemesterFilterDropdown.value;

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
	        body: 'programId=' + encodeURIComponent(selectedProgramId) + '&semester=' + encodeURIComponent(selectedSemester)
	    })
	    .then(function(r) {
	        if (!r.ok) throw new Error('HTTP status ' + r.status + ': ' + r.statusText);
	        return r.json();
	    })
	    .then(function(subjects) {
	        if (attSubjectFilterDropdown) {
	            var formattedSubjects = subjects.map(function(s) {
                    return { value: s.courseId, text: s.courseName };
                });
	            fillSelect(attSubjectFilterDropdown, formattedSubjects, 'All Subjects');
	        }
	    })
	    .catch(function(error) {
	        console.error('Error loading subjects for filter:', error);
	        if (attSubjectFilterDropdown) {
	            attSubjectFilterDropdown.innerHTML = '<option value="">Error loading subjects</option>';
	            disable(attSubjectFilterDropdown, true);
	        }
	    });
	}

	if (attProgramFilterDropdown) {
	    attProgramFilterDropdown.addEventListener('change', function() {
	        loadAttendanceViewData();
	        populateSubjectsForFilter();
	    });
	}
	if (attSemesterFilterDropdown) {
	    attSemesterFilterDropdown.addEventListener('change', populateSubjectsForFilter);
	    attSemesterFilterDropdown.addEventListener('change', loadAttendanceViewData);
	}
	if (attSubjectFilterDropdown) {
	    attSubjectFilterDropdown.addEventListener('change', loadAttendanceViewData);
	}
	if (attDateFilterInput) attDateFilterInput.addEventListener('change', loadAttendanceViewData);
    if (attStudentSearchInput) {
        // Debounce needs to be defined for this to work
        // It was moved to facultymain.js. This event listener should also be moved
        // or debounce should be exposed globally. For now, removing to fix error.
	    // attStudentSearchInput.addEventListener('input', debounce(loadAttendanceViewData, 500));
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
	var enterMarksStudentSection = qs('#enterMarksStudentSection');
	var marksFeatureCards = qs('#marksFeatureCards');
	var viewMarksProgramFilter = qs('#viewMarksProgramFilter');
	var viewMarksSemesterFilter = qs('#viewMarksSemesterFilter');
	var viewMarksSubjectFilter = qs('#viewMarksSubjectFilter');
	var viewMarksExamTypeFilter = qs('#viewMarksExamTypeFilter');
	var viewMarksStudentSearchInput = qs('#viewMarksStudentSearchInput');
	var resetViewMarksFiltersBtn = qs('#resetViewMarksFiltersBtn');
	var marksRecordsTableBody = qs('#marksRecordsTableBody');
	var viewMarksRecordsContainer = qs('#viewMarksRecordsContainer');

	function showMarksAction(action) {
	    if (marksFeatureCards) showFlex(marksFeatureCards, false);
	    show(qs('#enterMarksCourseSection'), false);
	    show(enterMarksStudentSection, false);
	    show(viewMarksRecordsContainer, false);

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
	        if (marksFeatureCards) showFlex(marksFeatureCards, true);
	    }
	}
    window.showMarksAction = showMarksAction;

	var marksSemesters = ['1', '2', '3', '4', '5', '6', '7', '8'].map(function(s) { return { value: s, text: 'Semester ' + s }; });
	var examTypes = [
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
	    var selectedProgramId = viewMarksProgramFilter.value;
	    var selectedSemester = viewMarksSemesterFilter.value;

	    if (!selectedProgramId || !selectedSemester) {
	        disable(viewMarksSubjectFilter, true);
	        fillSelect(viewMarksSubjectFilter, [], 'All Subjects');
	        loadMarksViewData();
	        return;
	    }

	    disable(viewMarksSubjectFilter, false);
	    viewMarksSubjectFilter.innerHTML = '<option value="">Loading Subjects...</option>';

	    var promise = fetch('GetCoursesByProgramAndSemesterServlet', {
	        method: 'POST',
	        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	        body: 'programId=' + encodeURIComponent(selectedProgramId) + '&semester=' + encodeURIComponent(selectedSemester)
	    });
        
        promise.then(function(r) {
            if (!r.ok) throw new Error('HTTP status ' + r.status + ': ' + r.statusText);
	        return r.json();
	    }).then(function(subjects) {
	        if (viewMarksSubjectFilter) {
	            var formattedSubjects = subjects.map(function(s) { return { value: s.courseId, text: s.courseName }; });
	            fillSelect(viewMarksSubjectFilter, formattedSubjects, 'All Subjects');
	        }
            loadMarksViewData();
	    }).catch(function(error) {
	        console.error('Error loading subjects for view marks filter:', error);
	        if (viewMarksSubjectFilter) {
	            viewMarksSubjectFilter.innerHTML = '<option value="">Error loading subjects</option>';
	            disable(viewMarksSubjectFilter, true);
	        }
            loadMarksViewData();
	    });
	}

	function loadMarksViewData() {
	    if (marksRecordsTableBody) {
	        marksRecordsTableBody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;">Loading marks records...</td></tr>';
	    }

	    var filters = {
	        programId: viewMarksProgramFilter && viewMarksProgramFilter.value !== "" ? parseInt(viewMarksProgramFilter.value, 10) : -1,
	        semester: viewMarksSemesterFilter && viewMarksSemesterFilter.value !== "" ? parseInt(viewMarksSemesterFilter.value, 10) : -1,
	        courseId: viewMarksSubjectFilter && viewMarksSubjectFilter.value !== "" ? viewMarksSubjectFilter.value : null,
	        examType: viewMarksExamTypeFilter && viewMarksExamTypeFilter.value !== "" ? viewMarksExamTypeFilter.value : null,
	        studentSearch: viewMarksStudentSearchInput ? viewMarksStudentSearchInput.value.trim() : null,
	        facultyId: parseInt(window.currentFacultyId, 10)
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
	    .then(function(r) {
	        if (!r.ok) throw new Error('HTTP status ' + r.status);
	        return r.json();
	    })
	    .then(function(data) {
	        if (!marksRecordsTableBody) return;
	        marksRecordsTableBody.innerHTML = '';
	        if (data.length === 0) {
	            marksRecordsTableBody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;">No marks records found matching criteria.</td></tr>';
	            return;
	        }
	        data.forEach(function(record) {
	            var tr = document.createElement('tr');
	            tr.innerHTML = '<td>' + record.studentId + '</td>' +
                    '<td>' + record.studentName + '</td>' +
                    '<td>' + record.programName + '</td>' +
                    '<td>' + record.semester + '</td>' +
                    '<td>' + record.subjectName + '</td>' +
                    '<td>' + record.examType + '</td>' +
                    '<td>' + (record.marks !== null ? record.marks : 'N/A') + '</td>';
	            marksRecordsTableBody.appendChild(tr);
	        });
	    })
	    .catch(function(error) {
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
        // Debounce needs to be defined for this to work
        // viewMarksStudentSearchInput.addEventListener('input', debounce(loadMarksViewData, 500));
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
    
    // EXPOSE PUBLIC FUNCTIONS TO THE GLOBAL SCOPE
    window.showStudentFeature = showStudentFeature;

    window.initAttendanceFeature = function() {
        populateAssignedProgramDropdown('programSelect');
        populateAttendanceViewFilters();
        showAttendanceAction('');
    };

    window.initMarksFeature = function() {
        populateMarksViewFilters();
        showMarksAction('');
    };

    window.initProfileFeature = function() {
        var profileId = qs('#profileFacultyId');
        var profileName = qs('#profileFacultyName');
        var profileEmail = qs('#profileFacultyEmail');
        if (profileId && window.currentFacultyId) profileId.textContent = window.currentFacultyId;
        if (profileName && window.currentFacultyName) profileName.textContent = window.currentFacultyName;
        if (profileEmail && window.currentFacultyEmail) profileEmail.textContent = window.currentFacultyEmail;
    };

})();
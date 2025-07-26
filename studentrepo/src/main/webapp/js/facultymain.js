// facultymain.js (ES5-Compatible Version)

document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    function qs(selector) { return document.querySelector(selector); }
    function qsa(selector) { return document.querySelectorAll(selector); }
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
	 // REPLACE this entire block in facultymain.js

	 document.addEventListener('click', function(e) {
	     var activeDropdown = qs('.has-dropdown.active');
	     
	     // If an active dropdown exists and the click was NOT inside it, close it.
	     // This also handles clicks outside the entire sidebar.
	     if (activeDropdown && !activeDropdown.contains(e.target)) {
	         activeDropdown.classList.remove('active');
	     }

	     // Additionally, if a link INSIDE a dropdown menu is clicked, close its parent dropdown.
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

    if (modeSwitch) {
        modeSwitch.addEventListener('click', function() {
            document.body.classList.toggle('dark');
            if (modeText) modeText.textContent = document.body.classList.contains('dark') ? 'Light mode' : 'Dark mode';
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
    };

    var navMapping = {
        'dashboardNavLink': { section: mainSections.dashboard, init: null },
        'manageStudentsLink': { section: mainSections.students, init: window.initStudentFeature },
        'attendanceNavLink': { section: mainSections.attendance, init: window.initAttendanceFeature },
        'enterMarksNavLink': { section: mainSections.marks, init: window.initMarksFeature },
        'profileNavLink': { section: mainSections.profile, init: window.initProfileFeature }
    };

    function showOnly(sectionToShow) {
        for (var key in mainSections) {
            if (mainSections.hasOwnProperty(key)) {
                var section = mainSections[key];
                if (section) {
                    section.style.display = (section === sectionToShow) ? 'block' : 'none';
                }
            }
        }
    }
    
    // Using a traditional for...in loop for ES5 compatibility
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
                        showOnly(section);
                        if (typeof init === 'function') {
                            init();
                        }

                        Array.prototype.forEach.call(qsa('.menu-links a'), function(a) {
                            a.classList.remove('active-nav');
                        });
                        linkElement.classList.add('active-nav');
                        var parentDropdown = linkElement.closest('.has-dropdown');
                        if (parentDropdown) {
                            parentDropdown.querySelector('a').classList.add('active-nav');
                        }
                        if (window.innerWidth <= 768 && sidebar) {
                            sidebar.classList.add('close');
                            sidebar.classList.remove('open');
                        }
                    });
                }
            })();
        }
    }


	// REPLACE the entire "COMMAND PALETTE SCRIPT" section in facultymain.js

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

	function openSearchModal() {
	    if (!searchModal) return;
	    searchModal.style.display = 'flex';
	    setTimeout(function() {
	        searchModal.classList.add('visible');
	        modalInput.focus();
	    }, 10);
	}

	function closeSearchModal() {
	    if (!searchModal) return;
	    searchModal.classList.remove('visible');
	    setTimeout(function() {
	        searchModal.style.display = 'none';
	        modalInput.value = '';
	        if (modalResults) modalResults.innerHTML = '';
	    }, 300);
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
	    
	    // Display Commands
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

	    // Display Students
	    if (students && students.length > 0) {
	        students.forEach(function(s) {
	            var item = document.createElement('div');
	            item.className = 'search-result-item';
	            // Student results are not clickable themselves, only their action buttons
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

	    // Fetch students only if search term is long enough
	    if (searchTerm.length >= 2) {
	        fetch('GetStudentsServlet', {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify({ searchTerm: searchTerm })
	        })
	        .then(function(r) { return r.json(); })
	        .then(function(students) {
	            // Display both commands and students
	            displayModalResults(filteredCommands, students);
	        })
	        .catch(function(err) {
	            console.error("Student search fetch failed:", err);
	            displayModalResults(filteredCommands, []); // Show commands even if student search fails
	        });
	    } else {
	        // If search term is short, only show commands
	        displayModalResults(filteredCommands, []);
	    }
	}, 300);


	// New listener for clicks on dynamically created student buttons
	if (modalResults) {
	    modalResults.addEventListener('click', function(e) {
	        var studentActionButton = e.target.closest('.action-btn[data-action="viewPerformance"]');
	        if (studentActionButton) {
	            var studentId = studentActionButton.dataset.id;
	            // Redirect to student performance page
	            window.location.href = 'viewStudentPerformance.jsp?studentId=' + studentId;
	            closeSearchModal();
	        }
	    });
	}


	if (modalInput) {
	    modalInput.addEventListener('keyup', function(e) {
	        // Note: Modern browsers use "Escape", older ones might use "Esc"
	        if (e.key === 'Escape' || e.key === 'Esc') return closeSearchModal();
	        handleModalSearch();
	    });
	}

    // --- INITIAL PAGE LOAD ---
    if (qs('#dashboardNavLink')) {
        qs('#dashboardNavLink').classList.add('active-nav');
    }
    showOnly(mainSections.dashboard);
});
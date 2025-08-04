// In js/student.js (replace the entire file with this)

document.addEventListener("DOMContentLoaded", () => {
    'use strict';
    
    const appContainer = document.querySelector('.app-container');
    if (!appContainer) {
        console.error("Dashboard Error: '.app-container' not found.");
        return;
    }

    // --- Selectors ---
    const themeToggle = document.querySelector('.theme-toggle');
    const profileDropdown = document.querySelector('.profile-dropdown');
    const eventModalOverlay = document.getElementById('event-modal-overlay');
    let isTransitioning = false;

    // --- Main Event Listener for the dashboard ---
    appContainer.addEventListener('click', (e) => {
        const navLink = e.target.closest('.nav-link');
        const backButton = e.target.closest('.back-button');
        const avatarButton = e.target.closest('.profile-avatar-btn');
        const examFilterBtn = e.target.closest('.exam-filter-btn');
        const eventListItem = e.target.closest('#upcoming-events-list .data-list-item');

        if (avatarButton) {
            e.stopPropagation();
            profileDropdown.classList.toggle('is-open');
            return;
        }
        if (navLink) {
            const target = navLink.dataset.target;
            if (target) switchView(target);
            profileDropdown.classList.remove('is-open');
            return;
        }
        if (backButton) {
            switchView('dashboard-home');
            return;
        }
        if (examFilterBtn) {
            handleExamFilterClick(examFilterBtn);
        }
        if (eventListItem) {
            showEventModal(eventListItem.dataset);
        }
    });
    
    // Close profile dropdown if clicking outside
    document.addEventListener('click', () => profileDropdown.classList.remove('is-open'));

    if (themeToggle) {
        themeToggle.addEventListener('click', () => document.body.classList.toggle('light-mode'));
    }
    // UPDATED FUNCTION: Uses transitionend for smoother view transitions
	function switchView(targetId) {
	    if (isTransitioning) return;
	    const currentView = document.querySelector('.view.is-active');
	    const newView = document.getElementById(targetId);
	    if (!newView || (currentView && currentView.id === targetId)) return;

	    isTransitioning = true;
	    const scrollWrapper = document.querySelector('.content-scroll-wrapper');

	    // Add active class immediately
	    newView.classList.add('is-active');

	    if (currentView) {
	        currentView.classList.add('is-exiting');

	        // Listen for the end of the ANIMATION on the exiting element
	        currentView.addEventListener('animationend', function handler() {
	            currentView.classList.remove('is-active', 'is-exiting');
	            currentView.removeEventListener('animationend', handler);
	            scrollWrapper.scrollTop = 0; // Reset scroll position after the old view is gone
	            isTransitioning = false;
	        }, { once: true }); // Use 'once' to automatically remove the listener after it fires
	    } else {
	        // If there's no current view, just activate the new one
	        scrollWrapper.scrollTop = 0;
	        isTransitioning = false;
	    }

	    // Run post-transition logic
	    if (targetId === 'attendance-view') updateAttendanceBars(newView);
	    filterSemesterContent(newView);

	    // Schedule-specific logic
	    if (targetId === 'schedule-view') {
	        loadTodaysSchedule();
	    } else if (targetId === 'dashboard-home') {
	        loadTodaysSchedule(true);
	    }
	}

    function updateAttendanceBars(activeView) {
        const visibleContent = activeView.querySelector('.semester-content.is-visible');
        if (!visibleContent) return;
        visibleContent.querySelectorAll('.attendance-bar').forEach(bar => {
            setTimeout(() => { bar.style.width = `${bar.dataset.percentage || 0}%`; }, 100);
        });
    }

    function filterSemesterContent(view) {
        if (!view) return;
        const filterSelect = view.querySelector('.semester-filter');
        if (!filterSelect) return;
        const selectedSemester = filterSelect.value;
        view.querySelectorAll('.semester-content').forEach(content => {
            content.classList.toggle('is-visible', content.dataset.semester === selectedSemester);
        });
        if (view.id === 'attendance-view') updateAttendanceBars(view);
    }

    document.querySelectorAll('.semester-filter').forEach(filter => {
        filter.addEventListener('change', (e) => {
            const view = document.getElementById(e.target.dataset.viewTarget);
            if (view) filterSemesterContent(view);
        });
    });

    function handleExamFilterClick(clickedButton) {
        const parentWidget = clickedButton.closest('.widget');
        if (!parentWidget) return;
        const targetExamType = clickedButton.dataset.examTarget;

        parentWidget.querySelectorAll('.exam-filter-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        clickedButton.classList.add('active');

        parentWidget.querySelectorAll('.exam-content').forEach(content => {
            content.classList.toggle('is-visible', content.dataset.examType === targetExamType);
        });
    }

    // New function to apply staggered appearance animation
    function staggerScheduleItems(container) {
        const items = container.querySelectorAll('.data-list-item');
        items.forEach((item, index) => {
            setTimeout(() => {
                // Apply final style to make the item visible
                item.style.transition = 'opacity 0.5s ease-out, transform 0.5s ease-out';
                item.style.opacity = '1';
                item.style.transform = 'translateY(0)';
            }, index * 100); // 100ms delay between each item
        });
    }

    function loadTodaysSchedule(isNextClassWidgetOnly = false) {
        const programId = window.studentProgramId;
        const semester = window.studentSemester;
        const serverDate = window.serverDate;
        const nextClassSubjectEl = document.getElementById('next-class-subject');
        const nextClassTimeEl = document.getElementById('next-class-time');
        const todaysScheduleListEl = document.getElementById('todays-schedule-list');
        const scheduleHeaderDateEl = document.getElementById('schedule-header-date');

        if (scheduleHeaderDateEl) {
            scheduleHeaderDateEl.textContent = 'Classes for ' + new Date(serverDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        }

        if (!programId || !semester || programId === 'null' || semester === 'null' || programId === '' || semester === '') {
            console.error("Student program or semester data is missing.");
            if (!isNextClassWidgetOnly) {
                if (todaysScheduleListEl) todaysScheduleListEl.innerHTML = '<p style="text-align: center; color: var(--text-secondary); padding: 20px;">Could not retrieve student details.</p>';
            }
            if (nextClassSubjectEl) nextClassSubjectEl.textContent = 'N/A';
            if (nextClassTimeEl) nextClassTimeEl.textContent = 'No class scheduled';
            return;
        }

        if (!isNextClassWidgetOnly && todaysScheduleListEl) {
            todaysScheduleListEl.innerHTML = '<div style="text-align: center; color: var(--text-secondary); padding: 20px;"><p>Loading schedule...</p></div>';
        }

        fetch(`FetchDailySchedulesServlet?programId=${programId}&semester=${semester}&date=${serverDate}`)
            .then(response => response.ok ? response.json() : Promise.reject('Failed to load schedules.'))
            .then(data => {
                const scheduleList = data.schedules;
                const now = new Date();
                const nowTime = now.getHours() * 60 + now.getMinutes();

                if (scheduleList && scheduleList.length > 0) {
                    scheduleList.sort((a, b) => a.time.localeCompare(b.time));

                    const nextClass = scheduleList.find(schedule => {
                        const classTimeParts = schedule.time.split(':');
                        const classHours = parseInt(classTimeParts[0], 10);
                        const classMinutes = parseInt(classTimeParts[1], 10);
                        const classStartTimeInMinutes = classHours * 60 + classMinutes;
                        return classStartTimeInMinutes > nowTime;
                    });

                    if (nextClass) {
                        nextClassSubjectEl.textContent = nextClass.subjectName;
                        nextClassTimeEl.textContent = nextClass.time;
                    } else {
                        nextClassSubjectEl.textContent = 'No more classes';
                        nextClassTimeEl.textContent = '';
                    }

                    if (isNextClassWidgetOnly) return;

                    todaysScheduleListEl.innerHTML = '';
                    scheduleList.forEach(schedule => {
                        const listItem = document.createElement('li');
                        listItem.className = 'data-list-item';
                        // Initial style for the staggered animation
                        listItem.style.opacity = '0';
                        listItem.style.transform = 'translateY(20px)';
                        
                        let iconHtml = `<div class="icon" style="background: var(--accent-blue);"><i class='bx bxs-book-bookmark'></i></div>`;
                        let timeRange = schedule.time;

                        listItem.innerHTML = `
                            ${iconHtml}
                            <div class="content">
                                <h4>${schedule.subjectName}</h4>
                                <p class="meta">${schedule.location}</p>
                            </div>
                            <span class="trailing">${timeRange}</span>
                        `;
                        todaysScheduleListEl.appendChild(listItem);
                    });
                    
                    // Call the new function to apply the staggered animation
                    staggerScheduleItems(todaysScheduleListEl);

                } else {
                    if (nextClassSubjectEl) nextClassSubjectEl.textContent = 'N/A';
                    if (nextClassTimeEl) nextClassTimeEl.textContent = 'No class scheduled';
                    if (!isNextClassWidgetOnly && todaysScheduleListEl) {
                        todaysScheduleListEl.innerHTML = '<p style="text-align: center; color: var(--text-secondary); padding: 20px;">No classes scheduled for today.</p>';
                    }
                }
            })
            .catch(error => {
                console.error("Error loading today's schedule:", error);
                if (!isNextClassWidgetOnly && todaysScheduleListEl) {
                    todaysScheduleListEl.innerHTML = `<p style="text-align: center; color: var(--accent-red); padding: 20px;">Failed to load schedule.</p>`;
                }
            });
    }

	// In js/student.js, replace this function
	function fetchAndDisplayUpcomingEvents() {
	    const eventsList = document.getElementById('upcoming-events-list');
	    if (!eventsList) return;

	    fetch(`FetchUpcomingEventsServlet`)
	        .then(response => response.ok ? response.json() : [])
	        .then(events => {
	            if (events.length === 0) {
	                eventsList.innerHTML = `
	                    <div style="text-align: center; color: var(--text-secondary); padding: 10px;" id="events-placeholder">
	                        <p>No upcoming events.</p>
	                    </div>
	                `;
	            } else {
	                eventsList.innerHTML = ''; // Clear previous content
	                events.forEach(event => {
	                    const iconClass = getEventIcon(event.eventCategory);
	                    const li = document.createElement('li');
	                    li.className = 'data-list-item';

	                    // Store ALL event details in data-* attributes
	                    li.dataset.title = event.eventName;
	                    li.dataset.description = event.eventDescription || 'No description provided.';
	                    // Directly use the server-formatted date strings
	                    li.dataset.date = event.eventDate || 'N/A';
	                    li.dataset.time = event.eventTime; // Pass along null/undefined if missing
	                    li.dataset.regDate = event.registrationEndDate ? 'Register by ' + event.registrationEndDate : null;
	                    li.dataset.link = event.eventLink || '#';

	                    li.innerHTML = `
	                        <div class="icon" style="background: var(--accent-blue);">
	                            <i class='${iconClass}'></i>
	                        </div>
	                        <div class="content">
	                            <h4>${event.eventName}</h4>
	                            <p class="meta">${event.eventDate || 'Date not specified'}</p>
	                        </div>
	                    `;
	                    eventsList.appendChild(li);
	                });
	            }
	        })
	        .catch(error => {
	            console.error('Error fetching upcoming events:', error);
	            if (eventsList) {
	                eventsList.innerHTML = `<p style="color:var(--accent-red);">Failed to load events.</p>`;
	            }
	        });
	}

	    // === NEW: The missing logic for the Event Modal ===

		// In js/student.js, replace this function
		function showEventModal(eventData) {
		      if (!eventModalOverlay) return;
		      
		      const timeItemEl = document.getElementById('modal-event-time-item');
		      const regDateItemEl = document.getElementById('modal-event-reg-date-item');
		      const linkButton = document.getElementById('modal-event-link');

		      document.getElementById('modal-event-title').textContent = eventData.title;
		      document.getElementById('modal-event-description').textContent = eventData.description;
		      document.getElementById('modal-event-date').textContent = eventData.date || 'Not specified';

		      // FIXED: Correctly handle missing time
		      if (eventData.time && eventData.time !== 'null' && eventData.time !== 'undefined') {
		          document.getElementById('modal-event-time').textContent = formatEventTime(eventData.time);
		          timeItemEl.style.display = 'flex';
		      } else {
		          document.getElementById('modal-event-time').textContent = 'Not Mentioned';
		          timeItemEl.style.display = 'flex';
		      }

		      // FIXED: Correctly handle missing registration date and apply text
		      if (eventData.regDate && eventData.regDate !== 'null' && eventData.regDate !== 'undefined') {
		          document.getElementById('modal-event-reg-date').textContent = eventData.regDate;
		          regDateItemEl.style.display = 'flex';
		      } else {
		          regDateItemEl.style.display = 'none';
		      }

		      if (eventData.link && eventData.link !== '#') {
		          linkButton.href = eventData.link;
		          linkButton.style.display = 'inline-flex';
		      } else {
		          linkButton.style.display = 'none';
		      }
		      
		      eventModalOverlay.classList.add('is-visible');
		  }

		  function closeEventModal() {
		      if (eventModalOverlay) eventModalOverlay.classList.remove('is-visible');
		  }
		  
		  // FIXED: Robust close listeners
		  if (eventModalOverlay) {
		      eventModalOverlay.addEventListener('click', (e) => {
		          if (e.target === eventModalOverlay) { // Only if the dark overlay is clicked
		              closeEventModal();
		          }
		      });
		      // Separate listener for any button with the close class
		      document.querySelectorAll('.modal-close-btn').forEach(btn => {
		          btn.addEventListener('click', closeEventModal);
		      });
		  }
		  
		  function formatEventTime(timeString) {
		      if (!timeString) return 'Not Mentioned';
		      const [hours, minutes] = timeString.split(':');
		      const date = new Date();
		      date.setHours(parseInt(hours, 10), parseInt(minutes, 10));
		      return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
		  }

	    function getEventIcon(category) {
	        switch (category) {
	            case 'College Event': return 'bx bxs-institution';
	            case 'Fest': return 'bx bxs-party';
	            case 'Department Based': return 'bx bxs-building-house';
	            case 'National': return 'bx bx-globe';
	            case 'Sports': return 'bx bxs-basketball';
	            default: return 'bx bx-calendar-event';
	        }
	    }

	    // Initial load calls
	    document.querySelectorAll('.view').forEach(view => filterSemesterContent(view));
	    loadTodaysSchedule(true);
	    fetchAndDisplayUpcomingEvents();
	});

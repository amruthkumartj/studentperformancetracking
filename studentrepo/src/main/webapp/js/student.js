document.addEventListener("DOMContentLoaded", () => {
    const appContainer = document.querySelector('.app-container');
    if (!appContainer) { 
        console.error("Dashboard Error: '.app-container' not found. The JSP might have failed to render.");
        return; 
    }

    const themeToggle = document.querySelector('.theme-toggle');
    const profileDropdown = document.querySelector('.profile-dropdown');
    let isTransitioning = false;

    // --- Main Event Listener for all clicks within the app ---
    appContainer.addEventListener('click', (e) => {
        const navLink = e.target.closest('.nav-link');
        const backButton = e.target.closest('.back-button');
        const avatarButton = e.target.closest('.profile-avatar-btn');
        const examFilterBtn = e.target.closest('.exam-filter-btn');

        if (avatarButton) {
            profileDropdown.classList.toggle('is-open');
            return;
        }

        if (!e.target.closest('.profile-menu-wrapper')) {
            profileDropdown.classList.remove('is-open');
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
    });

    if (themeToggle) {
        themeToggle.addEventListener('click', () => document.body.classList.toggle('light-mode'));
    }

    // --- View Switching Logic ---
    function switchView(targetId) {
        if (isTransitioning) return;
        const currentView = document.querySelector('.view.is-active');
        const newView = document.getElementById(targetId);
        if (!newView || (currentView && currentView.id === targetId)) return;
        
        isTransitioning = true;
        newView.classList.add('is-active');
        if (currentView) currentView.classList.add('is-exiting');
        
        setTimeout(() => {
            document.querySelector('.content-scroll-wrapper').scrollTop = 0;
            if (currentView) currentView.classList.remove('is-active', 'is-exiting');
            if (targetId === 'attendance-view') updateAttendanceBars(newView);
            filterSemesterContent(newView);
            isTransitioning = false;
        }, 350);
    }

    // --- Attendance & Semester Filtering ---
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

    // --- NEW: Exam Type Filtering Logic ---
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

    // --- Initial Setup on Page Load ---
    document.querySelectorAll('.view').forEach(view => {
        filterSemesterContent(view);
    });
});
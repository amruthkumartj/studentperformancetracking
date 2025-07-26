<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>Student Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500&family=Poppins:wght@500;600;700&display=swap" rel="stylesheet">

<style>
    :root {
        --font-family: 'Inter', -apple-system, sans-serif;
        --radius-small: 8px;
        --radius-medium: 16px;
        --radius-large: 24px;
        --radius-curve: 32px;
        --transition-fast: 0.2s ease;
        --transition-smooth: 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
        
        /* Dark Theme (Default) */
        --bg-color: #121212;
        --content-bg: rgba(28, 28, 30, 0.7);
        --card-bg: #1c1c1e;
        --text-primary: #f5f5f7;
        --text-secondary: #a1a1a6;
        --border-color: rgba(255, 255, 255, 0.12);
        --hover-bg: rgba(255, 255, 255, 0.05);
        --shadow-light: 0 4px 12px rgba(0, 0, 0, 0.1);
        --shadow-strong: 0 8px 32px rgba(0, 0, 0, 0.2);

        --accent-green: #34c759;
        --accent-blue: #007aff;
        --accent-orange: #ff9500;
        --accent-red: #ff3b30;
    }

    body.light-mode {
        --bg-color: #f0f2f5;
        --content-bg: rgba(255, 255, 255, 0.75);
        --card-bg: #ffffff;
        --text-primary: #1a1b1f;
        --text-secondary: #5a5b60;
        --border-color: rgba(0, 0, 0, 0.08);
        --hover-bg: rgba(0, 0, 0, 0.03);
    }
    
    * { 
        margin: 0; 
        padding: 0; 
        box-sizing: border-box; 
        font-family: var(--font-family); 
        transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease, fill 0.3s ease;
    }

    .welcome-header h1, .view-header h2, .nav-card h3, .data-list-item h4, .widget-header, .attendance-details .percentage {
        font-family: 'Poppins', sans-serif;
    }

    body { 
        background-color: var(--bg-color); 
        color: var(--text-primary); 
        height: 100vh; 
        overflow: hidden; 
    }
    .app-container { display: flex; flex-direction: column; height: 100%; }
    
    .app-header {
        position: relative;
        padding: 24px 24px 88px 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #fff;
        background-size: cover;
        background-position: center;
        background-image: url('NHCE_Campus2.jpg'); 
    }
    .header-logo img { height: 40px; opacity: 0.95; }
    .header-actions { display: flex; align-items: center; gap: 16px; }
    .action-btn { font-size: 24px; background: none; border: none; cursor: pointer; height: 44px; width: 44px; display: grid; place-items: center; border-radius: 50%; transition: background-color var(--transition-fast); }
    .app-header .action-btn { color: #fff; text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6); }
    .app-header .action-btn:hover { background-color: rgba(255, 255, 255, 0.15); }

    .theme-toggle .bx-moon { display: none; }
    .theme-toggle .bx-sun { display: block; }
    body.light-mode .theme-toggle .bx-sun { display: none; }
    body.light-mode .theme-toggle .bx-moon { display: block; }
    
    .profile-avatar-btn { padding: 0; }
    .profile-avatar { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; border: 2px solid rgba(255,255,255,0.7); }
    
    .content-area {
        flex-grow: 1;
        background-color: var(--content-bg);
        backdrop-filter: blur(30px);
        -webkit-backdrop-filter: blur(30px);
        border-top-left-radius: var(--radius-curve);
        border-top-right-radius: var(--radius-curve);
        margin-top: -64px;
        border: 1px solid var(--border-color);
        box-shadow: var(--shadow-strong);
        position: relative;
        overflow: hidden;
        display: flex;
        flex-direction: column;
    }
    .content-scroll-wrapper { flex-grow: 1; overflow-y: auto; overflow-x: hidden; position: relative; padding: 24px; }
    .content-scroll-wrapper::-webkit-scrollbar { width: 8px; }
    .content-scroll-wrapper::-webkit-scrollbar-track { background-color: transparent; }
    .content-scroll-wrapper::-webkit-scrollbar-thumb { background-color: rgba(128, 128, 128, 0.3); border-radius: 4px; }
    
    @keyframes slideInFromRight { from { transform: translateX(30px); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    @keyframes fadeOut { to { opacity: 0; } }
    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

    .view { display: none; flex-direction: column; width: 100%; }
    .view.is-active { 
        display: flex; 
        animation: slideInFromRight var(--transition-smooth) forwards;
    }
    .view.is-exiting {
        display: flex !important; 
        animation: fadeOut 0.3s forwards ease;
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        width: auto;
        padding: 24px;
    }

    .view.is-active .widget, .view.is-active .nav-card {
        animation: fadeInUp 0.5s 0.1s forwards ease-out;
        opacity: 0;
    }
    .view.is-active .nav-grid .nav-card:nth-child(2) { animation-delay: 0.2s; }
    .view.is-active .home-col-right .widget { animation-delay: 0.2s; }

    .view-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .view-header .back-button { font-size: 22px; background-color: var(--card-bg); color: var(--text-primary); border: 1px solid var(--border-color); }
    .view-header .back-button:hover { background-color: var(--hover-bg); }
    .view-header h2 { font-size: 24px; font-weight: 600; }
    
    .welcome-header h1 { font-size: 24px; font-weight: 700; }
    .welcome-header p { color: var(--text-secondary); font-size: 16px; margin-bottom: 24px; }
    
    .home-layout-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; }
    .home-col-left, .home-col-right { display: flex; flex-direction: column; gap: 24px; }
    
    .widget { background-color: var(--card-bg); border: 1px solid var(--border-color); border-radius: var(--radius-large); padding: 24px; }
    .widget-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; font-size: 14px; font-weight: 600; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px; }
    .widget-header i { font-size: 18px; }
    
    .data-list { list-style: none; display: flex; flex-direction: column; gap: 12px; }
    .data-list-item { display: flex; align-items: center; gap: 16px; padding: 14px; border-radius: var(--radius-medium); border: 1px solid transparent; transition: background-color var(--transition-fast), border-color 0.3s; }
    .data-list-item:hover { background-color: var(--hover-bg); }
    .data-list-item .icon { width: 36px; height: 36px; display: grid; place-items: center; border-radius: var(--radius-small); color: #fff; font-size: 20px; flex-shrink: 0; }
    .data-list-item .content { flex-grow: 1; }
    .data-list-item h4 { font-weight: 500; font-size: 15px; margin:0; }
    .data-list-item .meta { color: var(--text-secondary); font-size: 13px; }
    .data-list-item .trailing { font-weight: 500; font-size: 14px; color: var(--text-primary); }

    .nav-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
    .nav-card { background: var(--card-bg); padding: 20px; border-radius: var(--radius-medium); border: 1px solid var(--border-color); display: flex; align-items: flex-start; gap: 16px; cursor: pointer; transition: transform var(--transition-fast), box-shadow var(--transition-fast), background-color 0.3s, border-color 0.3s; }
    .nav-card:hover { transform: translateY(-5px); box-shadow: var(--shadow-strong); }
    .nav-card .icon-wrapper { width: 40px; height: 40px; border-radius: var(--radius-small); display: grid; place-items: center; flex-shrink: 0; color: #fff; font-size: 22px; }
    .nav-card h3 { font-size: 16px; font-weight: 600; margin: 0 0 2px 0; }
    .nav-card p { font-size: 13px; color: var(--text-secondary); line-height: 1.4; margin: 0; }

    .action-btn, .nav-card {
        -webkit-tap-highlight-color: transparent;
    }

    .profile-avatar-large-wrapper { text-align: center; margin-bottom: 24px; }
    .profile-avatar-large { width: 110px; height: 110px; border-radius: 50%; border: 4px solid var(--accent-blue); }
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; font-size: 13px; font-weight: 500; color: var(--text-secondary); margin-bottom: 8px; }
    .form-group .form-control { width: 100%; padding: 14px; border: 1px solid var(--border-color); background-color: var(--card-bg); border-radius: var(--radius-small); font-size: 16px; color: var(--text-primary); }
    .form-group .form-control:focus { outline: none; border-color: var(--accent-blue); box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.2); }
    
    .attendance-chart-container { display: flex; align-items: center; gap: 24px; }
    .attendance-svg-chart { width: 120px; height: 120px; transform: rotate(-90deg); }
    .progress-ring-bg { stroke: var(--border-color); stroke-width: 4; }
    .progress-ring-fg { 
        stroke: url(#progress-gradient); 
        stroke-width: 4; 
        stroke-linecap: round; 
        transition: stroke-dashoffset 1s var(--transition-smooth);
    }
    .attendance-details .percentage { font-size: 40px; font-weight: 700; color: var(--text-primary); }
    
    @media (max-width: 1024px) { .home-layout-grid { grid-template-columns: 1fr; } }
    
    @media (max-width: 600px) {
        .app-header { 
            padding: 16px 16px 70px 16px;
        }
        /* --- CHANGE: Reduce logo size on mobile to fix alignment --- */
        .header-logo img {
            height: 32px;
        }
        .content-area { margin-top: -40px; }
        .content-scroll-wrapper { 
            padding: 16px;
            scrollbar-width: none;
        }
        .content-scroll-wrapper::-webkit-scrollbar {
            display: none;
        }
        .nav-grid { grid-template-columns: 1fr; }
        .attendance-chart-container { flex-direction: column; text-align: center; }
        .view.is-exiting { padding: 16px; }
    }
</style>
</head>
<body class="dark-mode">

<div class="app-container">
    <header class="app-header">
        <div class="header-logo"><img src="New_Horizon_College_of_Engineering_logo.png" alt="NHCE Logo"></div>
        <div class="header-actions">
            <button class="action-btn theme-toggle">
                <i class='bx bx-sun'></i>
                <i class='bx bx-moon'></i>
            </button>
            <button class="action-btn profile-avatar-btn nav-link" data-target="profile-view"><img src="https://i.pravatar.cc/44?u=Nihal" alt="Avatar" class="profile-avatar"></button>
        </div>
    </header>

    <main class="content-area">
        <div class="content-scroll-wrapper">
            
            <div id="dashboard-home" class="view is-active">
                 <div class="welcome-header">
                    <h1>Welcome, Nihal!</h1>
                    <br><br>
                </div>
                <div class="home-layout-grid">
                    <div class="home-col-left">
                        <div class="widget">
                            <div class="widget-header"><i class='bx bxs-star'></i><span>Today's Highlights</span></div>
                            <ul class="data-list">
                                <li class="data-list-item">
                                    <div class="icon" style="background-color: var(--accent-orange);"><i class='bx bx-time-five'></i></div>
                                    <div class="content"><h4>Next Class: AI & ML</h4><p class="meta">12:00 PM - 1:00 PM</p></div>
                                </li>
                                <li class="data-list-item">
                                    <div class="icon" style="background-color: var(--accent-green);"><i class='bx bxs-pie-chart-alt-2'></i></div>
                                    <div class="content"><h4>Overall Attendance</h4></div>
                                    <span class="trailing">88%</span>
                                </li>
                            </ul>
                        </div>
                        <div class="nav-card nav-link" data-target="attendance-view">
                            <div class="icon-wrapper" style="background: linear-gradient(135deg, #23C35F, #34c759);"><i class='bx bxs-pie-chart-alt-2'></i></div>
                            <div><h3>Attendance</h3><p>View your detailed status and history</p></div>
                        </div>
                        <div class="nav-grid">
                            <div class="nav-card nav-link" data-target="marks-view">
                                <div class="icon-wrapper" style="background: linear-gradient(135deg, #007BFF, #0056b3);"><i class='bx bxs-medal'></i></div>
                                <div><h3>Marks & Grades</h3><p>Check recent scores</p></div>
                            </div>
                            <div class="nav-card nav-link" data-target="schedule-view">
                                <div class="icon-wrapper" style="background: linear-gradient(135deg, #FF9500, #c77400);"><i class='bx bxs-calendar'></i></div>
                                <div><h3>Schedule</h3><p>See your timetable</p></div>
                            </div>
                        </div>
                    </div>
                    <div class="home-col-right">
                        <div class="widget">
                            <div class="widget-header"><i class='bx bxs-calendar-star'></i><span>Upcoming Events</span></div>
                            <ul class="data-list">
                                <li class="data-list-item"><div class="icon" style="background: var(--accent-green);"><i class='bx bx-calendar-event'></i></div><div class="content"><h4>Innovate 2.0 Hackathon</h4></div><span class="trailing">Aug 15</span></li>
                                <li class="data-list-item"><div class="icon" style="background: var(--accent-red);"><i class='bx bxs-flame'></i></div><div class="content"><h4>Sports Day</h4></div><span class="trailing">Sep 01</span></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
            
            <div id="profile-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>My Profile</h2></div>
                <div class="widget">
                    <div class="profile-avatar-large-wrapper"><img src="https://i.pravatar.cc/110?u=Nihal" alt="Student Avatar" class="profile-avatar-large"></div>
                    <div class="form-group"><label for="studentName">Full Name</label><input type="text" id="studentName" class="form-control" value="Nihal Kumar" readonly></div>
                    <div class="form-group"><label for="studentId">Student ID</label><input type="text" id="studentId" class="form-control" value="1NH21CS100" readonly></div>
                </div>
            </div>

            <div id="attendance-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>Attendance</h2></div>
                <div class="widget">
                    <div class="widget-header"><i class='bx bxs-pie-chart-alt-2'></i><span>Overall Percentage</span></div>
                    <div class="attendance-chart-container">
                        <svg class="attendance-svg-chart" viewBox="0 0 36 36" data-percentage="88">
                            <defs><linearGradient id="progress-gradient" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#34c759" /><stop offset="100%" stop-color="#23C35F" /></linearGradient></defs>
                            <path class="progress-ring-bg" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"></path>
                            <path class="progress-ring-fg" fill="none" stroke-dasharray="100, 100" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"></path>
                        </svg>
                        <div class="attendance-details"><span class="percentage">88%</span><p class="meta">Great standing!</p></div>
                    </div>
                </div>
            </div>
            
            <div id="marks-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>Marks & Grades</h2></div>
                <div class="widget">
                    <div class="widget-header"><i class='bx bxs-medal'></i><span>Internal Scores</span></div>
                    <ul class="data-list">
                        <li class="data-list-item"><div class="icon" style="background-color: #5865F2;"><i class='bx bxl-react'></i></div><div class="content"><h4>Web Technologies</h4></div><span class="trailing">85%</span></li>
                        <li class="data-list-item"><div class="icon" style="background-color: var(--accent-green);"><i class='bx bxs-brain'></i></div><div class="content"><h4>AI & Machine Learning</h4></div><span class="trailing">92%</span></li>
                    </ul>
                </div>
            </div>
            
            <div id="schedule-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>Today's Schedule</h2></div>
                <div class="widget">
                    <div class="widget-header"><i class='bx bxs-calendar'></i><span>Classes for Jul 27, 2025</span></div>
                    <ul class="data-list">
                        <li class="data-list-item">
                            <div class="icon" style="background: var(--accent-blue);"><i class='bx bx-code-alt'></i></div>
                            <div class="content"><h4>Web Tech Lab</h4><p class="meta">Block A, Room 301</p></div>
                            <span class="trailing">10-11 AM</span></li>
                    </ul>
                </div>
            </div>
        </div>
    </main>
</div>

<script>
document.addEventListener("DOMContentLoaded", () => {
    const navLinks = document.querySelectorAll('.nav-link');
    const backButtons = document.querySelectorAll('.back-button');
    const themeToggle = document.querySelector('.theme-toggle');
    const scrollWrapper = document.querySelector('.content-scroll-wrapper');
    let isTransitioning = false;

    function switchView(targetId) {
        if (isTransitioning) return;

        const currentView = document.querySelector('.view.is-active');
        const newView = document.getElementById(targetId);
        
        if (!newView || (currentView && currentView.id === targetId)) return;
        
        isTransitioning = true;

        newView.classList.add('is-active');
        
        if (currentView) {
            currentView.classList.add('is-exiting');
        }

        setTimeout(() => {
            scrollWrapper.scrollTop = 0;

            if (currentView) {
                currentView.classList.remove('is-active', 'is-exiting');
            }

            if (targetId === 'attendance-view') {
                updateAttendanceChart();
            }

            isTransitioning = false;
        }, 300);
    }


    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            const target = link.dataset.target;
            if (target) switchView(target);
        });
    });

    backButtons.forEach(button => {
        button.addEventListener('click', () => switchView('dashboard-home'));
    });

    themeToggle.addEventListener('click', () => {
        document.body.classList.toggle('light-mode');
    });

    function updateAttendanceChart() {
        const chart = document.querySelector('#attendance-view .attendance-svg-chart');
        if (!chart) return;

        const percentage = chart.dataset.percentage;
        const ring = chart.querySelector('.progress-ring-fg');
        const offset = 100 - percentage;
        
        setTimeout(() => {
            ring.style.strokeDashoffset = offset;
        }, 100); 
    }
});
</script>

</body>
</html>
<%@ page isELIgnored="false" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>Student Dashboard | ${dashboard.studentName}</title>
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
    * { margin: 0; padding: 0; box-sizing: border-box; font-family: var(--font-family); transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease, fill 0.3s ease, opacity 0.3s ease, transform 0.3s ease; }
    .welcome-header h1, .view-header h2, .nav-card h3, .data-list-item h4, .widget-header, .attendance-details .percentage, .semester-filter-wrapper label, .marks-table th, .profile-dropdown-item { font-family: 'Poppins', sans-serif; }
    body { background-color: var(--bg-color); color: var(--text-primary); height: 100vh; overflow: hidden; }
    .app-container { display: flex; flex-direction: column; height: 100%; }
    .app-header { position: relative; padding: 24px 24px 88px 24px; display: flex; justify-content: space-between; align-items: center; color: #fff; background-size: cover; background-position: center; background-image: url('${pageContext.request.contextPath}/NHCE_Campus2.jpg'); }
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
    /* --- NEW: Profile Dropdown --- */
    .profile-menu-wrapper { position: relative; }
    .profile-dropdown { position: absolute; top: 120%; right: 0; width: 200px; background-color: var(--card-bg); border-radius: var(--radius-medium); border: 1px solid var(--border-color); box-shadow: var(--shadow-strong); padding: 8px; z-index: 100; opacity: 0; visibility: hidden; transform: translateY(-10px); transition: opacity 0.2s ease, visibility 0.2s ease, transform 0.2s ease; }
    .profile-dropdown.is-open { opacity: 1; visibility: visible; transform: translateY(0); }
    .profile-dropdown-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: var(--radius-small); font-size: 15px; font-weight: 500; color: var(--text-primary); text-decoration: none; cursor: pointer; }
    .profile-dropdown-item:hover { background-color: var(--hover-bg); }
    .profile-dropdown-item i { font-size: 20px; color: var(--text-secondary); }
    .profile-dropdown-item.logout { color: var(--accent-red); }
    .profile-dropdown-item.logout i { color: var(--accent-red); }
    .profile-dropdown-divider { height: 1px; background-color: var(--border-color); margin: 8px 0; }
    .content-area { flex-grow: 1; background-color: var(--content-bg); backdrop-filter: blur(30px); -webkit-backdrop-filter: blur(30px); border-top-left-radius: var(--radius-curve); border-top-right-radius: var(--radius-curve); margin-top: -64px; border: 1px solid var(--border-color); box-shadow: var(--shadow-strong); position: relative; overflow: hidden; display: flex; flex-direction: column; }
    .content-scroll-wrapper { flex-grow: 1; overflow-y: auto; overflow-x: hidden; position: relative; padding: 24px; }
    .content-scroll-wrapper::-webkit-scrollbar { width: 8px; }
    .content-scroll-wrapper::-webkit-scrollbar-track { background-color: transparent; }
    .content-scroll-wrapper::-webkit-scrollbar-thumb { background-color: rgba(128, 128, 128, 0.3); border-radius: 4px; }
    @keyframes slideInFromRight { from { transform: translateX(30px); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    @keyframes fadeOut { to { opacity: 0; } }
    @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
    .view { display: none; flex-direction: column; width: 100%; }
    .view.is-active { display: flex; animation: slideInFromRight var(--transition-smooth) forwards; }
    .view.is-exiting { display: flex !important; animation: fadeOut 0.3s forwards ease; position: absolute; top: 0; left: 0; right: 0; bottom: 0; width: auto; padding: 24px; }
    .view.is-active .widget, .view.is-active .nav-card { animation: fadeInUp 0.5s 0.1s forwards ease-out; opacity: 0; }
    .view.is-active .nav-grid .nav-card:nth-child(2) { animation-delay: 0.2s; }
    /* --- FIX: Header Alignment --- */
    .view-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .view-header h2 { font-size: 24px; font-weight: 600; margin-right: auto; } /* Pushes other items to the right */
    .view-header .back-button { font-size: 22px; background-color: var(--card-bg); color: var(--text-primary); border: 1px solid var(--border-color); flex-shrink: 0; }
    .view-header .back-button:hover { background-color: var(--hover-bg); }
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
    /* --- FIX: Highlight Widget Alignment --- */
    .data-list-item .content { flex-grow: 1; min-width: 0; } /* Allows content to shrink and prevents pushing */
    .data-list-item h4 { font-weight: 500; font-size: 15px; margin:0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .data-list-item .meta { color: var(--text-secondary); font-size: 13px; }
    .data-list-item .trailing { font-weight: 500; font-size: 14px; color: var(--text-primary); flex-shrink: 0; padding-left: 16px; }
    .nav-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
    .nav-card { background: var(--card-bg); padding: 20px; border-radius: var(--radius-medium); border: 1px solid var(--border-color); display: flex; align-items: flex-start; gap: 16px; cursor: pointer; transition: transform var(--transition-fast), box-shadow var(--transition-fast), background-color 0.3s, border-color 0.3s; }
    .nav-card:hover { transform: translateY(-5px); box-shadow: var(--shadow-strong); }
    .nav-card .icon-wrapper { width: 40px; height: 40px; border-radius: var(--radius-small); display: grid; place-items: center; flex-shrink: 0; color: #fff; font-size: 22px; }
    .nav-card h3 { font-size: 16px; font-weight: 600; margin: 0 0 2px 0; }
    .nav-card p { font-size: 13px; color: var(--text-secondary); line-height: 1.4; margin: 0; }
    .action-btn, .nav-card { -webkit-tap-highlight-color: transparent; }
    .profile-avatar-large-wrapper { text-align: center; margin-bottom: 24px; }
    .profile-avatar-large { width: 110px; height: 110px; border-radius: 50%; border: 4px solid var(--accent-blue); }
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; font-size: 13px; font-weight: 500; color: var(--text-secondary); margin-bottom: 8px; }
    .form-group .form-control { width: 100%; padding: 14px; border: 1px solid var(--border-color); background-color: var(--card-bg); border-radius: var(--radius-small); font-size: 16px; color: var(--text-primary); }
    .form-group .form-control:focus { outline: none; border-color: var(--accent-blue); box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.2); }
    .semester-filter-wrapper { display: flex; align-items: center; gap: 10px; }
    .semester-filter-wrapper label { font-size: 14px; color: var(--text-secondary); font-weight: 500; }
    .semester-filter { padding: 8px 12px; background-color: var(--card-bg); color: var(--text-primary); border: 1px solid var(--border-color); border-radius: var(--radius-small); font-size: 14px; }
    .semester-filter:focus { outline: none; border-color: var(--accent-blue); }
    .semester-content { display: none; }
    .semester-content.is-visible, .widget .exam-content.is-visible { display: block; }
    .marks-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
    .marks-table th, .marks-table td { padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--border-color); }
    .marks-table th { font-size: 13px; font-weight: 600; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px; }
    .marks-table td { font-size: 15px; }
    .marks-table .course-name { font-weight: 500; }
    .marks-table .not-available { color: var(--text-secondary); font-style: italic; }
    .attendance-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-top: 20px; }
    .attendance-card { background-color: var(--card-bg); border-radius: var(--radius-medium); padding: 20px; border: 1px solid var(--border-color); }
    .attendance-card h4 { font-size: 16px; font-weight: 600; margin: 0 0 12px 0; }
    .attendance-bar-container { background-color: var(--hover-bg); height: 8px; border-radius: 4px; overflow: hidden; }
    .attendance-bar { background: linear-gradient(90deg, #23C35F, #34c759); height: 100%; width: 0; transition: width 1s var(--transition-smooth); }
    .attendance-meta { display: flex; justify-content: space-between; margin-top: 8px; font-size: 13px; color: var(--text-secondary); }
    @media (max-width: 1024px) { .home-layout-grid { grid-template-columns: 1fr; } }
@media (max-width: 600px) {
    .app-header { padding: 16px 16px 70px 16px; }
    .header-logo img { height: 32px; }
    .content-area {
        margin-top: -40px;
        width: 100%; /* Ensure content-area itself takes full width */
        max-width: 100%; /* Prevent overflow */
        box-sizing: border-box;
    }
    .content-scroll-wrapper {
        padding: 16px;
        scrollbar-width: none;
        width: 100%; /* Ensure it takes full width */
        max-width: 100%; /* Prevent overflow */
        box-sizing: border-box;
        overflow-x: hidden; /* Crucial: Hide any horizontal overflow within this wrapper */
    }
    .content-scroll-wrapper::-webkit-scrollbar { display: none; }

    /* Ensure the main layout grid collapses and its columns take full width */
    .home-layout-grid {
        grid-template-columns: 1fr; /* Ensures single column layout */
        width: 100%; /* Make sure the grid itself takes full width */
        max-width: 100%; /* Prevent it from exceeding its container */
        margin: 0 auto; /* Center the grid horizontally */
        box-sizing: border-box; /* Include padding/border in its width */
        padding: 0; /* Remove any internal padding that might cause issues here */
    }
    .home-col-left,
    .home-col-right {
        width: 100%; /* Make sure both columns take full width on mobile */
        max-width: 100%; /* Prevent overflow */
        box-sizing: border-box; /* Include padding/border in width */
        padding: 0; /* Reset padding to ensure content aligns to the edge of the column */
    }

    /* Ensure widgets and nav cards take full width within their containers */
    .widget,
    .nav-card {
        width: 100%; /* Explicitly set to 100% to fit parent */
        max-width: 100%; /* Prevent it from exceeding its container */
        box-sizing: border-box; /* Crucial for padding to be internal */
        margin: 0; /* Remove any default margins that might push it right */
    }

    .nav-grid { grid-template-columns: 1fr; }
    .view.is-exiting { padding: 16px; }
    .marks-table { font-size: 14px; }
    .marks-table th, .marks-table td { padding: 10px; }
    .marks-table {
        font-size: 12px;
    }
    .marks-table th, .marks-table td {
        padding: 8px 6px; /* Reduced padding */
    }
    .marks-table thead th:last-child,
    .marks-table tbody td:last-child {
        white-space: nowrap; /* Prevent wrapping of percentage */
        text-align: right; /* Align percentage to the right */
    }
    /* ✨ NEW STYLES FOR SCROLLABLE TABLE ✨ */
    .table-scroll-wrapper {
        overflow-x: auto; /* This is the key property! */
        -webkit-overflow-scrolling: touch; /* Smooth scrolling on iOS */
    }
}
    /* --- FIX for UI BUG --- */
    .widget .exam-content { display: none; }
    .widget .exam-content.is-visible { display: block; }
    .attendance-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-top: 20px; }
    .attendance-card h4 { font-size: 16px; font-weight: 600; margin: 0 0 12px 0; }
    .attendance-bar-container { background-color: var(--hover-bg); height: 8px; border-radius: 4px; overflow: hidden; }
    .attendance-bar { background: linear-gradient(90deg, #23C35F, #34c759); height: 100%; width: 0; transition: width 1s var(--transition-smooth); }
    .attendance-meta { display: flex; justify-content: space-between; margin-top: 8px; font-size: 13px; color: var(--text-secondary); }
    .marks-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
    .marks-table th, .marks-table td { padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--border-color); }
    .marks-table th { font-size: 13px; font-weight: 600; color: var(--text-secondary); text-transform: uppercase; }
    .marks-table .not-available { color: var(--text-secondary); font-style: italic; }
    .exam-filter-container { display: flex; gap: 8px; padding: 6px; background-color: var(--hover-bg); border-radius: var(--radius-small); margin-bottom: 24px; }
    .exam-filter-btn { flex: 1; padding: 10px; border: none; background-color: transparent; color: var(--text-secondary); font-family: 'Poppins', sans-serif; font-weight: 500; font-size: 14px; border-radius: 6px; cursor: pointer; }
    .exam-filter-btn.active { background-color: var(--card-bg); color: var(--text-primary); box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    /* Add this to your existing styles */
/* Ensure this is present and correct */
.view {
    /* Existing styles, if any */
    display: none; 
    flex-direction: column;
    /* This transition is crucial for the back button to work */
    transition: opacity 0.3s ease-out, transform 0.3s ease-out;
}

/* Also ensure your is-exiting class is defined */
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
/* In studentDashboard.jsp, inside the <style> tag */

/* --- Styles for Event Details Modal --- */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(5px);
    -webkit-backdrop-filter: blur(5px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.3s ease, visibility 0.3s ease;
}
.modal-overlay.is-visible {
    opacity: 1;
    visibility: visible;
}
.modal-content {
    background-color: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-large);
    box-shadow: var(--shadow-strong);
    width: 90%;
    max-width: 550px;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    transform: scale(0.95);
    transition: transform 0.3s ease;
}
.modal-overlay.is-visible .modal-content {
    transform: scale(1);
}
.modal-header {
    padding: 20px 24px;
    border-bottom: 1px solid var(--border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.modal-header h3 {
    font-family: 'Poppins', sans-serif;
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
}
.modal-close-btn {
    font-size: 28px;
}
.modal-body {
    padding: 24px;
    overflow-y: auto;
    color: var(--text-secondary);
    line-height: 1.6;
}
.modal-body::-webkit-scrollbar { width: 5px; }
.modal-body::-webkit-scrollbar-thumb { background-color: rgba(128, 128, 128, 0.3); border-radius: 3px; }
.modal-body .event-meta-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid var(--border-color);
}
.meta-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
}
.meta-item i {
    font-size: 20px;
    color: var(--text-primary);
}
.modal-footer {
    padding: 16px 24px;
    border-top: 1px solid var(--border-color);
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}
.modal-footer .btn {
    padding: 10px 20px;
    font-size: 15px;
    font-weight: 500;
}
.btn-register {
    background-color: var(--accent-blue);
    color: #fff;
}
.btn-register:hover {
    filter: brightness(1.1);
}
/* In studentDashboard.jsp, add this inside the <style> tag */

/* --- Improved Modal Button Styles --- */
.modal-header .modal-close-btn {
    border-radius: 50%; /* Makes the header close button circular */
    color: var(--text-secondary);
}
.modal-header .modal-close-btn:hover {
    background-color: var(--hover-bg);
    color: var(--text-primary);
}
.modal-footer .modal-close-btn {
    /* Styles the footer button to be a proper secondary button */
    padding: 10px 20px;
    font-size: 15px;
    font-weight: 500;
    border-radius: var(--radius-small);
    background-color: var(--hover-bg);
    color: var(--text-secondary);
    border: 1px solid var(--border-color);
}
.modal-footer .modal-close-btn:hover {
    background-color: var(--border-color);
    color: var(--text-primary);
}
/* In studentDashboard.jsp, add this inside the <style> tag */

.btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    padding: 10px 20px;
    font-size: 15px;
    border-radius: var(--radius-small); /* Creates the rounded rectangular shape */
    transition: all 0.2s ease;
    text-decoration: none;
    justify-content: center;
    cursor: pointer;
    border: 1px solid transparent;
}
.btn-register {
    background-color: var(--accent-blue);
    color: #fff;
}
.btn-register:hover {
    filter: brightness(1.1);
    transform: scale(1.03);
}
/* In studentDashboard.jsp, add this inside the <style> tag */

/* --- Final Modal Text and Design Styles --- */
.meta-item {
    display: flex;
    flex-direction: column; /* Stack label and value vertically */
    align-items: flex-start;
    gap: 4px;
    font-size: 14px;
}
.meta-item .meta-label {
    font-size: 12px;
    font-weight: 500;
    color: var(--text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
}
.meta-item .meta-value {
    font-size: 15px;
    font-weight: 500;
    color: var(--text-primary);
}
/* Special design for the "Register by" date */
#modal-event-reg-date-item .meta-value {
    font-weight: 600;
    color: var(--accent-orange);
}
#modal-event-description {
    white-space: pre-wrap; /* This is the magic property */
    word-wrap: break-word; /* Ensures long words don't overflow */
}
</style>
</head>
<body class="dark-mode">

<div class="app-container">
    <header class="app-header">
        <div class="header-logo"><img src="${pageContext.request.contextPath}/New_Horizon_College_of_Engineering_logo.png" alt="NHCE Logo"></div>
        <div class="header-actions">
            <button class="action-btn theme-toggle">
                <i class='bx bx-sun'></i>
                <i class='bx bx-moon'></i>
            </button>
            <div class="profile-menu-wrapper">
                <button class="action-btn profile-avatar-btn">
                    <img src="https://i.pravatar.cc/44?u=${fn:replace(dashboard.studentName, ' ', '')}" alt="Avatar" class="profile-avatar">
                </button>
                <div class="profile-dropdown">
                    <div class="profile-dropdown-item nav-link" data-target="profile-view">
                        <i class='bx bxs-user-circle'></i>
                        <span>View Profile</span>
                    </div>
                    <div class="profile-dropdown-divider"></div>
                    <a href="${pageContext.request.contextPath}/logout" class="profile-dropdown-item logout">
                        <i class='bx bxs-log-out'></i>
                        <span>Logout</span>
                    </a>
                </div>
            </div>
        </div>
    </header>

    <main class="content-area">
        <div class="content-scroll-wrapper">
            
            <div id="dashboard-home" class="view is-active">
                 <div class="welcome-header">
                    <h1>Welcome, <c:out value="${dashboard.studentName}"/>!</h1>
                    <br>
                    <br>
                </div>
                <div class="home-layout-grid">
                    <div class="home-col-left">
                        <div class="widget">
                            <div class="widget-header"><i class='bx bxs-star'></i><span>Today's Highlights</span></div>
                            <ul class="data-list">
                                <li class="data-list-item" id="next-class-item">
                                    <div class="icon" style="background-color: var(--accent-orange);"><i class='bx bx-time-five'></i></div>
                                    <div class="content"><h4>Next Class: <span id="next-class-subject">N/A</span></h4><p class="meta" id="next-class-time">No class scheduled</p></div>
                                </li>
                                <li class="data-list-item">
                                    <div class="icon" style="background-color: var(--accent-green);"><i class='bx bxs-pie-chart-alt-2'></i></div>
                                    <div class="content"><h4>Overall Attendance</h4></div>
                                    <span class="trailing"><fmt:formatNumber value="${dashboard.overallAttendance.overallPercentage}" maxFractionDigits="0"/>%</span>
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
                                <div><h3>Marks & Grades</h3><p>Check recent scores by semester</p></div>
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
                				<ul class="data-list" id="upcoming-events-list">
                    			<div style="text-align: center; color: var(--text-secondary); padding: 10px;" id="events-placeholder">
                        		<p>No upcoming events.</p>
                    			</div>
                				</ul>
                        </div>
                    </div>
                </div>
            </div>
            
            <div id="profile-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>My Profile</h2></div>
                <div class="widget">
                    <div class="profile-avatar-large-wrapper">
                        <img src="https://i.pravatar.cc/110?u=${fn:replace(dashboard.studentName, ' ', '')}" alt="Student Avatar" class="profile-avatar-large">
                    </div>
                    <div class="form-group">
                        <label for="studentName">Full Name</label>
                        <input type="text" id="studentName" class="form-control" value="<c:out value="${dashboard.studentName}"/>" readonly>
                    </div>
                    <div class="form-group">
                        <label for="studentId">Student ID</label>
                        <input type="text" id="studentId" class="form-control" value="<c:out value="${dashboard.studentId}"/>" readonly>
                    </div>
                </div>
            </div>

          <div id="attendance-view" class="view">
    <div class="view-header">
        <button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button>
        <h2>Attendance</h2>
    </div>

    <div class="semester-filter-wrapper">
        <label for="attendance-semester-filter">Semester:</label>
        <select id="attendance-semester-filter" class="semester-filter" data-view-target="attendance-view">
            <c:forEach var="entry" items="${dashboard.performanceBySemester}">
                <option value="${entry.key}" ${entry.key == dashboard.currentSemester ? 'selected' : ''}>
                    Semester ${entry.key}
                </option>
            </c:forEach>
        </select>
    </div>

    <c:forEach var="entry" items="${dashboard.performanceBySemester}">
        <div class="semester-content" data-view="attendance-view" data-semester="${entry.key}">
            <div class="attendance-grid">
                <c:forEach var="perf" items="${entry.value}">
                     <div class="attendance-card">
                         <h4><c:out value="${perf.subjectName}"/></h4>
                         <div class="attendance-bar-container">
                            <div class="attendance-bar" data-percentage="<fmt:formatNumber value='${perf.attendancePercentage}' maxFractionDigits='2'/>"></div>
                         </div>
                         <div class="attendance-meta">
                            <span><c:out value="${perf.classesAttended}"/> / <c:out value="${perf.totalClassesHeld}"/> classes</span>
                            <span><fmt:formatNumber value="${perf.attendancePercentage}" maxFractionDigits="0"/>%</span>
                         </div>
                     </div>
                </c:forEach>
            </div>
        </div>
    </c:forEach>
</div>

               <div id="marks-view" class="view">
    <div class="view-header">
        <button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button>
        <h2>Marks & Grades</h2>
    </div>

    <div class="semester-filter-wrapper">
        <label for="marks-semester-filter">Semester:</label>
        <select id="marks-semester-filter" class="semester-filter" data-view-target="marks-view">
           <c:forEach var="entry" items="${dashboard.performanceBySemester}">
                <option value="${entry.key}" ${entry.key == dashboard.currentSemester ? 'selected' : ''}>Semester ${entry.key}</option>
            </c:forEach>
        </select>
    </div><br><br>
 

                    <c:forEach var="entry" items="${dashboard.performanceBySemester}">
                        <div class="semester-content" data-view="marks-view" data-semester="${entry.key}">
                            <div class="widget">
                                <div class="exam-filter-container">
                                    <button class="exam-filter-btn active" data-exam-target="cie1">CIE-1</button>
                                    <button class="exam-filter-btn" data-exam-target="cie2">CIE-2</button>
                                    <button class="exam-filter-btn" data-exam-target="see">Final (SEE)</button>
                                </div>

                                <div class="exam-content is-visible" data-exam-type="cie1">
                                <div class="table-scroll-wrapper">
                                    <table class="marks-table">
                                        <thead><tr><th>Course</th><th>Marks Obtained</th><th>Total Marks</th><th>Percentage</th></tr></thead>
                                        <tbody>
                                            <c:forEach var="perf" items="${entry.value}">
                                                <tr>
                                                    <td><c:out value="${perf.subjectName}"/></td>
                                                    <td><c:out value="${not empty perf.ia1Marks ? perf.ia1Marks : 'N/A'}"/></td>
                                                    <td><c:out value="${not empty perf.ia1Marks ? perf.maxCieMarks : 'N/A'}"/></td>
                                                    <td><c:if test="${not empty perf.cie1Percentage}"><fmt:formatNumber value="${perf.cie1Percentage}" maxFractionDigits="2"/>%</c:if><c:if test="${empty perf.cie1Percentage}">N/A</c:if></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                  </div>
                                </div>

                                <div class="exam-content" data-exam-type="cie2">
                                <div class="table-scroll-wrapper">
                                    <table class="marks-table">
                                        <thead><tr><th>Course</th><th>Marks Obtained</th><th>Total Marks</th><th>Percentage</th></tr></thead>
                                        <tbody>
                                            <c:forEach var="perf" items="${entry.value}">
                                                <tr>
                                                    <td><c:out value="${perf.subjectName}"/></td>
                                                    <td><c:out value="${not empty perf.ia2Marks ? perf.ia2Marks : 'N/A'}"/></td>
                                                    <td><c:out value="${not empty perf.ia2Marks ? perf.maxCieMarks : 'N/A'}"/></td>
                                                    <td><c:if test="${not empty perf.cie2Percentage}"><fmt:formatNumber value="${perf.cie2Percentage}" maxFractionDigits="2"/>%</c:if><c:if test="${empty perf.cie2Percentage}">N/A</c:if></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                    </div>
                                </div>

                                <div class="exam-content" data-exam-type="see">
                                <div class="table-scroll-wrapper">
                                    <table class="marks-table">
                                        <thead><tr><th>Course</th><th>Combined CIE</th><th>SEE Marks</th><th>Total</th><th>Percentage</th></tr></thead>
                                        <tbody>
                                            <c:forEach var="perf" items="${entry.value}">
                                                <tr>
                                                    <td><c:out value="${perf.subjectName}"/></td>
                                                    <td><c:out value="${not empty perf.combinedCieMarks ? perf.combinedCieMarks : 'N/A'}"/> / <fmt:formatNumber value="${perf.maxCieMarks}" maxFractionDigits="0"/></td>
                                                    <td><c:out value="${not empty perf.seeMarks ? perf.seeMarks : 'N/A'}"/> / <fmt:formatNumber value="${perf.maxSeeMarks}" maxFractionDigits="0"/></td>
                                                    <td><c:out value="${not empty perf.finalTotalMarks ? perf.finalTotalMarks : 'N/A'}"/> / <fmt:formatNumber value="${perf.finalMaxMarks}" maxFractionDigits="0"/></td>
                                                    <td><c:if test="${not empty perf.finalPercentage}"><fmt:formatNumber value="${perf.finalPercentage}" maxFractionDigits="2"/>%</c:if><c:if test="${empty perf.finalPercentage}">N/A</c:if></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                        </div>
                    </c:forEach>
                </div>
                
      

            
            <div id="schedule-view" class="view">
                <div class="view-header"><button class="back-button action-btn"><i class='bx bx-arrow-back'></i></button><h2>Today's Schedule</h2></div>
                <div class="widget">
                    <div class="widget-header"><i class='bx bxs-calendar'></i><span id="schedule-header-date">Classes for <fmt:formatDate value="<%= new java.util.Date() %>" pattern="MMM d, yyyy"/></span></div>
                    <ul class="data-list" id="todays-schedule-list">
                         <div style="text-align: center; color: var(--text-secondary); padding: 20px;">
                             <p>Loading schedule...</p>
                         </div>
                    </ul>
                </div>
            </div>
        </div>
    </main>
</div>
<div class="modal-overlay" id="event-modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="modal-event-title">Event Title</h3>
            <button class="action-btn modal-close-btn"><i class='bx bx-x'></i></button>
        </div>
        <div class="modal-body">
            <p id="modal-event-description">Event description will go here...</p>
            <div class="event-meta-grid">
                <div class="meta-item" id="modal-event-date-item">
                    <span class="meta-label">Event Date</span>
                    <span class="meta-value" id="modal-event-date"></span>
                </div>
                <div class="meta-item" id="modal-event-time-item">
                    <span class="meta-label">Time</span>
                    <span class="meta-value" id="modal-event-time"></span>
                </div>
                <div class="meta-item" id="modal-event-reg-date-item">
                    <span class="meta-label">Registration</span>
                    <span class="meta-value" id="modal-event-reg-date"></span>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <a href="#" target="_blank" class="btn btn-register" id="modal-event-link">
                <i class='bx bx-link-external'></i> Register 
            </a>
        </div>
    </div>
</div>
<script>
    window.studentProgramId = '<c:out value="${dashboard.programId}"/>';
    window.studentSemester = '<c:out value="${dashboard.currentSemester}"/>';
    window.serverDate = '<fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy-MM-dd"/>';
</script>
<script src="${pageContext.request.contextPath}/js/student.js"></script>

</body>
</html>
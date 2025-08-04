<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Host a New Event</title>
    <link href="https://cdn.jsdelivr.net/npm/boxicons@2.1.4/css/boxicons.min.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <style>
        /* Sea Blue Color Schema */
        :root {
            --body-color: #f5f7fa;
            --panel-color: #ffffff;
            --primary-color: #2E86C1;
            --primary-color-rgb: 46, 134, 193;
            --primary-color-light: #D6EAF8;
            --success-color: #28a745;
            --danger-color: #dc3545;
            --text-color: #222537;
            --text-color-light: #7a7a8c;
            --border-color: #dfe7ef;
            --input-background: #ffffff;
            --toggle-color: #f0f0f0;
            --card-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            --tran-03: all 0.3s ease;
        }

        /* General Body and Container */
        body {
            min-height: 100vh;
            background-color: var(--body-color);
            font-family: 'Poppins', sans-serif;
            margin: 0;
            padding: 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .container {
            width: 100%;
            max-width: 800px;
            margin: 20px auto;
            background: var(--panel-color);
            padding: 25px 30px;
            border-radius: 18px;
            box-shadow: var(--card-shadow);
        }
        #pageTitle {
            font-size: 2.2rem;
            font-weight: 700;
            margin-bottom: 25px;
            color: var(--primary-color);
            text-align: center;
        }

        /* Buttons */
        .btn {
            display: inline-flex; align-items: center; gap: 8px; font-weight: 600;
            padding: 0.75rem 1.5rem; font-size: 1rem; border-radius: 10px;
            transition: all var(--tran-03); text-decoration: none; justify-content: center;
            cursor: pointer; border: 1px solid transparent;
        }
        .btn-primary {
            color: white; background-color: var(--primary-color); border-color: var(--primary-color);
            box-shadow: 0 4px 12px rgba(var(--primary-color-rgb), 0.2);
        }
        .btn-primary:hover { filter: brightness(1.1); transform: translateY(-2px); box-shadow: 0 6px 16px rgba(var(--primary-color-rgb), 0.3); }
        .btn-secondary {
            color: var(--text-color); background-color: var(--toggle-color); border-color: var(--border-color);
        }
        .btn-secondary:hover { background-color: #e9ecef; }
        .btn-back { margin-bottom: 20px; align-self: flex-start; }
        .btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; box-shadow: none; }

        /* Form Controls */
        .form-group { margin-bottom: 20px; }
        .form-label { display: block; font-weight: 500; color: var(--text-color); margin-bottom: 8px; }
        .form-control {
            display: block; width: 100%; padding: 12px 15px; font-size: 1rem;
            color: var(--text-color); background-color: var(--input-background);
            border: 1px solid var(--border-color); border-radius: 8px;
            transition: border-color var(--tran-03), box-shadow var(--tran-03);
            box-sizing: border-box;
        }
        .form-control:focus { outline: none; border-color: var(--primary-color); box-shadow: 0 0 0 3px rgba(var(--primary-color-rgb), 0.2); }
        .form-control::placeholder { color: var(--text-color-light); opacity: 0.7; }

        /* === NEW: Styles for Input Group (URL field) === */
        .input-group {
            display: flex;
            align-items: stretch;
            width: 100%;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            transition: border-color var(--tran-03), box-shadow var(--tran-03);
            overflow: hidden; /* Ensures child elements respect the parent's border-radius */
        }
        /* Style the entire group on focus */
        .input-group:focus-within {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(var(--primary-color-rgb), 0.2);
        }
        .input-group-text {
            display: flex;
            align-items: center;
            padding: 0 12px;
            font-size: 1rem;
            font-weight: 400;
            color: var(--text-color-light);
            background-color: var(--body-color);
            border-right: 1px solid var(--border-color);
        }
        /* Remove individual borders from the input inside the group */
        .input-group .form-control {
            flex: 1 1 auto;
            width: 1%;
            min-width: 0;
            border: none;
            border-radius: 0;
            box-shadow: none;
        }
        .input-group .form-control:focus {
            box-shadow: none; /* Also remove focus shadow from the input itself */
        }
        
        /* Form Layout Helpers */
        .form-row { display: flex; gap: 20px; }
        .form-row > .form-group { flex: 1; }
        .form-buttons { display: flex; justify-content: flex-end; gap: 15px; margin-top: 30px; }

        /* Event Type Category Cards */
        .event-category-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
            gap: 15px;
            margin-bottom: 20px;
        }
        .category-card {
            display: flex; flex-direction: column; align-items: center; justify-content: center;
            padding: 20px 10px; background: var(--body-color); border-radius: 12px;
            cursor: pointer; border: 2px solid transparent; 
            text-align: center;
            transition: all 0.25s ease;
        }
        .category-card:hover { transform: translateY(-5px); box-shadow: var(--card-shadow); }
        .category-card.selected { 
            border-color: var(--primary-color); background: var(--primary-color-light);
            transform: translateY(-5px) scale(1.05); box-shadow: 0 8px 20px rgba(var(--primary-color-rgb), 0.2);
        }
        .event-category-grid.selection-active .category-card:not(.selected) {
            opacity: 0.6; filter: grayscale(50%); transform: scale(0.98);
        }
        .category-card i { font-size: 2.5rem; color: var(--text-color); margin-bottom: 10px; transition: color 0.25s; }
        .category-card.selected i { color: var(--primary-color); }
        .category-card p { font-size: 0.9rem; font-weight: 500; color: var(--text-color); margin: 0; }
        
        /* Submission Dialog */
        .submission-dialog-overlay {
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background-color: rgba(0, 0, 0, 0.6); display: flex; justify-content: center;
            align-items: center; z-index: 1000; opacity: 0; visibility: hidden;
            transition: opacity 0.3s ease, visibility 0.3s ease;
        }
        .submission-dialog-overlay.show { opacity: 1; visibility: visible; }
        .submission-dialog-content {
            background: var(--panel-color); padding: 30px; border-radius: 10px;
            box-shadow: var(--card-shadow); text-align: center; max-width: 400px;
            width: 90%; transform: translateY(-50px); opacity: 0;
            transition: transform 0.3s ease, opacity 0.3s ease;
        }
        .submission-dialog-overlay.show .submission-dialog-content { transform: translateY(0); opacity: 1; }
        .submission-dialog-content i { font-size: 3.5rem; margin-bottom: 15px; }
        .submission-dialog-content .success-icon { color: var(--success-color); }
        .submission-dialog-content .error-icon { color: var(--danger-color); }
        .submission-dialog-content h3 { margin-bottom: 10px; font-size: 1.8rem; color: var(--text-color); }
        .submission-dialog-content p { margin-bottom: 25px; color: var(--text-color-light); }

        /* Responsive */
        @media (max-width: 768px) {
            body { padding: 10px 0; }
            .container { width: 95%; margin: 10px auto; padding: 20px 15px; }
            #pageTitle { font-size: 1.8rem; }
            .form-row { flex-direction: column; gap: 0; }
            .event-category-grid { grid-template-columns: repeat(3, 1fr); }
            .category-card { padding: 15px 5px; }
            .category-card i { font-size: 2rem; }
            .form-buttons { flex-direction: column; gap: 10px; }
            .form-buttons .btn { width: 100%; }
        }
        @media (max-width: 480px) {
            body { padding: 5px 0; }
            .container { padding: 15px 10px; }
            #pageTitle { font-size: 1.6rem; }
            .event-category-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
        }
    </style>
</head>
<body>
    <script>
        window.currentFacultyId = "<c:out value='${sessionScope.user.id}'/>";
    </script>

    <div class="container">
        <button onclick="window.history.back()" class="btn btn-primary btn-back">
            <i class='bx bx-arrow-back'></i> Back to Dashboard
        </button>

        <h2 id="pageTitle">Host a New Event</h2>

        <form id="createEventForm">
            <input type="hidden" id="eventCategory" name="eventCategory" required>
            <div class="form-group">
                <label class="form-label">Event Type</label>
                <div class="event-category-grid">
                    <div class="category-card" data-category="College Event"><i class='bx bxs-institution'></i><p>College Event</p></div>
                    <div class="category-card" data-category="Fest"><i class='bx bxs-party'></i><p>Fest</p></div>
                    <div class="category-card" data-category="Department Based"><i class='bx bxs-building-house'></i><p>Department</p></div>
                    <div class="category-card" data-category="National"><i class='bx bx-globe'></i><p>National</p></div>
                    <div class="category-card" data-category="Sports"><i class='bx bxs-basketball'></i><p>Sports</p></div>
                    <div class="category-card" data-category="Other"><i class='bx bx-dots-horizontal-rounded'></i><p>Other</p></div>
                </div>
            </div>
            <div class="form-group">
                <label for="eventName" class="form-label">Event Title</label>
                <input type="text" id="eventName" name="eventName" class="form-control" required placeholder="e.g., Tech-Fest 2025: Innovate with AI">
            </div>
            <div class="form-group">
                <label for="eventDescription" class="form-label">Description</label>
                <textarea id="eventDescription" name="eventDescription" class="form-control" rows="4" required placeholder="Provide a detailed description of the event..."></textarea>
            </div>
            <div class="form-group">
                <label for="eventLink" class="form-label">Registration/Info Link (Optional)</label>
                <div class="input-group">
                    <span class="input-group-text">https://</span>
                    <input type="text" id="eventLink" name="eventLink" class="form-control" placeholder="college.events/register">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label for="eventDate" class="form-label">Event Date</label>
                    <input type="date" id="eventDate" name="eventDate" class="form-control" required>
                </div>
                <div class="form-group">
                    <label for="eventTime" class="form-label">Event Time (Optional)</label>
                    <input type="time" id="eventTime" name="eventTime" class="form-control">
                </div>
            </div>
            <div class="form-group">
                <label for="registrationEndDate" class="form-label">Registration End Date (Optional)</label>
                <input type="date" id="registrationEndDate" name="registrationEndDate" class="form-control">
            </div>
            <div class="form-buttons">
                <button type="button" id="cancelEventBtn" class="btn btn-secondary">Clear Form</button>
                <button type="submit" class="btn btn-primary">Host Event</button>
            </div>
        </form>
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
        function qs(selector) { return document.querySelector(selector); }
        
        function showSubmissionDialog(isSuccess, message) {
            const overlay = qs('#submissionDialogOverlay');
            qs('#dialogIcon').innerHTML = isSuccess ? '<i class="bx bx-check-circle success-icon"></i>' : '<i class="bx bx-x-circle error-icon"></i>';
            qs('#dialogTitle').textContent = isSuccess ? 'Success!' : 'Error!';
            qs('#dialogMessage').textContent = message;
            overlay.classList.add('show');
            qs('#dialogCloseBtn').onclick = () => overlay.classList.remove('show');
        }

        document.addEventListener('DOMContentLoaded', function() {
            const form = qs('#createEventForm');
            const submitBtn = form.querySelector('button[type="submit"]');
            const categoryGrid = qs('.event-category-grid');
            const eventDateInput = qs('#eventDate');
            const regEndDateInput = qs('#registrationEndDate');
            
            const today = new Date().toISOString().split("T")[0]; 
            eventDateInput.setAttribute('min', today);
            regEndDateInput.setAttribute('min', today);

            eventDateInput.addEventListener('change', function() {
                if (this.value) {
                    regEndDateInput.setAttribute('max', this.value);
                    if (regEndDateInput.value > this.value) {
                        regEndDateInput.value = '';
                    }
                }
            });

            function resetFormState() {
                form.reset();
                categoryGrid.classList.remove('selection-active');
                document.querySelectorAll('.category-card').forEach(c => c.classList.remove('selected'));
            }

            document.querySelectorAll('.category-card').forEach(card => {
                card.addEventListener('click', () => {
                    categoryGrid.classList.add('selection-active');
                    document.querySelectorAll('.category-card').forEach(c => c.classList.remove('selected'));
                    card.classList.add('selected');
                    qs('#eventCategory').value = card.dataset.category;
                });
            });

            qs('#cancelEventBtn').addEventListener('click', resetFormState);

            form.addEventListener('submit', function(e) {
                e.preventDefault();
                
                if (!qs('#eventCategory').value) {
                    showSubmissionDialog(false, "Please select an Event Type.");
                    return;
                }
                const formData = new FormData(form);
                const data = Object.fromEntries(formData.entries());
                
                // Prepend https:// to the link if it exists
                if (data.eventLink) {
                    data.eventLink = 'https://' + data.eventLink;
                }

                data.facultyId = window.currentFacultyId;
                
                submitBtn.disabled = true;
                submitBtn.innerHTML = "<i class='bx bx-loader-alt bx-spin'></i> Hosting...";
                
                fetch('add-event-servlet', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        showSubmissionDialog(true, 'Event hosted successfully!');
                        resetFormState();
                    } else {
                        showSubmissionDialog(false, 'Failed to host event: ' + (result.message || 'Unknown server error.'));
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showSubmissionDialog(false, 'An error occurred. Please check your connection and try again.');
                })
                .finally(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = "Host Event";
                });
            });
        });
    </script>
</body>
</html>
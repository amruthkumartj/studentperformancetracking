<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Approve Faculty</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    
<script src="<%= request.getContextPath() %>/js/theme.js"></script>
    <style>
      body {
            font-family: 'Poppins', sans-serif;
            padding: 20px;
        }
       
        .container {
            max-width: 900px;
        }
        .message-box {
            padding: 1rem;
            border-radius: 0.5rem;
            margin-bottom: 1rem;
            font-weight: 500;
        }
        .message-success {
            background-color: #d1fae5; /* green-100 */
            color: #065f46; /* green-800 */
            border: 1px solid #34d399; /* green-400 */
        }
        .message-error {
            background-color: #fee2e2; /* red-100 */
            color: #991b1b; /* red-800 */
            border: 1px solid #ef4444; /* red-500 */
        }
        /* New style for form within table cell */
        .action-cell {
            min-width: 280px; /* Adjust as needed to fit the form elements */
        }
        .form-group {
            margin-bottom: 0.5rem;
        }
        .form-group label {
            display: block;
            font-size: 0.875rem; /* text-sm */
            font-weight: 500;
            color: #4b5563; /* gray-700 */
            margin-bottom: 0.25rem;
        }
        .form-group input[type="text"],
        .form-group select {
            width: 100%;
            padding: 0.5rem;
            border: 1px solid #d1d5db; /* gray-300 */
            border-radius: 0.375rem; /* rounded-md */
            font-size: 0.875rem;
            color: #374151; /* gray-700 */
        }
        .form-group select[multiple] {
            height: 100px; /* Adjust height for multi-select */
        }
        .form-group button {
            width: 100%;
            margin-top: 0.75rem;
        }
          body.dark {
        background-color: #18191a;
        color: #ccc;
    }

    /* Main container and text */
    body.dark .container {
        background-color: #242526;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
    }
    body.dark h1 { /* Main Page Title */
        color: #81a4ff; /* Unified blue accent color */
    }
    body.dark h2 { /* Subtitle like "Pending Faculty" */
        color: #f0f0f0; /* Set to white/light-gray */
    }
    body.dark p,
    body.dark a,
    body.dark .text-gray-600 {
        color: #a0a0b0;
    }
    body.dark a:hover {
        color: #81a4ff;
    }
    
    /* Table styles */
    body.dark .min-w-full { /* Targets the table */
        background-color: #242526;
        border-color: #3a3b3c;
    }
    body.dark thead { /* Table header */
        background-color: rgba(129, 164, 255, 0.05);
    }
    body.dark th { /* Table header cells */
        color: #f0f0f0; /* Set to white/light-gray */
        border-bottom-color: #3a3b3c;
    }
    body.dark td { /* Table data cells */
        border-color: #3a3b3c;
        color: #ccc;
    }
    body.dark tbody tr:hover { /* Table row hover */
        background-color: rgba(129, 164, 255, 0.08);
    }

    /* Form elements inside the table */
    body.dark .form-group label {
        color: #ccc;
    }
    body.dark .form-group input,
    body.dark .form-group select {
        background-color: #18191a;
        border-color: #3a3b3c;
        color: #ccc;
    }
    body.dark .form-group select option {
        background-color: #18191a; /* Ensure options are readable */
        color: #ccc;
    }
    body.dark .form-group input:focus,
    body.dark .form-group select:focus {
        border-color: #81a4ff;
        box-shadow: 0 0 0 2px rgba(129, 164, 255, 0.3);
    }

    /* Message Boxes */
    body.dark .message-success {
        background-color: rgba(40, 167, 69, 0.2);
        border-color: #28a745;
        color: #81c784;
    }
    body.dark .message-error {
        background-color: rgba(220, 53, 69, 0.2);
        border-color: #dc3545;
        color: #e57373;
    }
    </style>
</head>
<body class="bg-gray-100 min-h-screen flex flex-col items-center py-8 ${sessionScope.theme}">
    <div class="container bg-white p-8 rounded-lg shadow-xl w-full">
        <h1 class="text-3xl font-bold text-gray-800 mb-6 text-center">Approve Faculty Accounts</h1>
        <br>
        <br>

        <c:if test="${not empty sessionScope.message}">
            <div class="message-box message-success">
                <p>${sessionScope.message}</p>
            </div>
            <c:remove var="message" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.error}">
            <div class="message-box message-error">
                <p>${sessionScope.error}</p>
            </div>
            <c:remove var="error" scope="session"/>
        </c:if>

        

        <c:choose>
            <c:when test="${empty pendingFaculty}">
                <p class="text-gray-600 text-center py-8">No faculty accounts are pending approval at this time.</p>
            </c:when>
            <c:otherwise>
                <div class="overflow-x-auto">
                    <table class="min-w-full bg-white border border-gray-300 rounded-lg shadow-sm">
                        <thead class="bg-gray-200">
                            <tr>
                                <th class="py-3 px-4 text-left text-sm font-semibold text-gray-700 border-b">User ID</th>
                                <th class="py-3 px-4 text-left text-sm font-semibold text-gray-700 border-b">Username</th>
                                <th class="py-3 px-4 text-left text-sm font-semibold text-gray-700 border-b">Role</th>
                                <th class="py-3 px-4 text-left text-sm font-semibold text-gray-700 border-b action-cell">Action / Details</th> <%-- Updated header --%>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="faculty" items="${pendingFaculty}">
                                <tr class="hover:bg-gray-50">
                                    <td class="py-3 px-4 border-b border-gray-200 text-gray-800">${faculty.id}</td>
                                    <td class="py-3 px-4 border-b border-gray-200 text-gray-800">${faculty.username}</td>
                                    <td class="py-3 px-4 border-b border-gray-200 text-gray-800">${faculty.role}</td>
                                    <td class="py-3 px-4 border-b border-gray-200 action-cell"> <%-- Apply min-width style to this cell --%>
                                        <form action="<c:url value='/admin/approveFaculty' />" method="post" class="inline-block w-full">
                                            <input type="hidden" name="userId" value="${faculty.id}">
                                            <input type="hidden" name="action" value="approve">

                                            <div class="form-group">
                                                <label for="designation-${faculty.id}">Designation:</label>
                                                <input type="text" id="designation-${faculty.id}" name="designation"
                                                       class="form-input" required placeholder="e.g., Professor">
                                            </div>

                                            <div class="form-group">
                                                <label for="programIds-${faculty.id}">Assign Programs (Ctrl+click to select multiple):</label>
                                                <select id="programIds-${faculty.id}" name="programIds" multiple required class="form-select">
                                                    <c:forEach var="program" items="${allPrograms}">
                                                        <%-- program[0] is programId, program[1] is programName --%>
                                                        <option value="${program[0]}">${program[1]}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>

                                            <button type="submit" class="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300 ease-in-out transform hover:scale-105">
                                                Approve Faculty
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="mt-8 text-center">
            <a href="<c:url value='/facultydashboard' />" class="text-blue-600 hover:text-blue-800 font-semibold transition duration-300 ease-in-out">
                &larr; Back to Admin Dashboard
            </a>
        </div>
    </div>
</body>
</html>
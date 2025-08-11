package com.portal.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ThemeServlet")
public class ThemeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

 // In ThemeServlet.java

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String theme = request.getParameter("theme");

        // 1. First, check if the 'theme' parameter from the JavaScript is valid.
        // If it's missing or incorrect, then it's a true bad request.
        if (theme == null || (!theme.equals("dark") && !theme.equals("light"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return; 
        }

        // 2. Try to get the current session. It's okay if it's null (e.g., on the login page).
        HttpSession session = request.getSession(false); 

        // 3. IMPORTANT: Only try to set the session attribute if the session actually exists.
        if (session != null) {
            session.setAttribute("theme", theme);
        }
        
        // 4. Always return a success code if the theme parameter was valid.
        // This will stop the red error from appearing in your browser console on the login page.
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
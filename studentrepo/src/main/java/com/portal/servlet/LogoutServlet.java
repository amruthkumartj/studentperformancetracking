package com.portal.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get the current session, but don't create a new one if it doesn't exist
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Invalidate the session, removing all attributes (like the 'user' object)
            session.invalidate();
        }
        
        // Redirect the user to the login page
        response.sendRedirect(request.getContextPath() + "/login.html");
    }
}
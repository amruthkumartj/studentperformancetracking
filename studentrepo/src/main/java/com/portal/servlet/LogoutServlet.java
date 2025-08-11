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
          
          // 1. Invalidate the current user's session
          if (session != null) {
              session.invalidate();
          }
          
          // 2. Redirect back to the login page with a status parameter
          // This parameter tells our login page JavaScript that the user just logged out.
          String loginPage = request.getContextPath() + "/login.html?status=logged_out";
          response.sendRedirect(loginPage);
      }
}
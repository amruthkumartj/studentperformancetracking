package com.portal.servlet; // Or your servlets package

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.portal.User; // Make sure your User class is imported

// This filter applies ONLY to the login page.

public class LoginAccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
    	 System.out.println("--- LOGIN ACCESS FILTER TRIGGERED! ---");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false); // Do not create a new session

        // Check if the user IS already logged in
        if (session != null && session.getAttribute("user") != null) {
            // User is already logged in, so they should not see the login page.
            // Redirect them to their appropriate dashboard.
            User user = (User) session.getAttribute("user");
            String role = user.getRole();
            String dashboardUrl = httpRequest.getContextPath();

            if ("STUDENT".equalsIgnoreCase(role)) {
                dashboardUrl += "/studentdashboard";
            } else { // FACULTY and ADMIN go to the faculty dashboard
                dashboardUrl += "/facultydashboard";
            }
            
            System.out.println("LoginAccessFilter: User already logged in. Redirecting to " + dashboardUrl);
            httpResponse.sendRedirect(dashboardUrl);
            
        } else {
            // User is not logged in, so allow them to see the login page.
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
package com.portal.servlet;

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


public class AuthenticationFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        // Check if the user is logged in
        if (isLoggedIn) {
            // User is authenticated, so let the request proceed to the dashboard.
            chain.doFilter(request, response);
        } else {
            // User is not logged in.
            // Invalidate the session just in case, and redirect to the login page.
            if (session != null) {
                session.invalidate();
            }
            String loginURI = httpRequest.getContextPath() + "/login.html";
            httpResponse.sendRedirect(loginURI);
        }
    }

    public void init(FilterConfig fConfig) throws ServletException { }

    public void destroy() { }
}
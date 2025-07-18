package com.portal.servlet;

import com.google.gson.JsonObject;
import com.portal.EmailUtil;
import com.portal.User;
import com.portal.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Random;

@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        HttpSession session = request.getSession();

        String action = request.getParameter("action");
        UserDAO userDAO = new UserDAO();

        if ("sendOTP".equals(action)) {
            String email = request.getParameter("email");
            try {
                User user = userDAO.getUserByEmail(email);
                if (user != null) {
                    String otp = generateOTP();
                    session.setAttribute("otp", otp);
                    session.setAttribute("otpEmail", email);
                    session.setAttribute("otpTimestamp", System.currentTimeMillis());

                    EmailUtil.sendOtpEmail(email, otp);

                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "OTP sent to your email address.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "No account found with that email address.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Database error. Please try again later.");
            }
        } else if ("verifyOTP".equals(action)) {
            String otp = request.getParameter("otp");
            String sessionOtp = (String) session.getAttribute("otp");
            Long otpTimestamp = (Long) session.getAttribute("otpTimestamp");

            if (sessionOtp != null && otpTimestamp != null && sessionOtp.equals(otp)) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - otpTimestamp) <= 10 * 60 * 1000) { // 10 minutes
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "OTP verified successfully.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "OTP has expired. Please request a new one.");
                }
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid OTP. Please try again.");
            }
        } else if ("resetPassword".equals(action)) {
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");
            String otpEmail = (String) session.getAttribute("otpEmail");

            if (newPassword != null && newPassword.equals(confirmPassword)) {
                boolean updated = userDAO.updatePassword(otpEmail, newPassword);
                if (updated) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Password has been reset successfully.");
                    session.removeAttribute("otp");
                    session.removeAttribute("otpEmail");
                    session.removeAttribute("otpTimestamp");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to update password. Please try again.");
                }
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Passwords do not match.");
            }
        } else {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Invalid action.");
        }

        out.print(jsonResponse.toString());
        out.flush();
    }

    private String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
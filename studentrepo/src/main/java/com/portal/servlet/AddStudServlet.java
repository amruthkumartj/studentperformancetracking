package com.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.portal.UserDAO;

@WebServlet("/AddStudServlet")
public class AddStudServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {

            int id  = Integer.parseInt(req.getParameter("studentId"));
            int sem = Integer.parseInt(req.getParameter("semester"));

            String name   = req.getParameter("fullName");
            String course = req.getParameter("course");
            String phone  = req.getParameter("phone");
            String email  = req.getParameter("email");

            UserDAO dao = new UserDAO();

            if (dao.studentIdExists(id))   { out.print("Student ID already exists");   return; }
            if (dao.emailExists(email))    { out.print("Email already registered");   return; }
            if (dao.phoneExists(phone))    { out.print("Phone already registered");   return; }

            boolean ok = dao.addStudent(id, name, course, sem, phone, email);
            out.print(ok ? "Student added successfully"
                         : "Failed to add student. Try again.");
        }
        /* ── ALL ERRORS FALL THROUGH HERE ── */
        catch (NumberFormatException ex) {
            resp.setStatus(400);
            resp.getWriter().print("ID and Semester must be numbers");
        }
        catch (Exception ex) {            // SQLException etc.
            resp.setStatus(500);
            resp.getWriter().print("Server error: " + ex.getMessage());
        }
    }
}

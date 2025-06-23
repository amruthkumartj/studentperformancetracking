package com.portal.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.portal.DBUtil;
import com.portal.User;

@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User u = (User) req.getSession().getAttribute("user");
        if (u == null || !"STUDENT".equals(u.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        String sql = """
            SELECT c.course_code, c.course_name,
                   m.assessment, m.score, m.max_score,
                   ROUND((m.score/m.max_score)*100,2) AS percent,
                   att.present_days, att.total_days
            FROM courses c
            JOIN enrollments e ON e.course_id = c.course_id
            LEFT JOIN (
                 SELECT course_id, assessment, score, max_score
                 FROM marks WHERE student_id = ?
            ) m ON m.course_id = c.course_id
            LEFT JOIN (
                 SELECT course_id,
                        SUM(status='PRESENT') AS present_days,
                        COUNT(*) AS total_days
                 FROM attendance
                 WHERE student_id = ?
                 GROUP BY course_id
            ) att ON att.course_id = c.course_id
            WHERE e.student_id = ?;
        """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, u.getId());
            ps.setInt(2, u.getId());
            ps.setInt(3, u.getId());
            ResultSet rs = ps.executeQuery();

            req.setAttribute("rs", rs);
            req.getRequestDispatcher("/studentDashboard.jsp").forward(req, resp);

        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}

package com.portal.servlet;
import com.portal.DBUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DBTestServlet
 */
@WebServlet("/DBTestServlet")
public class DBTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DBTestServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		 response.setContentType("text/plain");

	        try (Connection conn = DBUtil.getConnection()) {
	            response.getWriter().println("✅ Database connection successful.");
	        } catch (Exception e) {
	            response.getWriter().println("❌ Database connection failed:");
	            e.printStackTrace(response.getWriter());
	}}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

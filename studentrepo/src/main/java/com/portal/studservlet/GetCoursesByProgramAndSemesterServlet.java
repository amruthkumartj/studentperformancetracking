package com.portal.studservlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject; // Import JsonObject
import com.portal.datatransfer_access.ProgramCourseDAO;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList; // Import ArrayList, though not strictly needed here
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetCoursesByProgramAndSemesterServlet")
public class GetCoursesByProgramAndSemesterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProgramCourseDAO programCourseDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        programCourseDAO = new ProgramCourseDAO();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String programIdStr = request.getParameter("programId");
        String semesterStr = request.getParameter("semester");

        System.out.println("DEBUG: GetCoursesByProgramAndSemesterServlet - Received programId: " + programIdStr + ", semester: " + semesterStr);

        if (programIdStr == null || programIdStr.isEmpty() || semesterStr == null || semesterStr.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("message", "Missing programId or semester parameter.");
            response.getWriter().write(gson.toJson(errorJson));
            System.err.println("❌ GetCoursesByProgramAndSemesterServlet - Missing parameters.");
            return;
        }

        try {
            int programId = Integer.parseInt(programIdStr);
            int semester = Integer.parseInt(semesterStr);

            // This method `getAllCourses` in ProgramCourseDAO should ideally return
            // a List<Object[]> where Object[0] is course_id (String), Object[1] is course_name (String),
            // and Object[2] is semester (Integer).
            List<Object[]> allCoursesForProgram = programCourseDAO.getAllCourses(programId);

         // Inside GetCoursesByProgramAndSemesterServlet.java, within the doPost method, in the try block:

            List<JsonObject> coursesForDropdown = allCoursesForProgram.stream()
                .filter(courseArr -> {
                    // Check if courseArr[2] (semester) is valid and matches
                    return courseArr.length > 2 && courseArr[2] instanceof Integer && (int) courseArr[2] == semester;
                })
                .map(courseArr -> {
                    JsonObject courseJson = new JsonObject();
                    // CORRECTED INDICES based on ProgramCourseDAO.getAllCourses return order
                    String courseId = (String) courseArr[0];       // This is the actual course_id (e.g., "BCA101")
                    String courseName = (String) courseArr[3];     // This is the actual course_name (e.g., "Introduction to Programming")

                    courseJson.addProperty("courseId", courseId);
                    courseJson.addProperty("courseName", courseName);
                    return courseJson;
                })
                .collect(Collectors.toList());

            response.getWriter().write(gson.toJson(coursesForDropdown));
            System.out.println("DEBUG: GetCoursesByProgramAndSemesterServlet - Sent " + coursesForDropdown.size() + " courses for programId " + programId + " and semester " + semester);

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("message", "Invalid number format for programId or semester.");
            response.getWriter().write(gson.toJson(errorJson));
            System.err.println("❌ GetCoursesByProgramAndSemesterServlet - NumberFormatException: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassCastException e) {
             // This catch block is crucial. If you see this error again,
             // it means the indices in .map(courseArr -> ...) are still incorrect
             // for the data types being returned by programCourseDAO.getAllCourses.
             System.err.println("❌ ClassCastException in GetCoursesByProgramAndSemesterServlet: Check array indices for courseId/courseName casting! " + e.getMessage());
             e.printStackTrace();
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             JsonObject errorJson = new JsonObject();
             errorJson.addProperty("message", "Error fetching courses: Data type mismatch. Please contact support.");
             response.getWriter().write(gson.toJson(errorJson));
        }
        catch (Exception e) {
            System.err.println("❌ Error in GetCoursesByProgramAndSemesterServlet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("message", "Error fetching courses: " + e.getMessage());
            response.getWriter().write(gson.toJson(errorJson));
        }
    }
}
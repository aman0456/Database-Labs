package displayGrades;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class displayGrades
 */
@WebServlet("/displayGrades")
public class displayGrades extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public displayGrades() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		HttpSession sess = request.getSession(false);
		if (sess == null) {
			response.sendRedirect("Login");
		}
		else {
			String name = (String) sess.getAttribute("id");
			
			//System.out.println("<html><body> " + name + " </body></html>");
			try (
				    Connection conn = DriverManager.getConnection(
				    		"jdbc:postgresql://localhost:5128/postgres", "labuser", "");
				    Statement stmt = conn.createStatement();
				){
				PreparedStatement stmt3 = conn.prepareStatement("select course_id, title, sec_id, semester, year, grade from takes natural join course where id = ?");
			    stmt3.setString(1, name);
			    ResultSet rs = stmt3.executeQuery();
			    //if (rs.next()) {
			    	toHTML(out, rs);
			    //}
			}
			catch (Exception sqle)
			{
				System.out.println("Exception : " + sqle);
			}	
		}
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	public static void toHTML(PrintWriter out, ResultSet rs) {
    	try {
    		
	    	out.print("<table>\n");
	    	ResultSetMetaData rsmd = rs.getMetaData();
	    	
	    	out.print("	<tr> ");
	    	for(int i=1; i<=rsmd.getColumnCount();i++) {
	    		out.print("<th>");
	    		out.print(rsmd.getColumnName(i));
	    		out.print("</th> ");
	    	}
	    	out.print("</tr>\n");
	    	
	    	while(rs.next()) {
	    		out.print("	<tr> ");
	    		for(int i=1; i<=rsmd.getColumnCount();i++) {
	        		out.print("<td>");
	        		out.print(rs.getString(i));
	        		out.print("</td> ");
	        	}
	    		out.print("</tr>\n");
	    	}
	    	out.print("</table>\n");
    	}
    	catch (Exception e) {
            e.printStackTrace();
        }
    }
}

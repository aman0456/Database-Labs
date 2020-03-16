package Home;
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
 * Servlet implementation class Home
 */
@WebServlet("/Home")
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Home() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		//String name = request.getParameter("name");
		//String pass = request.getParameter("pass");
		PrintWriter out = response.getWriter();
		HttpSession sess = request.getSession(false);
		//System.out.println(sess);
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
				PreparedStatement stmt3 = conn.prepareStatement("select * from student where id = ?");
			    stmt3.setString(1, name);
			    PreparedStatement stmt4 = conn.prepareStatement("select * from instructor where id = ?");
			    stmt4.setString(1, name);
			    ResultSet rs = stmt3.executeQuery();
				
			    if (rs.next()) {
			    	String myname = rs.getString(1);
			    	String mydept = rs.getString(2);
			    	out.print("<html><body> name : " + myname);
			    	out.print("<br> dept : " + mydept);
			    	out.println("<br><a href=\"displayGrades\"> Grades\n" + 
			    			"</body>");
			    	out.println("<form action=\"Logout\" method=\"post\">\n" + 
			    			"           <input type=\"submit\" value = \"logout\">\n" + 
			    			"       </form>\n" + 
			    			"</body>");
				}
				else {
					rs = stmt4.executeQuery();
					rs.next();
					String myname = rs.getString(1);
			    	String mydept = rs.getString(2);
			    	out.print("<html><body> name : " + myname);
			    	out.print("<br> dept : " + mydept);
			    	out.println("<form action=\"Logout\" method=\"post\">\n" + 
			    			"           <input type=\"submit\" value = \"logout\">\n" + 
			    			"       </form>\n" + 
			    			"</body>");
				}
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
	
}

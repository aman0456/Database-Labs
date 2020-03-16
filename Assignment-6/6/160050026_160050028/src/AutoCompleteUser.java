import java.io.IOException;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.List;

//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;


/**

 * Servlet implementation class AllConversations

 */

@WebServlet("/AutoCompleteUser")

public class AutoCompleteUser extends HttpServlet {

	private static final long serialVersionUID = 1L;

       

    /**

     * @see HttpServlet#HttpServlet()

     */

    public AutoCompleteUser() {

        super();

        // TODO Auto-generated constructor stub

    }


	/**

	* @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)

	*/

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ArrayNode node = new ArrayNode(null);
		String term = request.getParameter("term");
		try (Connection conn = DriverManager.getConnection(Config.url, Config.user, Config.password))
        {
            conn.setAutoCommit(false);
            String query = "select * from users where uid  like ? or  name like ? or  phone  like ?";
            try(PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, term + "%");
                stmt.setString(2, term + "%");
                stmt.setString(3, term + "%");
            	ResultSet rs = stmt.executeQuery();
            	while(rs.next()) {
            		String uid  = rs.getString(1);
            		String name = rs.getString(2);
            		String phone = rs.getString(3);
            		String label = uid+ " "+name+ " " + phone;
            		ObjectNode  topLevelJson = DbHelper.mapper.createObjectNode();
            		topLevelJson.put("label", label);
            		topLevelJson.put("value", uid);
            		node.add(topLevelJson);
            		
            	}
                //ArrayNode jsonArr = DbHelper.resultSetToJson(rs);
                //topLevelJson.putArray("data").addAll(jsonArr);
                conn.commit();
            }
            catch(Exception ex)
            {
                conn.rollback();
                throw ex;
            }
            finally{
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
//        	topLevelJson =  DbHelper.errorJson(e.getMessage());
        }
		
		response.getWriter().print(node.toString());

	}


	/**

	* @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)

	*/

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// TODO Auto-generated method stub

		doGet(request, response);

	}

	

	/**

	* For testing other methods in this class.

	*/

	public static void main(String[] args) throws ServletException, IOException {

		new AllConversations().doGet(null, null);

	}


}
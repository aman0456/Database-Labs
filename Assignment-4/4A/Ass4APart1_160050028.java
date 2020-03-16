import java.sql.*;
import java.util.*;
public class Main {
	public static void main(String[] args) {
		 
		// In the JDBC API 4.0, the DriverManager.getConnection method loads
		// JDBC drivers automatically. As a result you do not need to call the 
		// Class.forName method 
		// try {
		// 	  Class.forName ("org.postgresql.Driver");
		//  }
		//  catch (Exception e) {
		// 	System.out.println("Could not load driver: " + e);
		// }
		
	
		// The following syntax is called try with resources which can be used with any resource
		// that supports the java.lang.AutoCloseable interface. 
		// It ensures that the resources get closed at the end of the try block.  
		// It is **MUCH** preferred to the old style to avoid connection leakage.
		// Note the URL syntax below:  jdbc:postgresql tells the DriverManager to use the 
		// postgresql JDBC driver.  
		// localhost can be replaced with a host name if the postgresql is running on a remote machine.
		// Replace 6432 with the port number you are using, and dbis with your database name
		// Similarly, replace sudarsha with the user name you are using for your database.  
		//Class.forName("org.postgresql.Driver");
		Scanner sc = new Scanner(System.in);
		String id = sc.next();
		String cid = sc.next();
		String sid = sc.next();
		String sem = sc.next();
		int year = sc.nextInt();
		String grade = sc.next();
		
		try (
		    Connection conn = DriverManager.getConnection(
		    		"jdbc:postgresql://localhost:5928/postgres", "amanb", "");)
		{
			try (PreparedStatement stmt1 = conn.prepareStatement("select grade, course_id from takes where id = ? and course_id = ? and sec_id = ? and semester = ? and year = ?");
				    PreparedStatement  stmt2 = conn.prepareStatement("select tot_cred from student where id = ?");
				       ) {
			conn.setAutoCommit(false);
		          
		    stmt1.setString(1, id);
		    stmt1.setString(2, cid);
		    stmt1.setString(3, sid);
		    stmt1.setString(4, sem);
		    stmt1.setInt(5, year);
		    stmt2.setString(1, id);
		    
		    ResultSet rs1 = stmt1.executeQuery(); // can cause SQLException
            ResultSet rs2 = stmt2.executeQuery(); // can cause SQLException
            
            while (rs1.next()) {
            	Boolean flag = (rs1.getObject(1) != null);
            	if (flag) {
            		if (rs1.getString(1).equals("F")) flag = false;
            	}
            	//System.out.print(flag);
            	//System.out.print(grade);
            	String course_idd = rs1.getString(2);
            	int totc = 0;
            	while(rs2.next()) totc = rs2.getInt(1);
            	PreparedStatement stmt5 = conn.prepareStatement("select credits from course where course_id = ?");         
            	stmt5.setString(1, cid);
            	int cred = 0;
            	ResultSet rs5 = stmt5.executeQuery();
            	while(rs5.next())	cred = rs5.getInt(1);
            	PreparedStatement stmt3 = conn.prepareStatement("update takes set grade = ? where id = ? and course_id = ? and sec_id = ? and semester = ? and year = ?");         
            	stmt3.setString(1, grade);
            	stmt3.setString(2, id);
    		    stmt3.setString(3, cid);
    		    stmt3.setString(4, sid);
    		    stmt3.setString(5, sem);
    		    stmt3.setInt(6, year);
    		    stmt3.executeUpdate();
    		    if (grade.equals("F") && flag) {
    		    	PreparedStatement stmt4 = conn.prepareStatement("update student set tot_cred = ? where id = ?");         
                	stmt4.setInt(1, totc - cred);
                	stmt4.setString(2, id);
                	stmt4.executeUpdate();
        		 }
    		    if (!grade.equals("F") && !flag) {
    		    	PreparedStatement stmt6 = conn.prepareStatement("update student set tot_cred = ? where id = ?");         
                	stmt6.setInt(1, totc + cred);
                	stmt6.setString(2, id);
                	stmt6.executeUpdate();
        		 }
    		    
            }
            conn.commit();         // also can cause SQLException!
			}
			catch (Exception sqle)
			{
				conn.rollback();
				sqle.printStackTrace();
				System.out.println("Exception : " + sqle);
			}
			finally {
            conn.setAutoCommit(true);
			}
		}
		catch (Exception sqle)
		{
			sqle.printStackTrace();
			System.out.println("Exception : " + sqle);
		}
	}
}

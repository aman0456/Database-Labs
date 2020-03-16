import java.sql.*;
import java.util.Scanner;

public class Ass4APart2_160050028 {
    private static final String url = "jdbc:postgresql://localhost:5928/postgres";
    private static final String user = "amanb";
    private static final String password = "";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("ID: ");
        String i_id = sc.nextLine();
        noPrepStmt(i_id);
        //withPrepStmt(i_id);
    }

    private static void noPrepStmt(String id) {

        try (Connection conn = DriverManager.getConnection(url, user, password))
        {
            conn.setAutoCommit(false);
          //  System.out.println(id);
            try(Statement stmt = conn.createStatement()) {
                String query = "update instructordup " +
                        "set salary = salary * 1.10 " +
                        "where id = '" + id + "'";
                stmt.executeUpdate(query);
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
            e.printStackTrace();
        }
    }

    private static void withPrepStmt(String id) {
    	try (
    		    Connection conn = DriverManager.getConnection(
    		    		"jdbc:postgresql://localhost:5928/postgres", "amanb", "");)
    		{
    			try (PreparedStatement stmt1 = conn.prepareStatement("update instructordup set salary = salary * 1.10 where id = ?");
    	    		      ){
    			conn.setAutoCommit(false);
    		          
    		    stmt1.setString(1, id);
    		    
    		    stmt1.executeUpdate(); // can cause SQLException
                conn.commit();     
    		}// also can cause SQLException!
                catch(Exception ex)
                {
                    conn.rollback();
                    throw ex;
                }
                finally{
                    conn.setAutoCommit(true);
                }
    		}
    	catch (Exception sqle)
    	{
    			//conn.rollback();
    			System.out.println("Exception : " + sqle);
    			sqle.printStackTrace();
    	}
    }
}

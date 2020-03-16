//package ass;

import java.sql.*;
import java.util.Scanner;
class ResultSetOutput {
	public static void toHTML(ResultSet rs) {
    	try {
    		
	    	System.out.print("<table>\n");
	    	ResultSetMetaData rsmd = rs.getMetaData();
	    	
	    	System.out.print("	<tr> ");
	    	for(int i=1; i<=rsmd.getColumnCount();i++) {
	    		System.out.print("<th>");
	    		System.out.print(rsmd.getColumnName(i));
	    		System.out.print("</th> ");
	    	}
	    	System.out.print("</tr>\n");
	    	
	    	while(rs.next()) {
	    		System.out.print("	<tr> ");
	    		for(int i=1; i<=rsmd.getColumnCount();i++) {
	        		System.out.print("<td>");
	        		System.out.print(rs.getString(i));
	        		System.out.print("</td> ");
	        	}
	    		System.out.print("</tr>\n");
	    	}
	    	System.out.print("</table>\n");
    	}
    	catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void toJSON(ResultSet rs) {
		try{
			ResultSetMetaData rsmd = rs.getMetaData();
			System.out.print("{header: [\"");
			int i;
			int len = rsmd.getColumnCount();
			for (i = 1; i < len; i++) {
				System.out.print(rsmd.getColumnName(i));
				System.out.print("\", \"");
			}
			System.out.print(rsmd.getColumnName(i));
			System.out.println("\"],");
			int tempSpace = 1;
			int numSpaces = 1;
			int nextLineSpaces = tempSpace + numSpaces + 7;
			for (int j = 0; j < tempSpace; j++) {
				System.out.print(" ");
			}
			System.out.print("data: [");
			for (int j = 0; j < numSpaces; j++) {
				System.out.print(" ");
			}
			//int cnt = rs.getInt("rowcount");
			if (rs.next()) {
				System.out.print("{");
				for (i = 1; i < len; i++) {
					System.out.print(rsmd.getColumnName(i) + ":");
					String temp =rs.getString(i);
					if (rsmd.getColumnTypeName(i).equals("varchar") && !rs.wasNull()) {
						System.out.print("\"" + temp + "\"");
					}
					else System.out.print(rs.getString(i));
					System.out.print(", ");
				}
				System.out.print(rsmd.getColumnName(i) + ":");
				String temp =rs.getString(i);
				if (rsmd.getColumnTypeName(i).equals("varchar") && !rs.wasNull()) {
					System.out.print("\"" + temp + "\"");
				}
				else System.out.print(rs.getString(i));
			}
			while(rs.next()) {
				System.out.println("},");
				for (int j = 0; j < nextLineSpaces; j++) {
					System.out.print(" ");
				}
				System.out.print("{");
				for (i = 1; i < len; i++) {
					System.out.print(rsmd.getColumnName(i) + ":");
					String temp =rs.getString(i);
					if (rsmd.getColumnTypeName(i).equals("varchar") && !rs.wasNull()) {
						System.out.print("\"" + temp + "\"");
					}
					else System.out.print(rs.getString(i));
					System.out.print(", ");
				}
				System.out.print(rsmd.getColumnName(i) + ":");
				String temp =rs.getString(i);
				if (rsmd.getColumnTypeName(i).equals("varchar") && !rs.wasNull()) {
					System.out.print("\"" + temp + "\"");
				}
				else System.out.print(rs.getString(i));
			}
			System.out.println("}");
			for (int j = 0; j < nextLineSpaces - 1; j++) {
				System.out.print(" ");
			}
			System.out.println("]");
			System.out.println("}");
		}
		catch(SQLException sqle) {
			System.out.println("Exception : " + sqle);
		}
	}
}
public class Ass4B_Part1_160050026_160050028 {
    private static final String url = "jdbc:postgresql://localhost:5122/postgres";
    private static final String user = "Aman";
    private static final String password = "";

    public static void main(String[] args) {
    	Scanner sc = new Scanner(System.in);
        String query = sc.nextLine(); 
        try (Connection conn = DriverManager.getConnection(url, user, password))
        {
            try{
            	Statement stmt = conn.createStatement(); 
            	ResultSet rs = stmt.executeQuery(query);
        		ResultSetOutput tt = new ResultSetOutput();
            	tt.toHTML(rs);
        		rs = stmt.executeQuery(query);
        		tt.toJSON(rs);
        		stmt.close();
        		conn.close();
            }
            catch(Exception ex)
            {
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

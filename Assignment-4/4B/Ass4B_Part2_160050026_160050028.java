//package ass;

import java.sql.*;
import java.util.Scanner;

public class Ass4B_Part2_160050026_160050028 {
    private static final String url = "jdbc:postgresql://localhost:5122/postgres";
    private static final String user = "Aman";
    private static final String password = "";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String query = "create temporary table maxheight as("
                + "with recursive heights( ID, i) as (\n" + 
                "   select ID, 1\n" + 
                "   from part\n" + 
                "   union\n" + 
                "   select subpart.pID, heights.i + 1\n" + 
                "   from heights, subpart\n" + 
                "   where subpart.spID = heights.ID and heights.i < 100\n" + 
                "   ),\n" + 
                "   maxheight(id, height) as (\n" + 
                "       select id, max(i)\n" + 
                "       from heights\n" + 
                "       group by id\n" + 
                "       )\n" + 
                "   select * from maxheight);"
                + "create temporary table cost as select m.id, part.cost from part, maxheight m where part.id = m.id and m.height = 1;";
        try (Connection conn = DriverManager.getConnection(url, user, password))
        {
            try{
                conn.setAutoCommit(false);
                Statement stmt = conn.createStatement(); 
                ResultSet rs;
                int rsv = stmt.executeUpdate(query);
                String left = "insert into cost \n" + 
                        "select maxheight.ID, sum(subpart.number*cost.cost) + part.cost\n" + 
                        "from maxheight, cost, subpart, part\n" + 
                        "where maxheight.height = ";
                String right = " and subpart.pid = maxheight.id and cost.id= subpart.spid and part.id = maxheight.id\n" + 
                        "group by maxheight.id, part.cost";
                for(int i=2; i<=100 ; i++) {
                    stmt.executeUpdate(left + i + right);
                }
                conn.commit();
                while(true) {
                    String quer = sc.next();
                    PreparedStatement stmt1 = conn.prepareStatement("select c.cost from cost c where c.id = ?");
                    stmt1.setString(1,quer);
                    rs =  stmt1.executeQuery();
                    int cnt = 0;
                    while(rs.next()) {
                        cnt++;
                        System.out.println(rs.getInt(1));
                    }
                    if  (cnt  == 0) {
                        System.out.println("No such part found");
                    }
                }
            }
            catch(Exception ex)
            {
                conn.rollback();
                throw ex;
            }
            finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

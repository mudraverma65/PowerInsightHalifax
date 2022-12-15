import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
public class DistributionHub implements Serializable {

    String hubIdentifier;

    Point location;

    Set<String> servicedAreas = new HashSet<String>();

    DistributionHub( String hubIdentifier, Point location, Set<String> servicedAreas){
        this.hubIdentifier = hubIdentifier;
        this.location = location;
        this.servicedAreas = servicedAreas;
    }

    boolean addHub(){
        JDBCConnection conn = new JDBCConnection();
        String query = "Insert into distributionhub (hubIdentifier) values (?)";
        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(query);
            statement.setString(1,hubIdentifier);
            statement.executeUpdate();
            conn.setupConnection().close();
        } catch (SQLException e) {
            return false;
        }

        String query1 = "Insert into point (hubIdentifier, x,y) values (?, ?,?)";
        double currentx = location.getX();
        double currenty = location.getY();
        try{
            PreparedStatement statement = conn.setupConnection().prepareStatement(query1);
            statement.setString(1,hubIdentifier);
            statement.setDouble(2,currentx);
            statement.setDouble(3,currenty);
            statement.executeUpdate();
            conn.setupConnection().close();
        }
        catch(SQLException e){
            return false;
        }


        Iterator<String> it1 = servicedAreas.iterator();
        while(it1.hasNext()){
            String postal = it1.next();
            String query2 = "Insert into hubpostal (hubIdentifier, postalCode) values (?, ?)";
            try {
                PreparedStatement statement = conn.setupConnection().prepareStatement(query2);
                statement.setString(1,hubIdentifier);
                statement.setObject(2,postal);
                statement.executeUpdate();
                conn.setupConnection().close();
            } catch (SQLException e) {
                return false;
            }
        }
        return true;
    }

    boolean updateHub() {
        JDBCConnection conn = new JDBCConnection();
        String query1 = "update point \n" +
                "set x= ?, y = ? " +
                "where hubIdentifier = ?";
        double currentx = location.getX();
        double currenty = location.getY();
        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(query1);
            statement.setDouble(1, currentx);
            statement.setDouble(2, currenty);
            statement.setString(3, hubIdentifier);
            statement.executeUpdate();
            conn.setupConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        Iterator<String> it1 = servicedAreas.iterator();
        while (it1.hasNext()) {
            String postal = it1.next();
            String query2 = "update hubpostal\n" +
                    "set postalCode = ? " +
                    "where hubIdentifier = ?";
            try {
                PreparedStatement statement = conn.setupConnection().prepareStatement(query2);
                statement.setObject(1, postal);
                statement.setString(2, hubIdentifier);
                statement.executeUpdate();
                conn.setupConnection().close();
            } catch (SQLException e) {
                return false;
            }
        }
        return true;
    }

}

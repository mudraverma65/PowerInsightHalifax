import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostalCode {

    String postalCode;
    int population;
    int area;

    PostalCode(String postalCode, int population, int area) throws SQLException {
        this.postalCode = postalCode;
        this.population = population;
        this.area = area;
    }

    boolean addPost(){
        JDBCConnection conn = new JDBCConnection();
        String query = "Insert into postalcode (postalCode, population, area) values (?, ?, ?)";
        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(query);
            statement.setString(1,postalCode);
            statement.setInt(2,population);
            statement.setInt(3, area);
            statement.executeUpdate();
            conn.setupConnection().close();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    boolean updatePost(){
        JDBCConnection conn = new JDBCConnection();
        String query = "update postalcode\n" +
                "set population = ?, area = ?\n" +
                "where postalCode = ?";

        try {
            PreparedStatement statement = conn.setupConnection().prepareStatement(query);
            statement.setInt(1,population);
            statement.setInt(2,area);
            statement.setString(3, postalCode);
            statement.executeUpdate();
            conn.setupConnection().close();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

}

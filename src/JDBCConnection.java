import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnection {

    private static Connection connection = null;

    public Connection setupConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/powerservice",
                    "root",
                    "Mybrainisdead#123");
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
import java.sql.*;
import java.util.List;

public class SQLExecutor {
	private static PreparedStatement statement;

    static ResultSet executeQuery(Connection conn, String sql, List<Object> params) throws SQLException {
        statement = conn.prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
        return statement.executeQuery();
    }


    static int executeUpdate(Connection conn, String sql, List<Object> params) throws SQLException {
        statement = conn.prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
        return statement.executeUpdate();
    }

    static void closeStatement() throws SQLException {
        if (statement != null)
            statement.close();
    }

}

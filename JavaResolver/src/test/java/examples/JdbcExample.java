package examples;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class JdbcExample extends AbstractDatabaseExample {
    public DatabaseValue getForId(int id) throws SQLException {
        DataSource dataSource = createDataSource();
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT ID, VALUE FROM T WHERE ID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    DatabaseValue databaseValue = new DatabaseValue();
                    databaseValue.setId(resultSet.getInt("ID"));
                    databaseValue.setValue(resultSet.getString("VALUE"));
                    return databaseValue;
                } catch (SQLException e) {
                    // handle exception
                }
            } catch (SQLException e) {
                // handle exception
            }
        } catch (SQLException e) {
            // handle exception
        }
        return null;
    }

    public void insert(DatabaseValue value) throws SQLException {
        DataSource dataSource = createDataSource();
        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO T (ID, VALUE) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, value.getId());
                preparedStatement.setString(2, value.getValue());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                // handle exception
            }
        } catch (SQLException e) {
            // handle exception
        }
    }
}

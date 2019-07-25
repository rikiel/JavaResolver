package examples;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("Convert2Lambda")
public class JdbcTemplateExample extends AbstractDatabaseExample {
    public DatabaseValue getForId(int id) throws SQLException {
        DataSource dataSource = createDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(
                "SELECT ID, VALUE FROM T WHERE ID = ?",
                new RowMapper<DatabaseValue>() {
                    @Override
                    public DatabaseValue mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                        DatabaseValue databaseValue = new DatabaseValue();
                        databaseValue.setId(resultSet.getInt("ID"));
                        databaseValue.setValue(resultSet.getString("VALUE"));
                        return databaseValue;
                    }
                },
                id);
    }

    public void insert(DatabaseValue value) throws SQLException {
        DataSource dataSource = createDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                "INSERT INTO T (ID, VALUE) VALUES (?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setInt(1, value.getId());
                        preparedStatement.setString(2, value.getValue());
                    }
                });
    }
}
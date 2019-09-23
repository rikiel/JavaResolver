package examples;

import java.sql.SQLException;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

public abstract class AbstractDatabaseExample {
    public static DataSource createDataSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL("jdbc:oracle:thin:@//192.168.0.16:1521/orcl");
        dataSource.setUser("User");
        dataSource.setPassword("Password");
        return dataSource;
    }

    public static class DatabaseValue {
        private Integer id;
        private String value;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

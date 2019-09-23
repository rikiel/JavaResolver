package eu.profinit.manta.connector.java.analysis.datasource.target;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.ibm.db2.jcc.DB2SimpleDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;
import com.teradata.jdbc.TeraDataSource;

import eu.profinit.manta.connector.java.analysis.TestWrapper;
import oracle.jdbc.pool.OracleDataSource;

public class DataSourceTarget {
    public static void runApacheCommonsDataSource() throws SQLException {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("ConnectionUrl");
        dataSource.setUsername("UserName");

        runForDataSource(dataSource);
    }

    public static void runTeraDataSource() throws SQLException {
        final TeraDataSource dataSource = new TeraDataSource();
        dataSource.setuser("UserName");

        runForDataSource(dataSource);
    }

    public static void runOracleDataSource() throws SQLException {
        final OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL("ConnectionUrl");
        dataSource.setUser("UserName");

        runForDataSource(dataSource);
    }

    public static void runMsSqlDataSourceStandard() throws SQLException {
        final SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setURL("ConnectionUrl");
        dataSource.setUser("UserName");

        runForDataSource(dataSource);
    }

    public static void runMsSqlDataSourcePooled() throws SQLException {
        final SQLServerConnectionPoolDataSource dataSource = new SQLServerConnectionPoolDataSource();
        dataSource.setURL("ConnectionUrl");
        dataSource.setUser("UserName");

        runForDataSource(dataSource);
    }

    public static void runMsSqlDataSourceDistributed() throws SQLException {
        final SQLServerXADataSource dataSource = new SQLServerXADataSource();
        dataSource.setURL("ConnectionUrl");
        dataSource.setUser("UserName");

        runForDataSource(dataSource);
    }

    public static void runPostgreSqlDataSource() throws SQLException {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("ConnectionUrl");
        dataSource.setUser("UserName");

        runForDataSource(dataSource);
    }

    public static void runDb2SqlDataSource() throws SQLException {
        final DB2SimpleDataSource dataSource = new DB2SimpleDataSource();
        dataSource.setUser("UserName");

        runForDataSource(dataSource);
    }

    public static void runEmbeddedDataSource() throws SQLException {
        final DataSource dataSource = new EmbeddedDatabaseBuilder()
                .addScript("/tmp/script1.sql")
                .addScripts("/tmp/script2.sql", "/tmp/script3.sql")
                .setName("DbName")
                .setType(EmbeddedDatabaseType.HSQL)
                .build();

        runForDataSource(dataSource);
    }

    private static void runForDataSource(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                final ResultSet resultSet = statement.executeQuery("SELECT TABLE_VALUE FROM TABLE_NAME");

                TestWrapper.storeValString(resultSet.getString(1), "OUTPUT_TABLE");
            }
        }
    }
}

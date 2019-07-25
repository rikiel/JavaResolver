package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

public class JdbcTemplateBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        ps.setInt(1, i);
    }

    @Override
    public int getBatchSize() {
        return 100;
    }
}

package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementSetter;

public class JdbcTemplatePreparedStatementSetter implements PreparedStatementSetter {
    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setInt(1, 1);
    }
}

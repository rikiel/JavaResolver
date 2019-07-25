package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;

public class JdbcTemplateParameterizedPreparedStatementSetter implements ParameterizedPreparedStatementSetter<JdbcTemplateModel> {
    @Override
    public void setValues(PreparedStatement ps, JdbcTemplateModel argument) throws SQLException {
        ps.setInt(1, argument.getId());
    }
}

package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementCreator;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateQueryConstants;

public class JdbcTemplatePreparedStatementCreator implements PreparedStatementCreator {
    @Override
    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(JdbcTemplateQueryConstants.SELECT_BY_ID);
        preparedStatement.setInt(1, 1);
        return preparedStatement;
    }
}

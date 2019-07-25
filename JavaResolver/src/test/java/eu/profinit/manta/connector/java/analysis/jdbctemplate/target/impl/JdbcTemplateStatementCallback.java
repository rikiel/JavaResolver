package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.StatementCallback;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateQueryConstants;

public class JdbcTemplateStatementCallback implements StatementCallback<JdbcTemplateModel> {
    @Override
    public JdbcTemplateModel doInStatement(Statement statement) throws SQLException, DataAccessException {
        final ResultSet resultSet = statement.executeQuery(JdbcTemplateQueryConstants.SELECT_ALL);
        return JdbcTemplateModel.map(resultSet);
    }
}

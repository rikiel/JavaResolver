package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;

public class JdbcTemplatePreparedStatementCallback implements PreparedStatementCallback<JdbcTemplateModel> {
    @Override
    public JdbcTemplateModel doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
        final ResultSet resultSet = preparedStatement.executeQuery();
        return JdbcTemplateModel.map(resultSet);
    }
}

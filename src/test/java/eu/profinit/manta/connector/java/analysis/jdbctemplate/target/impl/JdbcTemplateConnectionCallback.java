package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateQueryConstants;

public class JdbcTemplateConnectionCallback implements ConnectionCallback<JdbcTemplateModel> {
    @Override
    public JdbcTemplateModel doInConnection(Connection connection) throws SQLException, DataAccessException {
        final ResultSet resultSet = connection.createStatement().executeQuery(JdbcTemplateQueryConstants.SELECT_ALL);
        return JdbcTemplateModel.map(resultSet);
    }
}

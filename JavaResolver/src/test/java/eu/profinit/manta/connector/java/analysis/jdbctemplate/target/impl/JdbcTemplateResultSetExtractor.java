package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;

public class JdbcTemplateResultSetExtractor implements ResultSetExtractor<JdbcTemplateModel> {
    @Override
    public JdbcTemplateModel extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        return JdbcTemplateModel.map(resultSet);
    }
}

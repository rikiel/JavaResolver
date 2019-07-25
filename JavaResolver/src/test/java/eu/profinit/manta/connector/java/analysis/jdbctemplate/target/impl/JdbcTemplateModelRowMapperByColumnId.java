package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;

public class JdbcTemplateModelRowMapperByColumnId implements RowMapper<JdbcTemplateModel> {
    @Override
    public JdbcTemplateModel mapRow(ResultSet resultSet, int i) throws SQLException {
        final JdbcTemplateModel model = new JdbcTemplateModel();
        model.setId(resultSet.getInt(1));
        model.setValue(resultSet.getString(2));
        return model;
    }
}

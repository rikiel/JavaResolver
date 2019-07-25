package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowCallbackHandler;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;

public class JdbcTemplateRowCallbackHandler implements RowCallbackHandler {
    @Override
    public void processRow(ResultSet rs) throws SQLException {
        final JdbcTemplateModel jdbcTemplateModel = JdbcTemplateModel.map(rs);

        jdbcTemplateModel.store();
    }
}

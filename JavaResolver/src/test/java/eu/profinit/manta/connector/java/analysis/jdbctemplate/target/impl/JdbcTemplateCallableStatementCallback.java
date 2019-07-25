package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateModel;
import oracle.jdbc.OracleTypes;

public class JdbcTemplateCallableStatementCallback implements CallableStatementCallback<JdbcTemplateModel> {
    @Override
    public JdbcTemplateModel doInCallableStatement(CallableStatement callableStatement) throws SQLException, DataAccessException {
        callableStatement.setInt(1, 1);
        callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
        callableStatement.executeUpdate();
        final ResultSet resultSet = (ResultSet) callableStatement.getObject(2);
        return JdbcTemplateModel.map(resultSet);
    }
}

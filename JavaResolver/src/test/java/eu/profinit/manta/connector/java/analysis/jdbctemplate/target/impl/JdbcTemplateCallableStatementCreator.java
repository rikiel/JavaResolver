package eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.jdbc.core.CallableStatementCreator;

import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.JdbcTemplateQueryConstants;
import oracle.jdbc.OracleTypes;

public class JdbcTemplateCallableStatementCreator implements CallableStatementCreator {
    @Override
    public CallableStatement createCallableStatement(Connection connection) throws SQLException {
        final CallableStatement callableStatement = connection.prepareCall(JdbcTemplateQueryConstants.PROCEDURE_CALL);
        callableStatement.setInt(1, 1);
        callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
        return callableStatement;
    }
}

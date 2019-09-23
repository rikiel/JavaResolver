package eu.profinit.manta.connector.java.analysis.jdbctemplate.target;

import java.sql.SQLException;

import javax.annotation.Nonnull;

import org.springframework.jdbc.core.JdbcTemplate;

import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl.JdbcTemplateCallableStatementCallback;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl.JdbcTemplateCallableStatementCreator;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl.JdbcTemplateConnectionCallback;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl.JdbcTemplatePreparedStatementCallback;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl.JdbcTemplatePreparedStatementCreator;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.target.impl.JdbcTemplateStatementCallback;
import oracle.jdbc.pool.OracleDataSource;

public class JdbcTemplateBasicTarget {
    /**
     * @see eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateHandler#handleExecuteConnectionCallback(MethodCallDescriptionImpl)
     */
    public static void runHandleExecuteWithConnectionCallback() {
        final JdbcTemplateModel jdbcTemplateModel = jdbcTemplate().execute(new JdbcTemplateConnectionCallback());

        jdbcTemplateModel.store();
    }

    /**
     * @see eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateHandler#handleExecuteStatementCallback(MethodCallDescriptionImpl)
     */
    public static void runHandleExecuteWithStatementCallback() {
        final JdbcTemplateModel jdbcTemplateModel = jdbcTemplate().execute(new JdbcTemplateStatementCallback());

        jdbcTemplateModel.store();
    }

    /**
     * @see eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateHandler#handleExecutePreparedStatementCreatorWithPreparedStatementCallback(MethodCallDescriptionImpl)
     */
    public static void runHandleExecuteWithPreparedStatementCreatorAndPreparedStatementCallback() {
        final JdbcTemplateModel jdbcTemplateModel = jdbcTemplate().execute(
                new JdbcTemplatePreparedStatementCreator(),
                new JdbcTemplatePreparedStatementCallback());

        jdbcTemplateModel.store();
    }

    /**
     * @see eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateHandler#handleExecuteCallableStatementCreatorWithCallableStatementCallback(MethodCallDescriptionImpl)
     */
    public static void runHandleExecuteWithCallableStatementCreatorAndCallableStatementCallback() {
        final JdbcTemplateModel jdbcTemplateModel = jdbcTemplate().execute(
                new JdbcTemplateCallableStatementCreator(),
                new JdbcTemplateCallableStatementCallback());

        jdbcTemplateModel.store();
    }

    @Nonnull
    private static JdbcTemplate jdbcTemplate() {
        try {
            final OracleDataSource dataSource = new OracleDataSource();
            dataSource.setUser("java_martin");
            dataSource.setPassword("java_martin");
            dataSource.setURL("jdbc:oracle:thin:@//192.168.0.16:1521/orcl");
            return new JdbcTemplate(dataSource);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to database", e);
        }
    }
}

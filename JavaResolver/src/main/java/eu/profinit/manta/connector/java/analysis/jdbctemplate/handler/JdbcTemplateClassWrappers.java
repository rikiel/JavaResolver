package eu.profinit.manta.connector.java.analysis.jdbctemplate.handler;

import javax.annotation.Nonnull;

import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.StringStuff;

import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;

public enum JdbcTemplateClassWrappers implements ClassWrapper {
    BATCH_UPDATE_UTILS("org.springframework.jdbc.core.BatchUpdateUtils"),
    CALLABLE_STATEMENT_CALLBACK("org.springframework.jdbc.core.CallableStatementCallback"),
    CALLABLE_STATEMENT_CREATOR("org.springframework.jdbc.core.CallableStatementCreator"),
    CONNECTION_CALLBACK("org.springframework.jdbc.core.ConnectionCallback"),
    ROW_MAPPER("org.springframework.jdbc.core.RowMapper"),
    ROW_CALLBACK_HANDLER("org.springframework.jdbc.core.RowCallbackHandler"),
    JDBC_TEMPLATE("org.springframework.jdbc.core.JdbcTemplate"),
    PREPARED_STATEMENT_CALLBACK("org.springframework.jdbc.core.PreparedStatementCallback"),
    PREPARED_STATEMENT_CREATOR("org.springframework.jdbc.core.PreparedStatementCreator"),
    STATEMENT_CALLBACK("org.springframework.jdbc.core.StatementCallback"),
    ;

    private final String javaClassName;
    private final TypeName walaTypeName;

    JdbcTemplateClassWrappers(@Nonnull final String javaClassName) {
        this.javaClassName = javaClassName;
        this.walaTypeName = TypeName.findOrCreate(StringStuff.deployment2CanonicalTypeString(javaClassName));
    }

    @Nonnull
    @Override
    public String getJavaClassName() {
        return javaClassName;
    }

    @Nonnull
    @Override
    public TypeName getWalaTypeName() {
        return walaTypeName;
    }
}

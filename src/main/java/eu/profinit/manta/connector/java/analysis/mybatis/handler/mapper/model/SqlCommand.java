package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model;

import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.MyBatisUtils.MyBatisSqlVariable;

public class SqlCommand {
    private String plainSql;
    private Set<String> sqlRequestMapping;
    private CommandType commandType;
    private IMethod iMethod;
    private SqlMapping resultMapping;
    private List<MyBatisSqlVariable> variables;
    private List<SqlMapping> argumentsMapping;

    public String getPlainSql() {
        return plainSql;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setSql(String sql, CommandType commandType) {
        this.plainSql = sql;
        this.commandType = commandType;
    }

    public Set<String> getSqlRequestMapping() {
        return sqlRequestMapping;
    }

    public void setSqlRequestMapping(Set<String> sqlRequestMapping) {
        this.sqlRequestMapping = sqlRequestMapping;
    }

    public IMethod getMethod() {
        return iMethod;
    }

    public void setMethod(IMethod iMethod) {
        this.iMethod = iMethod;
    }

    public SqlMapping getResultMapping() {
        return resultMapping;
    }

    public void setResultMapping(SqlMapping resultMapping) {
        this.resultMapping = resultMapping;
    }

    public List<MyBatisSqlVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<MyBatisSqlVariable> variables) {
        this.variables = variables;
    }

    public List<SqlMapping> getArgumentsMapping() {
        return argumentsMapping;
    }

    public void setArgumentsMapping(List<SqlMapping> argumentsMapping) {
        this.argumentsMapping = argumentsMapping;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("plainSql", plainSql)
                .add("sqlRequestMapping", sqlRequestMapping)
                .add("commandType", commandType)
                .add("iMethod", iMethod)
                .add("resultMapping", resultMapping)
                .add("variables", variables)
                .add("argumentsMapping", argumentsMapping)
                .toString();
    }

    public enum CommandType {
        SELECT,
        INSERT,
        DELETE,
        UPDATE
    }
}

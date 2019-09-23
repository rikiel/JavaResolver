package eu.profinit.manta.connector.java.analysis.datasource.handler;

import javax.annotation.Nonnull;

import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.StringStuff;

import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;

public enum DataSourceClassWrappers implements ClassWrapper {
    APACHE_BASIC_DATA_SOURCE("org.apache.commons.dbcp2.BasicDataSource"),
    DB2_BASE_DATA_SOURCE("com.ibm.db2.jcc.DB2BaseDataSource"),
    EMBEDDED_DATABASE_BUILDER("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder"),
    MS_SQL_DATA_SOURCE("com.microsoft.sqlserver.jdbc.ISQLServerDataSource"),
    ORACLE_DATA_SOURCE("oracle.jdbc.pool.OracleDataSource"),
    POSTGRESQL_BASE_DATA_SOURCE("org.postgresql.ds.common.BaseDataSource"),
    TERA_DATA_SOURCE("com.teradata.jdbc.TeraDataSourceBase");

    private final String javaClassName;
    private final TypeName walaTypeName;

    DataSourceClassWrappers(@Nonnull final String javaClassName) {
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

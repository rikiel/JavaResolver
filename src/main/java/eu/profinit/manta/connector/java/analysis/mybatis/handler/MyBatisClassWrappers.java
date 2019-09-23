package eu.profinit.manta.connector.java.analysis.mybatis.handler;

import javax.annotation.Nonnull;

import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.StringStuff;

import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;

public enum MyBatisClassWrappers implements ClassWrapper {
    SQL_SESSION_FACTORY_BUILDER("org.apache.ibatis.session.SqlSessionFactoryBuilder"),
    SQL_SESSION_FACTORY("org.apache.ibatis.session.SqlSessionFactory"),
    SQL_SESSION_MANAGER("org.apache.ibatis.session.SqlSessionManager"),
    CONFIGURATION("org.apache.ibatis.session.Configuration"),
    ENVIRONMENT("org.apache.ibatis.mapping.Environment"),
    RESOURCES("org.apache.ibatis.io.Resources"),
    TRANSACTION_FACTORY("org.apache.ibatis.transaction.TransactionFactory"),
    DELETE_ANNOTATION("org.apache.ibatis.annotations.Delete"),
    INSERT_ANNOTATION("org.apache.ibatis.annotations.Insert"),
    RESULTS_ANNOTATION("org.apache.ibatis.annotations.Results"),
    CONSTRUCTOR_ARGS_ANNOTATION("org.apache.ibatis.annotations.ConstructorArgs"),
    SELECT_ANNOTATION("org.apache.ibatis.annotations.Select"),
    UPDATE_ANNOTATION("org.apache.ibatis.annotations.Update"),
    PARAM_ANNOTATION("org.apache.ibatis.annotations.Param"),
    ;

    private final String javaClassName;
    private final TypeName walaTypeName;

    MyBatisClassWrappers(@Nonnull final String javaClassName) {
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

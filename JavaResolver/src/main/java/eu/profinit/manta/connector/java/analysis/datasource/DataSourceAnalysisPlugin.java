package eu.profinit.manta.connector.java.analysis.datasource;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.AbstractAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.datasource.handler.ApacheCommonsBasicDataSourceHandler;
import eu.profinit.manta.connector.java.analysis.datasource.handler.Db2DataSourceHandler;
import eu.profinit.manta.connector.java.analysis.datasource.handler.EmbeddedDatabaseBuilderHandler;
import eu.profinit.manta.connector.java.analysis.datasource.handler.MsSqlDataSourceHandler;
import eu.profinit.manta.connector.java.analysis.datasource.handler.OracleDataSourceHandler;
import eu.profinit.manta.connector.java.analysis.datasource.handler.PostgreSqlDataSourceHandler;
import eu.profinit.manta.connector.java.analysis.datasource.handler.TeraDataSourceHandler;

/**
 * Plugin for DataSource implementations for multiple database types
 */
public class DataSourceAnalysisPlugin extends AbstractAnalysisPlugin {
    @Nonnull
    @Override
    protected CallHandlers getCallHandlers(@Nonnull final ClassMethodCache classMethodCache) {
        return new CallHandlers(this,
                new OracleDataSourceHandler(classMethodCache),
                new MsSqlDataSourceHandler(classMethodCache),
                new Db2DataSourceHandler(classMethodCache),
                new PostgreSqlDataSourceHandler(classMethodCache),
                new TeraDataSourceHandler(classMethodCache),
                new ApacheCommonsBasicDataSourceHandler(classMethodCache),
                new EmbeddedDatabaseBuilderHandler());
    }

    @Nonnull
    @Override
    protected List<IMethod> getMethodsToAnalyse() {
        return Collections.emptyList();
    }
}

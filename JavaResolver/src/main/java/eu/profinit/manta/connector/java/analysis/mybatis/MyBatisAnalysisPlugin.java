package eu.profinit.manta.connector.java.analysis.mybatis;

import java.util.List;

import javax.annotation.Nonnull;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.AbstractAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisEnvironmentHandler;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisMapperHandler;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisResourcesHandler;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisSqlSessionFactoryBuilderHandler;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;

import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.SQL_SESSION_FACTORY_BUILDER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.MyBatisClassWrappers.SQL_SESSION_MANAGER;

/**
 * Plugin for MyBatis Framework
 */
public class MyBatisAnalysisPlugin extends AbstractAnalysisPlugin {
    @Nonnull
    @Override
    protected CallHandlers getCallHandlers(@Nonnull final ClassMethodCache classMethodCache) {
        return new CallHandlers(this,
                new MyBatisMapperHandler(classMethodCache),
                new MyBatisSqlSessionFactoryBuilderHandler(classMethodCache),
                new MyBatisEnvironmentHandler(classMethodCache),
                new MyBatisResourcesHandler(classMethodCache));
    }

    @Nonnull
    @Override
    protected List<IMethod> getMethodsToAnalyse() throws NoSuchMethodException {
        final List<IMethod> result = WalaUtils.getDeclaredMethodsForAllSubclasses(classMethodCache, SQL_SESSION_FACTORY_BUILDER, SQL_SESSION_MANAGER);
        if (!result.isEmpty()) {
            return result;
        }
        throw new NoSuchMethodException("MyBatis classes were not found!");
    }
}

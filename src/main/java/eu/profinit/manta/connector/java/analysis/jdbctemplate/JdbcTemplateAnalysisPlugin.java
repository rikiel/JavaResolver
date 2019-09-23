package eu.profinit.manta.connector.java.analysis.jdbctemplate;

import java.util.List;

import javax.annotation.Nonnull;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.AbstractAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.common.CallHandlers;
import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateHandler;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;

import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.BATCH_UPDATE_UTILS;
import static eu.profinit.manta.connector.java.analysis.jdbctemplate.handler.JdbcTemplateClassWrappers.JDBC_TEMPLATE;

public class JdbcTemplateAnalysisPlugin extends AbstractAnalysisPlugin {
    @Nonnull
    @Override
    protected CallHandlers getCallHandlers(@Nonnull final ClassMethodCache classMethodCache) {
        return new CallHandlers(this,
                new JdbcTemplateHandler(classMethodCache));
    }

    @Nonnull
    @Override
    protected List<IMethod> getMethodsToAnalyse() throws NoSuchMethodException {
        final List<IMethod> result = WalaUtils.getDeclaredMethodsForAllSubclasses(classMethodCache, JDBC_TEMPLATE, BATCH_UPDATE_UTILS);
        if (!result.isEmpty()) {
            return result;
        }
        throw new NoSuchMethodException("Spring JDBC classes were not found!");
    }
}

package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlMapping;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;

public abstract class MyBatisMapperSqlReader {
    @Nonnull
    protected final IMethod iMethod;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected MyBatisMapperSqlReader(@Nonnull final IMethod iMethod) {
        Validate.notNull(iMethod);

        this.iMethod = iMethod;
    }

    @Nonnull
    protected abstract String getSql();

    @Nonnull
    protected abstract CommandType getCommandType();

    @Nonnull
    protected abstract SqlMapping getResultMapping();

    @Nonnull
    protected abstract List<SqlMapping> getArgumentsMapping();

    @Nonnull
    public static Optional<MyBatisMapperSqlReader> getAnalyzer(@Nonnull final IMethod iMethod,
                                                               @Nonnull final FileContentReader reader) {
        final MyBatisAnnotationMapperSqlReader annotationAnalyzer = new MyBatisAnnotationMapperSqlReader(iMethod);
        if (annotationAnalyzer.accepts()) {
            return Optional.of(annotationAnalyzer);
        }
        final MyBatisXmlMapperSqlReader xmlAnalyzer = new MyBatisXmlMapperSqlReader(iMethod, reader);
        if (xmlAnalyzer.accepts()) {
            return Optional.of(xmlAnalyzer);
        }
        return Optional.empty();
    }

    @Nonnull
    public final SqlCommand generateSqlCommand() {
        final SqlCommand sqlCommand = new SqlCommand();

        sqlCommand.setMethod(iMethod);
        sqlCommand.setSql(normalizeSql(getSql()), getCommandType());
        sqlCommand.setResultMapping(getResultMapping());
        sqlCommand.setVariables(MyBatisUtils.getArgumentsFromSql(sqlCommand.getPlainSql()));
        sqlCommand.setArgumentsMapping(getArgumentsMapping());

        log.debug("Loaded sqlCommand {}", sqlCommand);

        return sqlCommand;
    }

    @Nonnull
    private String normalizeSql(@Nonnull final String sql) {
        // zmaze uvodne/zaverecne medzery a nahradi viac bielych znakov za sebou za jednu medzeru
        return sql.trim().replaceAll("\\s+", " ");
    }
}

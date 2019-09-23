package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.ibm.wala.classLoader.IClass;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;

public class MyBatisClassAnalyzer {
    private final FileContentReader reader;

    public MyBatisClassAnalyzer(@Nonnull final FileContentReader reader) {
        Validate.notNull(reader);
        this.reader = reader;
    }

    /**
     * @param iClass Class, its method we want to analyze
     * @return Analyzed methods
     */
    @Nonnull
    public List<SqlCommand> analyse(@Nonnull final IClass iClass) {
        if (!iClass.isInterface()) {
            return ImmutableList.of();
        }
        return WalaUtils.getAllInterfaceMethods(iClass).stream()
                .map(iMethod -> MyBatisMapperSqlReader.getAnalyzer(iMethod, reader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(MyBatisMapperSqlReader::generateSqlCommand)
                .collect(Collectors.toList());
    }
}

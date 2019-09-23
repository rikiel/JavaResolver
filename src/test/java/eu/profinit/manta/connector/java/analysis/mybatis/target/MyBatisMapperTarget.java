package eu.profinit.manta.connector.java.analysis.mybatis.target;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import eu.profinit.manta.connector.java.analysis.TestWrapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features.MyBatisComplexFeaturesXmlMappers.SelectToListMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features.MyBatisComplexFeaturesXmlMappers.SelectToListMapper.ListHolder;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features.MyBatisComplexFeaturesXmlMappers.SelectToMapMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features.MyBatisComplexFeaturesXmlMappers.UpdateValuesFromMapMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdAnnotatedMappers.DeleteAnnotatedMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdAnnotatedMappers.InsertAnnotatedMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdAnnotatedMappers.SelectAllAnnotatedMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdAnnotatedMappers.SelectByIdAnnotatedMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdAnnotatedMappers.UpdateAnnotatedMapper;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdTestModel;
import oracle.jdbc.pool.OracleDataSource;

public class MyBatisMapperTarget {
    public static void runSelectAllAndDelete() {
        try (final SqlSession sqlSession = openSession()) {
            final SelectAllAnnotatedMapper selectAllMapper = sqlSession.getMapper(SelectAllAnnotatedMapper.class);
            final DeleteAnnotatedMapper deleteMapper = sqlSession.getMapper(DeleteAnnotatedMapper.class);
            final List<MyBatisSingleIdTestModel> values = selectAllMapper.selectAll();
            for (MyBatisSingleIdTestModel value : values) {
                deleteMapper.delete(value);
            }
        }
    }

    public static void runSelectAll() {
        try (final SqlSession sqlSession = openSession()) {
            final SelectAllAnnotatedMapper selectAllMapper = sqlSession.getMapper(SelectAllAnnotatedMapper.class);
            final List<MyBatisSingleIdTestModel> values = selectAllMapper.selectAll();
            for (MyBatisSingleIdTestModel value : values) {
                TestWrapper.storeValString(value.getValue(), "UPDATE_TABLE");
            }
        }
    }

    public static void runSelectAllWithDataSource() {
        try (final SqlSession sqlSession = openSessionWithDataSource()) {
            final SelectAllAnnotatedMapper selectAllMapper = sqlSession.getMapper(SelectAllAnnotatedMapper.class);
            final List<MyBatisSingleIdTestModel> values = selectAllMapper.selectAll();
            for (MyBatisSingleIdTestModel value : values) {
                TestWrapper.storeValString(value.getValue(), "UPDATE_TABLE");
            }
        }
    }

    public static void runSelectById() {
        try (final SqlSession sqlSession = openSession()) {
            final SelectByIdAnnotatedMapper selectByIdMapper = sqlSession.getMapper(SelectByIdAnnotatedMapper.class);
            final MyBatisSingleIdTestModel model = selectByIdMapper.selectById(1);
            if (model != null) {
                TestWrapper.storeValString(model.getValue(), "UPDATE_TABLE");
            }
        }
    }

    public static void runInsert() {
        try (final SqlSession sqlSession = openSession()) {
            final MyBatisSingleIdTestModel model = new MyBatisSingleIdTestModel();
            model.setId(2);
            model.setValue(TestWrapper.loadValString("SOURCE_TABLE"));
            final InsertAnnotatedMapper insertMapper = sqlSession.getMapper(InsertAnnotatedMapper.class);
            insertMapper.insert(model);
        }
    }

    public static void runDelete() {
        try (final SqlSession sqlSession = openSession()) {
            final MyBatisSingleIdTestModel model = new MyBatisSingleIdTestModel();
            model.setId(1);
            model.setValue(TestWrapper.loadValString("SOURCE_TABLE"));
            final DeleteAnnotatedMapper deleteMapper = sqlSession.getMapper(DeleteAnnotatedMapper.class);
            deleteMapper.delete(model);
        }
    }

    public static void runUpdate() {
        try (final SqlSession sqlSession = openSession()) {
            final MyBatisSingleIdTestModel model = new MyBatisSingleIdTestModel();
            model.setId(1);
            model.setValue(TestWrapper.loadValString("SOURCE_TABLE"));
            final UpdateAnnotatedMapper updateMapper = sqlSession.getMapper(UpdateAnnotatedMapper.class);
            updateMapper.update(model);
        }
    }

    public static void runSelectForMap() {
        try (final SqlSession sqlSession = openSession()) {
            final SelectToMapMapper selectToMapMapper = sqlSession.getMapper(SelectToMapMapper.class);
            final Map<String, Object> requestAttributes = new HashMap<>();
            requestAttributes.put("id", 1);

            selectToMapMapper.selectForMap(requestAttributes);

            final List<MyBatisSingleIdTestModel> values = (List<MyBatisSingleIdTestModel>) requestAttributes.get("result");
            for (MyBatisSingleIdTestModel value : values) {
                TestWrapper.storeValString(value.getValue(), "UPDATE_TABLE");
            }
        }
    }

    public static void runSelectForList() {
        try (final SqlSession sqlSession = openSession()) {
            final SelectToListMapper selectToListMapper = sqlSession.getMapper(SelectToListMapper.class);
            final ListHolder resultValues = new ListHolder();

            selectToListMapper.selectToList(resultValues);

            for (MyBatisSingleIdTestModel value : resultValues.getResult()) {
                TestWrapper.storeValString(value.getValue(), "UPDATE_TABLE");
            }
        }
    }

    public static void runUpdateValuesFromMap() {
        try (final SqlSession sqlSession = openSession()) {
            final UpdateValuesFromMapMapper updateMapper = sqlSession.getMapper(UpdateValuesFromMapMapper.class);
            final Map<String, String> newValueMap = new HashMap<>();
            newValueMap.put("valueKey", "newValue");

            final MyBatisSingleIdTestModel model = new MyBatisSingleIdTestModel();
            model.setId(1);

            updateMapper.update(model, newValueMap);
        }
    }

    public static void runFullTest() {
        try (final SqlSession sqlSession = openSession()) {
            final SelectAllAnnotatedMapper selectAllMapper = sqlSession.getMapper(SelectAllAnnotatedMapper.class);
            final DeleteAnnotatedMapper deleteMapper = sqlSession.getMapper(DeleteAnnotatedMapper.class);
            final UpdateAnnotatedMapper updateMapper = sqlSession.getMapper(UpdateAnnotatedMapper.class);
            final InsertAnnotatedMapper insertMapper = sqlSession.getMapper(InsertAnnotatedMapper.class);

            final List<MyBatisSingleIdTestModel> values = selectAllMapper.selectAll();
            for (MyBatisSingleIdTestModel value : values) {
                if (value.getId() == 1) {
                    deleteMapper.delete(value);
                } else if (value.getId() == 3) {
                    value.setValue("Updated - " + value.getValue());
                    updateMapper.update(value);
                } else if (value.getId() == 2) {
                    final MyBatisSingleIdTestModel model = new MyBatisSingleIdTestModel();
                    model.setId(111);
                    model.setValue(value.getValue());
                    insertMapper.insert(model);
                }
            }
        }
    }

    @Nonnull
    private static SqlSession openSession() {
        try {
            return SqlSessionManager.newInstance(Resources.getResourceAsStream("config/MyBatisMapperTargetTest/MyBatisConfiguration.xml")).openSession();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read mybatis config file!", e);
        }
    }

    @Nonnull
    private static SqlSession openSessionWithDataSource() {
        try {
            final OracleDataSource dataSource = new OracleDataSource();
            dataSource.setURL("jdbc:oracle:thin:@//192.168.0.16:1521/orcl");
            dataSource.setPassword("java_martin");
            dataSource.setUser("java_martin");
            final Environment environment = new Environment.Builder("environmentId")
                    .transactionFactory(new JdbcTransactionFactory())
                    .dataSource(dataSource)
                    .build();
            final Configuration configuration = new Configuration(environment);
            configuration.addMapper(SelectAllAnnotatedMapper.class);
            return new SqlSessionFactoryBuilder().build(configuration).openSession();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot read mybatis config file!", e);
        }
    }
}

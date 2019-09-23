package examples;

import java.io.IOException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisXmlExample extends AbstractDatabaseExample {
    public DatabaseValue getForId(int id) throws Exception {
        SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Mapper mapper = sqlSession.getMapper(Mapper.class);
            return mapper.getForId(id);
        }
    }

    public void insert(DatabaseValue value) throws Exception {
        SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Mapper mapper = sqlSession.getMapper(Mapper.class);
            mapper.insert(value);
        }
    }

    public SqlSessionFactory createSqlSessionFactory() throws IOException {
            return new SqlSessionFactoryBuilder()
                    .build(Resources.getResourceAsReader("examples/configuration.xml"));
    }

    public interface Mapper {
        DatabaseValue getForId(int id);

        void insert(DatabaseValue value);
    }
}

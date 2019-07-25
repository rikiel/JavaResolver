package examples;

import java.sql.SQLException;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class MyBatisAnnotationExample extends AbstractDatabaseExample {
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

    public SqlSessionFactory createSqlSessionFactory() throws SQLException {
        Environment environment = new Environment.Builder("environmentId")
                .dataSource(createDataSource())
                .transactionFactory(new JdbcTransactionFactory())
                .build();
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(Mapper.class);
        return new SqlSessionFactoryBuilder()
                .build(configuration);
    }

    public interface Mapper {
        @Results(value = {
                @Result(column = "ID", property = "id"),
                @Result(column = "VALUE", property = "value")
        })
        @Select("SELECT ID, VALUE FROM T WHERE ID = #{id}")
        DatabaseValue getForId(int id);

        @Insert("INSERT INTO T (ID, VALUE) VALUES (#{id}, #{value})")
        void insert(DatabaseValue value);
    }
}

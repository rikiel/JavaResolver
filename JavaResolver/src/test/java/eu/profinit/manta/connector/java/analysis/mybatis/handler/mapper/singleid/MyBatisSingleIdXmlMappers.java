package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid;

import java.util.List;

import javax.annotation.Nonnull;

public class MyBatisSingleIdXmlMappers {
    public interface SelectAllXmlMapper {
        @Nonnull
        List<MyBatisSingleIdTestModel> selectAll();
    }

    public interface SelectByIdXmlMapper {
        @Nonnull
        MyBatisSingleIdTestModel selectById(int id);
    }

    public interface InsertXmlMapper {
        int insert(MyBatisSingleIdTestModel parameter);
    }

    public interface UpdateXmlMapper {
        boolean update(MyBatisSingleIdTestModel parameter);
    }

    public interface DeleteXmlMapper {
        void delete(MyBatisSingleIdTestModel parameter);
    }

    public interface SelectByIdWithConstructorArgsXmlMapper {
        MyBatisSingleIdTestModel selectById(int id);
    }
}

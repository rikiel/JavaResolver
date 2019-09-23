package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.multiid;

import java.util.List;

import javax.annotation.Nonnull;

public class MyBatisMultiIdXmlMappers {
    public interface SelectAllXmlMapper {
        @Nonnull
        List<MyBatisMultiIdTestModel> selectAll();
    }

    public interface SelectByIdXmlMapper {
        @Nonnull
        MyBatisMultiIdTestModel selectById(int id1, int id2);
    }

    public interface InsertXmlMapper {
        int insert(MyBatisMultiIdTestModel parameter);
    }

    public interface UpdateXmlMapper {
        boolean update(MyBatisMultiIdTestModel parameter);
    }

    public interface DeleteXmlMapper {
        void delete(MyBatisMultiIdTestModel parameter);
    }
}

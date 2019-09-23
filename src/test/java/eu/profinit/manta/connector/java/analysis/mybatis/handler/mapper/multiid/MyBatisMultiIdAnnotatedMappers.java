package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.multiid;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public class MyBatisMultiIdAnnotatedMappers {
    public interface SelectAllAnnotatedMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id1", column = "TABLE_ID_1", id = true),
                        @Result(property = "id2", column = "TABLE_ID_2", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "SELECT TABLE_ID_1, TABLE_ID_2, TABLE_VALUE",
                "FROM TABLE_NAME_MULTI"
        })
        List<MyBatisMultiIdTestModel> selectAll();
    }

    public interface SelectByIdAnnotatedMapper {
        @Nullable
        @Results(id = "mapById",
                value = {
                        @Result(property = "id1", column = "TABLE_ID_1", id = true),
                        @Result(property = "id2", column = "TABLE_ID_2", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "SELECT TABLE_ID_1, TABLE_ID_2, TABLE_VALUE",
                "FROM TABLE_NAME_MULTI",
                "WHERE TABLE_ID_1 = #{id1} AND TABLE_ID_2 = #{id2}"
        })
        MyBatisMultiIdTestModel selectById(int id1, int id2);
    }

    public interface InsertAnnotatedMapper {
        @Insert({
                "INSERT INTO TABLE_NAME_MULTI",
                "(TABLE_ID_1, TABLE_ID_2, TABLE_VALUE)",
                "VALUES (#{id1}, #{id2}, #{value})"
        })
        int insert(MyBatisMultiIdTestModel model);
    }

    public interface UpdateAnnotatedMapper {
        @Update({
                "UPDATE TABLE_NAME_MULTI",
                "SET TABLE_VALUE = #{value}",
                "WHERE TABLE_ID_1 = #{id1} AND TABLE_ID_2 = #{id2}"
        })
        boolean update(MyBatisMultiIdTestModel model);
    }

    public interface DeleteAnnotatedMapper {
        @Delete({
                "DELETE FROM TABLE_NAME_MULTI",
                "WHERE TABLE_ID_1 = #{id1} AND TABLE_ID_2 = #{id2}"
        })
        void delete(MyBatisMultiIdTestModel model);
    }
}

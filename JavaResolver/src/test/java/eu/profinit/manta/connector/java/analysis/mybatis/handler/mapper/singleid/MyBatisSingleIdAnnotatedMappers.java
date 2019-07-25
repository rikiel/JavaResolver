package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public class MyBatisSingleIdAnnotatedMappers {
    public interface SelectAllAnnotatedMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "SELECT TABLE_ID, TABLE_VALUE",
                "FROM TABLE_NAME"
        })
        List<MyBatisSingleIdTestModel> selectAll();
    }

    public interface SelectByIdAnnotatedMapper {
        @Nullable
        @Results(id = "mapById",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "SELECT TABLE_ID, TABLE_VALUE",
                "FROM TABLE_NAME",
                "WHERE TABLE_ID = #{id}"
        })
        MyBatisSingleIdTestModel selectById(int id);
    }

    public interface InsertAnnotatedMapper {
        @Insert({
                "INSERT INTO TABLE_NAME",
                "(TABLE_ID, TABLE_VALUE)",
                "VALUES (#{id}, #{value})"
        })
        int insert(MyBatisSingleIdTestModel model);
    }

    public interface UpdateAnnotatedMapper {
        @Update({
                "UPDATE TABLE_NAME",
                "SET TABLE_VALUE = #{value}, TABLE_ID = #{id}",
                "WHERE TABLE_ID = #{id}"
        })
        boolean update(MyBatisSingleIdTestModel model);
    }

    public interface DeleteAnnotatedMapper {
        @Delete({
                "DELETE FROM TABLE_NAME",
                "WHERE TABLE_ID = #{id}"
        })
        void delete(MyBatisSingleIdTestModel model);
    }

    public interface SelectByIdWithConstructorArgsAnnotatedMapper {
        @ConstructorArgs(value = {
                @Arg(column = "TABLE_ID", javaType = Integer.class, id = true)
        })
        @Results(id = "mapById",
                value = {
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "SELECT TABLE_ID, TABLE_VALUE",
                "FROM TABLE_NAME",
                "WHERE TABLE_ID = #{id}"
        })
        MyBatisSingleIdTestModel selectById(int id);
    }
}

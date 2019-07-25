package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.features;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.singleid.MyBatisSingleIdTestModel;

public class MyBatisComplexFeaturesAnnotatedMappers {
    public interface SelectAllIfScriptMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "<script>",
                " SELECT TABLE_ID, TABLE_VALUE",
                " FROM TABLE_NAME",
                "  <if test='vale != null'>",
                "   WHERE TABLE_VALUE LIKE #{value}",
                "  </if>",
                "</script>",
        })
        List<MyBatisSingleIdTestModel> selectAllLike(String value);
    }

    public interface SelectAllWithCommentsScriptMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "<script>",
                " <!-- some xml comment -->",
                " SELECT TABLE_ID, TABLE_VALUE",
                " <!-- some xml comment -->",
                " FROM TABLE_NAME",
                " <!-- some xml comment -->",
                "</script>",
        })
        List<MyBatisSingleIdTestModel> selectAll();
    }

    public interface SelectAllForeachScriptMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "<script>",
                " SELECT TABLE_ID, TABLE_VALUE",
                " FROM TABLE_NAME",
                " WHERE TABLE_VALUE IN",
                "  <foreach item='item' collection='values' open='(' close=')' separator=',' index='index'>",
                "   #{item}",
                "  </foreach>",
                "</script>",
        })
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllForeachDefaultValuesScriptMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "<script>",
                " SELECT TABLE_ID, TABLE_VALUE",
                " FROM TABLE_NAME",
                " WHERE TABLE_VALUE IN",
                "  (",
                "  <foreach collection='values'>",
                "   #{item}",
                "  </foreach>",
                "  )",
                "</script>",
        })
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllChooseScriptMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "<script>",
                " SELECT TABLE_ID, TABLE_VALUE",
                " FROM TABLE_NAME",
                " <choose>",
                "  <when test='values != null'>",
                "   WHERE TABLE_VALUE IN",
                "   <foreach collection='values' open='(' close=')' separator=','>",
                "    #{item}",
                "   </foreach>",
                "  </when>",
                "  <when test='values.size() == 1'>",
                "   TABLE_ID = 5",
                "  </when>",
                "  <otherwise>",
                "   TABLE_VALUE = TEST",
                "  </otherwise>",
                " </choose>",
                "</script>",
        })
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllWhereScriptMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "<script>",
                " SELECT TABLE_ID, TABLE_VALUE",
                " FROM TABLE_NAME",
                " <where>",
                "  <if test='values != null'>",
                "   AND TABLE_VALUE IN",
                "   <foreach collection='values' open='(' close=')' separator=','>",
                "    #{item}",
                "   </foreach>",
                "  </if>",
                "  <if test='values.size() == 1'>",
                "   AND TABLE_ID = 4",
                "  </if>",
                " </where>",
                "</script>",
        })
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface SelectAllWhereTrimScriptMapper {
        @Nonnull
        @Results(id = "selectAll",
                value = {
                        @Result(property = "id", column = "TABLE_ID", id = true),
                        @Result(property = "value", column = "TABLE_VALUE")
                })
        @Select({
                "<script>",
                " SELECT TABLE_ID, TABLE_VALUE",
                " FROM TABLE_NAME",
                " <trim prefix='WHERE' prefixOverrides='AND |OR '>",
                "  <if test='values != null'>",
                "   AND TABLE_VALUE IN",
                "   <foreach collection='values' open='(' close=')' separator=','>",
                "    #{item}",
                "   </foreach>",
                "  </if>",
                "  <if test='values.size() == 1'>",
                "   AND TABLE_ID = 4",
                "  </if>",
                " </trim>",
                "</script>",
        })
        List<MyBatisSingleIdTestModel> selectAllValues(List<String> values);
    }

    public interface UpdateWithSetScriptMapper {
        @Update({
                "<script>",
                " UPDATE TABLE_NAME",
                " <set>",
                "  <if test='some condition'>",
                "   TABLE_VALUE = #{value},",
                "  </if>",
                "  <if test='other condition'>",
                "   TABLE_ID = #{id},",
                "  </if>",
                " </set>",
                " WHERE TABLE_ID = #{id}",
                "</script>",
        })
        void updateAll(MyBatisSingleIdTestModel model);
    }

    public interface UpdateWithSetTrimScriptMapper {
        @Update({
                "<script>",
                " UPDATE TABLE_NAME",
                " <trim prefix='SET' suffixOverrides=','>",
                "  <if test='some condition'>",
                "   TABLE_VALUE = #{value},",
                "  </if>",
                "  <if test='other condition'>",
                "   TABLE_ID = #{id},",
                "  </if>",
                " </trim>",
                " WHERE TABLE_ID = #{id}",
                "</script>",
        })
        void updateAll(MyBatisSingleIdTestModel model);
    }
}

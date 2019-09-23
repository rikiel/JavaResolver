package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.MyBatisUtils.MyBatisSqlVariable;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlMapping;
import eu.profinit.manta.connector.java.model.flowgraph.AttributeValueConstants;

@SuppressWarnings("WeakerAccess")
public class ExpectedSqlCommandProvider {
    private final List<Class<?>> mappers = Lists.newArrayList();
    private final List<SqlCommand> commands = Lists.newArrayList();

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdSelectAll(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME", SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn("id", "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList());
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addMultiIdSelectAll(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID_1, TABLE_ID_2, TABLE_VALUE FROM TABLE_NAME_MULTI", SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn("id1", "TABLE_ID_1")
                .addPropertyToColumn("id2", "TABLE_ID_2")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList());
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdSelectById(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME WHERE TABLE_ID = #{id}", SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn("id", "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable("#{id}", "id", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addMultiIdSelectById(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID_1, TABLE_ID_2, TABLE_VALUE FROM TABLE_NAME_MULTI WHERE TABLE_ID_1 = #{id1} AND TABLE_ID_2 = #{id2}",
                SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn("id1", "TABLE_ID_1")
                .addPropertyToColumn("id2", "TABLE_ID_2")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList(
                new MyBatisSqlVariable("#{id1}", "id1", null, null),
                new MyBatisSqlVariable("#{id2}", "id2", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdInsert(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("INSERT INTO TABLE_NAME (TABLE_ID, TABLE_VALUE) VALUES (#{id}, #{value})", SqlCommand.CommandType.INSERT);
        command.setResultMapping(new SqlMapping());
        command.setVariables(Lists.newArrayList(
                new MyBatisSqlVariable("#{id}", "id", null, null),
                new MyBatisSqlVariable("#{value}", "value", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addMultiIdInsert(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("INSERT INTO TABLE_NAME_MULTI (TABLE_ID_1, TABLE_ID_2, TABLE_VALUE) VALUES (#{id1}, #{id2}, #{value})", SqlCommand.CommandType.INSERT);
        command.setResultMapping(new SqlMapping());
        command.setVariables(Lists.newArrayList(
                new MyBatisSqlVariable("#{id1}", "id1", null, null),
                new MyBatisSqlVariable("#{id2}", "id2", null, null),
                new MyBatisSqlVariable("#{value}", "value", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdUpdate(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("UPDATE TABLE_NAME SET TABLE_VALUE = #{value}, TABLE_ID = #{id} WHERE TABLE_ID = #{id}", SqlCommand.CommandType.UPDATE);
        command.setResultMapping(new SqlMapping());
        command.setVariables(Lists.newArrayList(
                new MyBatisSqlVariable("#{value}", "value", null, null),
                new MyBatisSqlVariable("#{id}", "id", null, null),
                new MyBatisSqlVariable("#{id}", "id", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addMultiIdUpdate(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("UPDATE TABLE_NAME_MULTI SET TABLE_VALUE = #{value} WHERE TABLE_ID_1 = #{id1} AND TABLE_ID_2 = #{id2}", SqlCommand.CommandType.UPDATE);
        command.setResultMapping(new SqlMapping());
        command.setVariables(Lists.newArrayList(
                new MyBatisSqlVariable("#{value}", "value", null, null),
                new MyBatisSqlVariable("#{id1}", "id1", null, null),
                new MyBatisSqlVariable("#{id2}", "id2", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdDelete(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("DELETE FROM TABLE_NAME WHERE TABLE_ID = #{id}", SqlCommand.CommandType.DELETE);
        command.setResultMapping(new SqlMapping());
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable("#{id}", "id", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addMultiIdDelete(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("DELETE FROM TABLE_NAME_MULTI WHERE TABLE_ID_1 = #{id1} AND TABLE_ID_2 = #{id2}", SqlCommand.CommandType.DELETE);
        command.setResultMapping(new SqlMapping());
        command.setVariables(Lists.newArrayList(
                new MyBatisSqlVariable("#{id1}", "id1", null, null),
                new MyBatisSqlVariable("#{id2}", "id2", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdCallForMap(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("{ CALL selectForMap ( "
                       + "#{id, mode=IN, jdbcType=INTEGER, javaType=Integer}, "
                       + "#{result, mode=OUT, jdbcType=CURSOR, javaType=ResultSet, resultMap=resultMapForMap} "
                       + ") }", SqlCommand.CommandType.SELECT);
        // result is stored in the map in argument..
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable(
                        "#{id, mode=IN, jdbcType=INTEGER, javaType=Integer}",
                        "id",
                        "IN",
                        null),
                new MyBatisSqlVariable(
                        "#{result, mode=OUT, jdbcType=CURSOR, javaType=ResultSet, resultMap=resultMapForMap}",
                        "result",
                        "OUT",
                        "resultMapForMap")));
        command.setArgumentsMapping(Lists.newArrayList(new SqlMapping("result")
                .addPropertyToColumn("id", "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE")));
        command.setResultMapping(new SqlMapping());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdCallForList(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("{ CALL selectForMap ( "
                       + "1, "
                       + "#{result, mode=OUT, jdbcType=CURSOR, javaType=ResultSet, resultMap=resultMapForList} "
                       + ") }", SqlCommand.CommandType.SELECT);
        // result is stored in the list wrapper in argument..
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable(
                "#{result, mode=OUT, jdbcType=CURSOR, javaType=ResultSet, resultMap=resultMapForList}",
                "result",
                "OUT",
                "resultMapForList")));
        command.setArgumentsMapping(Lists.newArrayList(new SqlMapping("result")
                .addPropertyToColumn("id", "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE")));
        command.setResultMapping(new SqlMapping());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdSelectByIdWithConstructor(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME WHERE TABLE_ID = #{id}", SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn(AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT, "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable("#{id}", "id", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdSelectAllLike(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME WHERE TABLE_VALUE LIKE #{value}", SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn("id", "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable("#{value}", "value", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider adddSingleIdSelectAllForeach(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME WHERE TABLE_VALUE IN ( #{item} )", SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn("id", "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable("#{item}", "item", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdSelectAllWhere(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME WHERE TABLE_VALUE IN ( #{item} ) AND TABLE_ID = 4", SqlCommand.CommandType.SELECT);
        command.setResultMapping(new SqlMapping()
                .addPropertyToColumn("id", "TABLE_ID")
                .addPropertyToColumn("value", "TABLE_VALUE"));
        command.setVariables(Lists.newArrayList(new MyBatisSqlVariable("#{item}", "item", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public ExpectedSqlCommandProvider addSingleIdUpdateSet(@Nonnull final Class<?> mapper) {
        final SqlCommand command = new SqlCommand();
        command.setSql("UPDATE TABLE_NAME SET TABLE_VALUE = #{value}, TABLE_ID = #{id} WHERE TABLE_ID = #{id}", SqlCommand.CommandType.UPDATE);
        command.setResultMapping(new SqlMapping());
        command.setVariables(Lists.newArrayList(
                new MyBatisSqlVariable("#{value}", "value", null, null),
                new MyBatisSqlVariable("#{id}", "id", null, null),
                new MyBatisSqlVariable("#{id}", "id", null, null)));
        command.setArgumentsMapping(Lists.newArrayList());

        mappers.add(mapper);
        commands.add(command);

        return this;
    }

    @Nonnull
    public Object[][] build() {
        final List<Object[]> objects = Lists.newArrayList();
        for (int i = 0; i < commands.size(); ++i) {
            objects.add(new Object[] { mappers.get(i), commands.get(i) });
        }
        return objects.toArray(new Object[0][0]);
    }
}

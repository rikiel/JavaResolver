package eu.profinit.manta.connector.java.analysis.common.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.resolver.analysis.DataEndpointFlowInfo;

public class DataEndpointFlowInfoImpl implements DataEndpointFlowInfo {
    private String sql;
    private QueryType queryType = QueryType.NOTHING;
    @Nonnull
    private final ObjectFlowInfo dataSourceFlowInfo = new ObjectFlowInfo();
    @Nonnull
    private final ObjectFlowInfo receiverFlowInfo = new ObjectFlowInfo();
    @Nonnull
    private final ObjectFlowInfo returnObjectFlowInfo = new ObjectFlowInfo();

    @Override
    public boolean isQueryAction() {
        return Arrays.asList(QueryType.QUERY, QueryType.UNKNOWN).contains(queryType);
    }

    @Override
    public Map<String, Set<Object>> getReceiverObjectAttributes() {
        return Collections.unmodifiableMap(receiverFlowInfo.getAttributes().getAttributes());
    }

    @Override
    public Map<String, Set<Object>> getReturnObjectAttributes() {
        return Collections.unmodifiableMap(returnObjectFlowInfo.getAttributes().getAttributes());
    }

    @Override
    public Map<String, Set<Object>> getDataSourceAttributes() {
        return Collections.unmodifiableMap(dataSourceFlowInfo.getAttributes().getAttributes());
    }

    @Override
    public String getDataSourceRep() {
        return sql;
    }

    @Override
    public boolean isUpdateAction() {
        return Arrays.asList(QueryType.UPDATE, QueryType.UNKNOWN).contains(queryType);
    }

    @Override
    public Map<String, Set<Object>> getReceiverObjectFieldsMapping() {
        return Collections.unmodifiableMap(receiverFlowInfo.getFieldsMapping());
    }

    @Override
    public Map<String, Map<String, Set<Object>>> getReceiverObjectFieldsAttributes() {
        return ImmutableMap.of();
    }

    @Override
    public Map<String, Set<Object>> getReturnObjectFieldsMapping() {
        return Collections.unmodifiableMap(returnObjectFlowInfo.getFieldsMapping());
    }

    @Override
    public Map<String, Map<String, Set<Object>>> getReturnObjectFieldsAttributes() {
        return ImmutableMap.of();
    }

    @Override
    public List<Map<String, Set<Object>>> getOutputArgumentFieldsMapping() {
        return ImmutableList.copyOf(receiverFlowInfo.getArgumentFieldsMapping().values());
    }

    @Override
    public List<Integer> getOutputArgumentIndexes() {
        return ImmutableList.copyOf(receiverFlowInfo.getArgumentFieldsMapping().keySet());
    }

    @Override
    public List<Map<String, Set<Object>>> getOutputArgumentAttributes() {
        return IntStream.range(0, getOutputArgumentIndexes().size())
                .mapToObj(i -> receiverFlowInfo.getArgumentAttributes(i).getAttributes())
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Map<String, Set<Object>>>> getOutputArgumentFieldsAttributes() {
        return IntStream.range(0, getOutputArgumentIndexes().size())
                .mapToObj(i -> (Map<String, Map<String, Set<Object>>>) null)
                .collect(Collectors.toList());
    }

    public void setSql(@Nonnull final String sql, @Nonnull final QueryType queryType) {
        Validate.notNullAll(sql, queryType);
        Validate.notEquals(queryType, QueryType.NOTHING);

        this.sql = sql;
        this.queryType = queryType;
    }

    @Nonnull
    public ObjectFlowInfo getDataSourceFlowInfo() {
        return dataSourceFlowInfo;
    }

    @Nonnull
    public ObjectFlowInfo getReceiverFlowInfo() {
        return receiverFlowInfo;
    }

    @Nonnull
    public ObjectFlowInfo getReturnObjectFlowInfo() {
        return returnObjectFlowInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DataEndpointFlowInfoImpl)) {
            return false;
        }

        final DataEndpointFlowInfoImpl that = (DataEndpointFlowInfoImpl) o;

        return new EqualsBuilder()
                .append(sql, that.sql)
                .append(queryType, that.queryType)
                .append(dataSourceFlowInfo, that.dataSourceFlowInfo)
                .append(receiverFlowInfo, that.receiverFlowInfo)
                .append(returnObjectFlowInfo, that.returnObjectFlowInfo)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(sql)
                .append(queryType)
                .append(dataSourceFlowInfo)
                .append(receiverFlowInfo)
                .append(returnObjectFlowInfo)
                .toHashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("sql", sql)
                .add("queryType", queryType)
                .add("dataSourceFlowInfo", dataSourceFlowInfo)
                .add("receiverFlowInfo", receiverFlowInfo)
                .add("returnObjectFlowInfo", returnObjectFlowInfo)
                .toString();
    }

    public enum QueryType {
        QUERY,
        UPDATE,
        NOTHING,
        UNKNOWN
    }
}

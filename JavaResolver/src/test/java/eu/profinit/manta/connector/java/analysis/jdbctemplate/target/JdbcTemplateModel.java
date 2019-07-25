package eu.profinit.manta.connector.java.analysis.jdbctemplate.target;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import eu.profinit.manta.connector.java.analysis.TestWrapper;

public class JdbcTemplateModel {
    private Integer id;
    private String value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Nonnull
    public static JdbcTemplateModel map(@Nonnull final ResultSet resultSet) throws SQLException {
        if (resultSet.isBeforeFirst()) {
            if (!resultSet.next()) {
                throw new IllegalStateException("No data found in result set!");
            }
        }
        final JdbcTemplateModel result = new JdbcTemplateModel();
        result.setId(resultSet.getInt(1));
        result.setValue(resultSet.getString(2));
        return result;
    }

    public void store() {
        TestWrapper.storeValString(getValue(), "OUTPUT_TABLE");
    }
}

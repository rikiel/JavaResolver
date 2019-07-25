package eu.profinit.manta.connector.java.analysis.jdbctemplate.target;

public class JdbcTemplateQueryConstants {
    public static final String PROCEDURE_CALL = "{ CALL selectForMap(?, ?) }";
    public static final String SELECT_ALL = "SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME";
    public static final String SELECT_ROWS_COUNT = "SELECT COUNT(*) FROM TABLE_NAME";
    public static final String SELECT_ROWS_COUNT_FOR_ID = "SELECT COUNT(*) FROM TABLE_NAME WHERE TABLE_ID = ?";
    public static final String SELECT_BY_ID = "SELECT TABLE_ID, TABLE_VALUE FROM TABLE_NAME WHERE TABLE_ID = ?";
    public static final String SELECT_VALUES = "SELECT TABLE_VALUE FROM TABLE_NAME";
    public static final String UPDATE_UNPARAMETRIZED = "UPDATE TABLE_NAME SET TABLE_VALUE='value' WHERE TABLE_ID=2";
    public static final String UPDATE_PARAMETRIZED = "UPDATE TABLE_NAME SET TABLE_VALUE='NEW_VALUE' WHERE TABLE_ID=?";
    public static final String DELETE_BY_ID = "DELETE FROM TABLE_NAME WHERE TABLE_ID = ?";
}

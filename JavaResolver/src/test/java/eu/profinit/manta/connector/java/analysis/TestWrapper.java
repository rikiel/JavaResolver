package eu.profinit.manta.connector.java.analysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Wrapper for standard JDBC API calls
 */
public class TestWrapper {
    public static String loadValString(String tableName) {
        try {
            // load the driver class
            Class.forName("OracleDriverString");
            Connection con = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.16:1521:orcl", "JavaUser", "JavaPass");

            ResultSet rs = con.createStatement().executeQuery("SELECT s_c1, s_c2 FROM " + tableName);
            return rs.getString(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void storeValString(String val, String tableName) {
        try {
            // load the driver class
            Class.forName("OracleDriverString");
            Connection con = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.16:1521:orcl", "JavaUser", "JavaPass");

            PreparedStatement stmt = con.prepareStatement("INSERT INTO " + tableName + " (i_c1, i_c2) VALUES (?, ?)");
            stmt.setString(1, val);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

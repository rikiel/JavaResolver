package examples;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SymbolicAnalysisExample {
    public static void writeValueForIdToFile(int id) throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.16:1521:orcl", "User", "Password");
        PreparedStatement queryStatement = connection.prepareStatement("SELECT ID, VALUE FROM TABLE_NAME WHERE ID = ?");
        queryStatement.setInt(1, id);
        ResultSet resultSet = queryStatement.executeQuery();
        String value = resultSet.getString("VALUE");
        FileWriter output = new FileWriter("outputFile.txt");
        output.write(value);
    }
}

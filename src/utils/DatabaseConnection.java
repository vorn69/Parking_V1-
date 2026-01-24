package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection = null;
    
    // Database configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "5432";
    private static final String DB_NAME = "parking_management";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "123vorn";
    
    // ADD currentSchema PARAMETER HERE
    private static final String URL = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
            "?currentSchema=inet_vehicleparking" +
            "&ssl=false" +
            "&connectTimeout=30" +
            "&socketTimeout=300" +
            "&loginTimeout=10";
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to register PostgreSQL JDBC Driver!");
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                System.out.println("Connecting to database: " + URL);
                connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection established successfully!");
            } catch (SQLException e) {
                System.err.println("Failed to establish database connection!");
                System.err.println("Error: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed!");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
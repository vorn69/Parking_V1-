// src/utils/DatabaseConnection.java
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static Connection connection = null;
    
    // Database configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "5432";
    private static final String DB_NAME = "parking_management";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "123vorn"; // Change to your password
    
    private static final String URL = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    
    static {
        try {
            // Load PostgreSQL JDBC Driver
            Class.forName("org.postgresql.Driver");
            System.err.println("Make sure postgresql-42.7.8.jar is in classpath");
            System.out.println("PostgreSQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to register PostgreSQL JDBC Driver!");
            System.err.println("Make sure postgresql-42.2.29.jre7.jar is in classpath");
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
                System.err.println("URL: " + URL);
                System.err.println("User: " + DB_USER);
                throw e;
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed!");
            } catch (SQLException e) {
                System.err.println("Error closing database connection!");
                e.printStackTrace();
            }
        }
    }
    
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection test: SUCCESS");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection test: FAILED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Please check:");
            System.err.println("1. Is PostgreSQL running?");
            System.err.println("2. Database credentials correct?");
            System.err.println("3. Database 'parking_management' exists?");
        }
        return false;
    }
    
    public static void printDatabaseInfo() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("\n=== Database Information ===");
                System.out.println("Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database Version: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("JDBC Driver: " + conn.getMetaData().getDriverName());
                System.out.println("JDBC Version: " + conn.getMetaData().getDriverVersion());
                System.out.println("URL: " + conn.getMetaData().getURL());
                System.out.println("User: " + conn.getMetaData().getUserName());
                System.out.println("===========================\n");
            }
        } catch (SQLException e) {
            System.err.println("Error getting database information: " + e.getMessage());
        }
    }
    
    // Helper method to check if database exists
    public static boolean databaseExists() {
        Connection tempConn = null;
        try {
            // Try to connect without database name first
            String tempUrl = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/postgres";
            tempConn = DriverManager.getConnection(tempUrl, DB_USER, DB_PASSWORD);
            
            Statement stmt = tempConn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'"
            );
            return rs.next();
        } catch (SQLException e) {
            return false;
        } finally {
            if (tempConn != null) {
                try { tempConn.close(); } catch (SQLException e) {}
            }
        }
    }
    
    // Create database if it doesn't exist
    public static boolean createDatabase() {
        Connection tempConn = null;
        try {
            String tempUrl = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/postgres";
            tempConn = DriverManager.getConnection(tempUrl, DB_USER, DB_PASSWORD);
            
            Statement stmt = tempConn.createStatement();
            stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
            System.out.println("Database '" + DB_NAME + "' created successfully!");
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
            return false;
        } finally {
            if (tempConn != null) {
                try { tempConn.close(); } catch (SQLException e) {}
            }
        }
    }
}
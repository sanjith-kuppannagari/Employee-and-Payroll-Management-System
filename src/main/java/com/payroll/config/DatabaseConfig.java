package com.payroll.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties PROPS = new Properties();

    static {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("src/main/resources/application.properties");
            PROPS.load(fis);
        } catch (IOException e) {
            PROPS.setProperty("db.url", "jdbc:sqlite:payroll.db");
            PROPS.setProperty("server.port", "8080");
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException ignored) {}
            }
        }
    }

    public static String getProperty(String key, String defaultVal) {
        return PROPS.getProperty(key, defaultVal);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getProperty("db.url", "jdbc:sqlite:payroll.db"));
    }

    public static void initDatabase() {
        String createSql = "CREATE TABLE IF NOT EXISTS employees (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL, " +
                "role TEXT NOT NULL, type TEXT NOT NULL, base_pay REAL NOT NULL, extra REAL NOT NULL)";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM employees");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO employees (name,email,role,type,base_pay,extra) VALUES " +
                        "('Aarav Sharma','aarav@techcorp.com','ENGINEERING','FULL_TIME',75000,12000)," +
                        "('Priya Nair','priya@techcorp.com','FINANCE','FULL_TIME',68000,9500)," +
                        "('Rohan Verma','rohan@techcorp.com','OPERATIONS','PART_TIME',450,90)");
            }
        } catch (SQLException e) {
            System.err.println("DB Init Error: " + e.getMessage());
        }
    }
}

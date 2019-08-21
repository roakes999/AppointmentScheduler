/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseproject;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Rob
 */
public class DBConnection {
    private static final String databaseName = "U03tnJ";
    private static final String DB_URL = "jdbc:mysql://52.206.157.109/" + databaseName;
    private static final String username = "U03tnJ";
    private static final String password = "53688080732";
    private static final String driver = "com.mysql.jdbc.Driver";
    static Connection conn;
    
    public static void makeConnection() throws ClassNotFoundException, SQLException, Exception {
        Class.forName(driver);
        conn = (Connection) DriverManager.getConnection(DB_URL, username, password);
        System.out.println("Connection successful");
        String sqlStatement;
        Statement stmt;
        stmt = conn.createStatement();
        sqlStatement = "SET SQL_MODE = '';";
        stmt.executeQuery(sqlStatement);
    }
    
    public static void closeConnection() throws ClassNotFoundException, SQLException, Exception {
        conn.close();
        System.out.println("Connection closed");
    }
}

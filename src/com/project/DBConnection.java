
package com.project;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getC() {

        Connection con = null;

        try {
        	 Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/restaurant",
                "root",
                "Mouni@009"
            );

            System.out.println("Database connected successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return con;
    }
}
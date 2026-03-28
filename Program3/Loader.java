/*
TODO: Now you have to actually make and execute a query
To do this, you make a
    Statement object and
    ResultSet object

The demo, in a try/catch does
    stmt = connection.createStatement()
    answer = stmt.executeQuery(query)

    Then it checks to make sure that the answer isnt null
    It then goes about printing the results of the query.

Instead of sending a query, I need to send insert statements
(dont forget to turn off autocommit first)
*/

import java.io.*;
import java.sql.*;

public class Loader {
    final static String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

    public static void main(String[] args) {
        Connection dbConnection = connectToDB(args);
        //All done. Close the connection
        try { dbConnection.close(); } catch(SQLException e) {
            System.out.println("Error closing the connection");
            System.exit(-1);
        }
    }

    private static Connection connectToDB(String[] args) {
        String username = null, password = null;
        if(args.length == 2) {
            username = args[0]; password = args[1];
        } else {
            System.out.println("Please supply just a username then your password");
            System.exit(-1);
        }
        //Load JDBC driver
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch(ClassNotFoundException e) {
            System.out.println("Error loading JDBC driver. Check that it's in classpath");
            System.exit(-1);
        }
        //Make a Connection to your Oracle DB
        Connection dbConnection = null;
        try {
            dbConnection = DriverManager.getConnection(oracleURL, username, password);
        } catch(SQLException e) {
            System.out.println("Error: could not open JDBC connection");
            System.exit(-1);
        }
        return dbConnection;
    }
}
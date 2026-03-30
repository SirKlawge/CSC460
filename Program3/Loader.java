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

supply the usernaem, password, and 
*/

import java.io.*;
import java.sql.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Loader {
    final static String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

    public static void main(String[] args) {
        //I guess first you should form the query
        String query = formQuery(args[2]);
        Connection dbConnection = connectToDB(args);
        //All done. Close the connection
        try { dbConnection.close(); } catch(SQLException e) {
            System.out.println("Error closing the connection");
            System.exit(-1);
        }
    }

    private static String formQuery(String filename) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch(FileNotFoundException e) {
            System.err.println("Unable to open the csv file");
            System.exit(-1);
        }
        //Read each line of the file, get values, and insert them into the table
        try {
            String currentLine = null;
            while((currentLine = br.readLine()) != null) {
                System.out.println(currentLine);
            }
            //close when all done
            br.close();
        } catch(IOException e) {
            System.err.println("Error reading from the csv file");
            System.exit(-1);
        }
        return "";
    }

    private static Connection connectToDB(String[] args) {
        String username = null, password = null;
        if(args.length >= 2) {
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
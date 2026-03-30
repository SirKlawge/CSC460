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

supply the usernaem, password, csv file name, and last 2 digits of the year
Adding one last littel bit

*/

import java.io.*;
import java.sql.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Loader {
    final static String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

    public static void main(String[] args) {
        //I guess first you should form the query
        String query = formQuery(args);
        //All done. Close the connection
    }

    private static String formQuery(String[] args) {
        String filename = args[2], year = args[3];
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("output.txt", StandardCharsets.UTF_8));
        } catch(IOException e) {
            e.printStackTrace();
        }
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
        } catch(FileNotFoundException e) {
            System.err.println("Unable to open the csv file");
            System.exit(-1);
        }
        //Read each line of the file, get values, and insert them into the table
        try {
            String currentLine = null;
            while((currentLine = br.readLine()) != null) {
                String[] tupleArray = currentLine.split(",");
                String insertString = "insert into vabram.RailIncident" + year + " values (";
                for(int i = 0; i < tupleArray.length -1; i++) { //Dont forget to add last value, an int
                    /*
                    i = 3 are dates
                    7, 10, 11 are integer fields
                    the rest are strings
                    */
                    if(i == 3) insertString += "TO_DATE('" + tupleArray[i] + "', 'YYYY-MM-DD'), ";
                    if(i != 3 && i != 7 && i != 10 && i != 11) insertString += "'" + tupleArray[i] + "',";
                    if(i == 7 || i == 10) insertString += tupleArray[i] + ", ";
                }
                insertString += tupleArray[11] + ");";
                bw.write(insertString);
                bw.newLine();
            }
            //close when all done
            bw.close();
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
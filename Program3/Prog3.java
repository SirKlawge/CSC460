/*
Author: Ventura Abram

So, In order to perform these queries, we need to rip off some of the code found in JDBC.java.


a) easiest, just gotta get the count for the four years
*/

import java.util.Scanner;
import java.io.*;
import java.sql.*;

public class Prog3 {

    static Scanner input;
    static final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
    static String username, password;
    List<String> tableNames;
    
    public static void main(String[] args) {
        validateCredentials(args);
        //Print menu
        printMenu();
        //get query selection
        int querySelection = getSelection();
        //Handle the selection
        handleSelection(querySelection);
    }

    private static void validateCredentials(String[] args) {
        if(args.length == 2) {
            username = args[0];
            password = args[1];
        } else {
            System.out.println("Invalid username and/or password provided at cmd line");
            System.exit(-1);
        }
        return;
    }

    private static void printMenu() {
        System.out.println("You can query anything you want about this database ");
        System.out.println("so long as it's one of the four following queries (enter a, b, c, or d)");
        System.out.println("1) How many incident reports were filed in each of the four years?");
        System.out.println("2) What are the ten states with the most rail incidents in a given year?");
        System.out.println("3) Given two years, which five states had the greatest decrease in rail incidents?");
        System.out.println("4) ");
        return;
    }

    private static int getSelection() {
        input = new Scanner(System.in);
        int selection = input.nextInt();
        //Validate it
        if(selection < 1 || selection > 4) {
            System.out.println("That's not even a selection");
            System.exit(1);
        }
        return selection;
    }

    private static void handleSelection(int querySelection) {
        //Make DB connection here.
        Connection dbConn = makeConnection();
        switch(querySelection) {
            case 1:
                handleSelection1(dbConn);
                break;
            case 2:
                System.out.println("Handling selection 2");
                break;
            case 3:
                System.out.println("Handling selection 3");
                break;
            case 4:
                System.out.println("Handling selection 4");
                break;
            default:
                break;
        }
        closeConnection(dbConn);
        return;
    }

    /*
    So, you gotta create the query.
    Then in a try/catch, you gotta make the statement object and the ResultSet (answer)
    object.
    What is the query?
        How many incident reports were filed in each of the four years?
        select distinct count(*) from vabram.RailIncident80
        select distinct count(*) from vabram.RailIncident95
        select distinct count(*) from vabram.RailIncident10
        select distinct count(*) from vabram.RailIncident25
    */
    private static void handleSelection1(Connection dbConn) {
        //Frist, let's get all four table names
        Statement stmt = null;
        ResultSet answer = null;
        try {
            stmt = dbConn.createStatement();
            answer = stmt.executeQuery("select table_name from user_tables order by table_name");
            ResultSetMetaData answerMetadata = answer.getMetaData();
            System.out.println(answerMetadata);
            stmt.close();
            answer.close();
        } catch(SQLException e) {
            System.out.println("Sql exception in handle selection 1");
            System.exit(-1);
        }
        return;
    }

    private static Connection makeConnection() {
        Connection dbConn = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch(ClassNotFoundException e) {
            System.err.println("Class not found exception.  Probably a problem wuith JDBC oracle driver.");
            System.exit(-1);
        }
        try {
            dbConn = DriverManager.getConnection(oracleURL, username, password);
        } catch(SQLException e) {
            System.err.println("SQL exception");
            System.exit(-1);
        }
        return dbConn;
    }

    private static void closeConnection() {
        try {
            dbConn.close();
        } catch(SQLException e) {
            System.out.println("Error closing the Connection");
            System.exit(-1);
        }
        return;
    }
}
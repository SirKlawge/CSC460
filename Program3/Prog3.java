/*
Author: Ventura Abram

So, In order to perform these queries, we need to rip off some of the code found in JDBC.java.


a) easiest, just gotta get the count for the four years
*/

import java.util.Scanner;
import java.io.*;
import java.sql.*;
import java.util.Comparator;
import java.util.Map;
import java.util.LinkedHashMap;

public class Prog3 {

    static Scanner input;
    static final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
    static String username, password;
    static Map<String, Integer> firstYearMap, secondYearMap;
    
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
                handleSelection2(dbConn);
                break;
            case 3:
                handleSelection3(dbConn);
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
    
    So the ResultSet holds the tuples that result from the query.
    ResultSetMetaData describe column names, count, types.
    You have to iterate over the ResultSet
    */
    private static void handleSelection1(Connection dbConn) {
        //Frist, let's get all four table names
        Statement stmt = null;
        ResultSet tableNames = null;
        try {
            stmt = dbConn.createStatement();
            tableNames = stmt.executeQuery("select table_name from user_tables order by table_name");
            while(tableNames.next()) {
                //Make the query to actually get the counts from the tables
                String tableCountQuery = "select count(*) from ";
                String tableString = tableNames.getString("TABLE_NAME");
                tableCountQuery += tableString;
                Statement newStmt = dbConn.createStatement();
                ResultSet tableCount = newStmt.executeQuery(tableCountQuery);
                tableCount.next();
                System.out.println("There were " + tableCount.getInt(1) + " incidents in " + tableString.substring(12));
                newStmt.close();
                tableCount.close();
            }
            stmt.close();
            tableNames.close();
        } catch(SQLException e) {
            System.out.println("Sql exception in handle selection 1");
            System.exit(-1);
        }
        return;
    }

    /*
    What is the query
        Must display name and quantity of incidents.
        select distinct states from year
        group by state
        order by num incidents
    */
    private static void handleSelection2(Connection dbConn) {
        //Prompt the user for a year
        System.out.println("Which year? (Enter 1980, 1995, 2010, or 2025)");
        int year = input.nextInt();
        if(year == 1980 || year == 1995 || year == 2010 || year == 2025) {
            String queryString = "select StateName, count(*) as count from RailIncident" + year + " group by StateName order by count(*) desc";
            try{
                Statement stmt = dbConn.createStatement();
                ResultSet answer = stmt.executeQuery(queryString);
                ResultSetMetaData colHeaders = answer.getMetaData();
                //Print the col headers
                for(int i = 1; i <= colHeaders.getColumnCount(); i++) {
                    System.out.printf("%-20s", colHeaders.getColumnLabel(i));
                }
                System.out.println();
                int i = 0;
                while(answer.next() && i < 10) {
                    System.out.printf("%-20s%-20s", answer.getString("StateName"), answer.getInt("count"));
                    System.out.println();
                    i++;
                }
                stmt.close();
                answer.close();
            } catch(SQLException e) {
                System.err.println("SQLException in handleSelection 2");
                System.exit(-1);
            }
        } else {
            System.out.println("Sorry, I have no records for that year");
            System.exit(0);
        }
        return;
    }

    private static void handleSelection3(Connection dbConn) {
        //First get the two years
        System.out.println("Which two years? Enter first year (1980, 1995, 2010, 2025)");
        int firstYear = input.nextInt();
        System.out.println("Enter 2nd year: ");
        int secondYear = input.nextInt();
        if(firstYear != 1980 && firstYear != 1995 && firstYear != 2010 && firstYear != 2025) {
            System.out.println("Invalid first year");
            System.exit(0);
        }
        if(secondYear != 1980 && secondYear != 1995 && secondYear != 2010 && secondYear != 2025) {
            System.out.println("Invalid second year");
            System.exit(0);
        }
        if(secondYear == firstYear) {
            System.out.println("Doesn't make sense to pick the same year");
            System.exit(0);
        }
        if(secondYear < firstYear) {
            int temp = firstYear;
            firstYear = secondYear;
            secondYear = temp;
        }
        /*
        Need counts grouped by states.  A map fits this nicely.
        */
        String queryString1 = "select StateName, count(*) as count from RailIncident" + firstYear + " group by StateName order by count desc";
        String queryString2 = "select StateName, count(*) as count from RailIncident" + secondYear + " group by StateName order by count desc";
        firstYearMap = new LinkedHashMap<String, Integer>();
        secondYearMap = new LinkedHashMap<String, Integer>();
        try {
            Statement stmt = dbConn.createStatement();
            ResultSet answer = stmt.executeQuery(queryString1);
            while(answer.next()) {
                firstYearMap.put(answer.getString("StateName"), answer.getInt("count"));
            }
            Statement stmt2 = dbConn.createStatement();
            ResultSet answer2 = stmt2.executeQuery(queryString2);
            while(answer2.next()) {
                secondYearMap.put(answer2.getNString("StateName"), answer2.getInt("count"));
            }
            stmt2.close();
            answer2.close();
            stmt.close();
            answer.close();
        } catch(SQLException e) {
            System.err.println("SQL error in handle section 3");
            System.exit(-1);
        }
        Map<String, Double> changeMap = new LinkedHashMap<>();
        for(String state : firstYearMap.keySet()) {
            if(firstYearMap.get(state) != null && secondYearMap.get(state) != null) {
                int firstYearCount = firstYearMap.get(state);
                int secondYearCount = secondYearMap.get(state);
                double percentChange = ((double)(firstYearCount - secondYearCount) / firstYearCount) * 100;
                changeMap.put(state, percentChange);
            }      
        }
        //Had to learn streams to figure out sorting this stuff.
        changeMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(5).forEach(
            e-> System.out.printf("%s decreased by %.2f\n",e.getKey(), e.getValue())
        );
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
            System.err.println("SQL exception in makeConnection");
            System.exit(-1);
        }
        return dbConn;
    }

    private static void closeConnection(Connection dbConn) {
        try {
            dbConn.close();
        } catch(SQLException e) {
            System.out.println("Error closing the Connection");
            System.exit(-1);
        }
        return;
    }
}
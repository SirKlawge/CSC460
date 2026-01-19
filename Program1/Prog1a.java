/*
Author: Ventura Abram

create a binary file version of the provided Dataset2.csv
records should be sorted in ascending order by Data.entry
Chose your own sorting algo.  Java provides .sort().

The binary file name should be the input file's name but end in .bin

Field types are limited to int, double, and String.

Pad strings on the right with space to reach the needed lengths

For alphanumeric fields, you need to first iterate through the records
to determine the maximum string length.  Store the max string length somewhere in 
THE BINARY FILE.  Best to store it at the end of the binary file.

The pathname and file name will be provided as a CL arg.  

The complete pathname for our data file is /home/cs460/spring26/Dataset2.csv

Dr. McCann suggests that we set a flag for when we're inside double quotes


----------------------------------------------------------------------------------------------------
For part b, we'll have to print certain field of
    THe first four records
    The middle for or 5 records (compute mid point then offset from here on both sides of it)
    and the last four records
For this, we'll need to know the total numRecords

For part b, we'll also have to compute the total number of records from the file size. Display 
the number on a new line.  Sounds like we'll have to know total bits that the records require 
and we'll have to know the the numBits per record.

Finally for part b, we'll have to print certain fields with the 10 caves furthest from the equator
We're given latitude values, so the 10 greatest latitude values.
List should be in desscending order of distance: furthest to closest
If there are ties, display the ties: this means you may end up displaying more than 10

If there are less than 10 records, then only print as many as exist for the four groups that we're printing.
So if there are two records, then we'll print the two records
    1) for the first four group
    2) for the middle four/five group
    3) for the last four group
    4) for the 10 most distant from equator group

User must be able to provide 0 or more Data.entry values. For each value
    1) locate with the binary file using exponential binary search
    2) display to the screen certain fields ID'd by the given Data.entry or display
    "not found"

If a field doesn't have a value, store -1000 or "null" there
Output one record per line, each field surrounded by square brackets
seek() method in Java API should help find middle and last records of binary files
Use a loop to prompt the user for Data.entry values.  Terminate when -1000 is entered as the value

*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

public class Prog1a {
    /*
    You need at least one pass over the file before you start writing the bin file.
    This is to determine the max length of the string fields

    This means that we need to store our records
    */
    public static void main(String[] args) {
        //First open the file
        String fileName = args[0];
        BufferedReader buffReader;
        try {
            buffReader = new BufferedReader(new FileReader(fileName));
            //To be deleted: store the headers on the first line for now
            Map<String, Integer> headerMap = storeHeaders(buffReader.readLine());
            //Then read all of the lines to produce the list of Records
            List<Record> recordList = makeRecordList(buffReader)
            buffReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static makeRecordList(BufferedReader buffReader) throws IOException {
        String currentLine;
        while((currentLine = buffReader.readLine()) != null) {

        }
    }

    /*
    TODO: you can delete this later, perhaps, since this line won't be written to the 
    final bin file.  This might just be useful later as I write this.
    */
    private static Map<String, Integer> storeHeaders(String headerLine) {
        Map<String, Integer> headerMap = new HashMap<String, Integer>();
        String[] headers = headerLine.split(",");
        for(int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i], i);
        }
        return headerMap;
    }
}

public class BinaryFileStats {
    private int numLines;
}
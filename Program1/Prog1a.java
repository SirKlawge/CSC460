/*
Author: Ventura Abram
CSC460
Program1a
Instructor: Professor McCann
TAs: Jianwei Shen, Muhammad Bilal
Due: 1/22/2026

This program has the sole purpose of producing a binary file for a particular csv file 
that holds info about bat caves.  The input file can be located anywhere, but its path 
and file name should be supplied as the first command line argument.  The output bin file 
will be stored in the same directory as this source file.  Written in Java 25.

------------------For Ventura's future reference-----------------------------------------
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
import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

public class Prog1a {
    /*
    Class: BinaryFileStats
    Author: Ventura Abram
    This is really just a bundle of metadata about the input file that I can pass around 
    from method to method as I build the bin file.
    Fields:
        numFields - an integer representing the number of fields/columns in the csv file
        maxLengths - an integer array where each index holds the lengths of the largest value
        for its field.  If you number the fields as they appear in the csv file starting at 0, 
        then the elements in this array correspond to the fields at that index.

    Method: setNumFields: a setter for numFields field
    */
    public static class BinaryFileStats {
        private int numFields;
        private int[] maxLengths;

        public void setNumFields(int numFields) {this.numFields = numFields; return;}
    }
    
    /*
    Method: main
    Purpose: This drives everything from reading in the csv file to producing the bin file.
    Precondition: the csv file exists
    Postcondition: a bin file exists in the current directory.
    Parameter:
        args - a String array of command line args.  args[0] is our csv file path/name
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
            BinaryFileStats fileStats = new BinaryFileStats();
            fileStats.setNumFields(headerMap.size());
            fileStats.maxLengths = new int[fileStats.numFields];
            List<Record> recordList = makeRecordList(buffReader, fileStats);
            //Now traverse the recordList to pad all of the Strings.
            padAllStrings(recordList, fileStats);
            //Write the binary file
            writeBinaryFile(fileName, recordList, fileStats);
            buffReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Method: writeBinaryFile
    Purpose: This method creates the binary file, writes the Record info to it from the recordList, 
    and finally writes the field size info at the very end.
    Precondition: recordList must contain 0 or more records from the csv file.  fileStats should 
    contain the max lengths of all fields.
    Postcondition: a new binary file with the same name as the input csv file (except now with a .bin 
    extension) will exist in the same directory as this java source file.
    Parameters:
        fileName: the name of the input csv file
        recordList: the ArrayList<Record> that we've built from the input csv file
        fileStats: a BinaryFileStats object. It's a bundle of metadata about the records.
    Return: none
    */
    private static void writeBinaryFile(String fileName, List<Record> recordList, BinaryFileStats fileStats) {
        File binFileName = null; //Really, it's more than a name. IT'S A FILE
        RandomAccessFile binFile = null;  //I guess a bin file is a type of RAF
        String[] filePathArr = fileName.split("/");
        fileName = filePathArr[filePathArr.length - 1].split("\\.")[0];
        //Delete any existing old bin file
        try {
            binFileName = new File(fileName + ".bin");
            if(binFileName.exists()) {
                binFileName.delete();
            }
        } catch(Exception e) {
            System.out.println("unable to delete an existing file.");
            e.printStackTrace();
        }

        try {
            binFile = new RandomAccessFile(binFileName, "rw");
        } catch(IOException e) {
            System.out.println("Error occurred while trying to create the bin file.");
            e.printStackTrace();
        }
        try {
            for(Record r: recordList) {
                binFile.writeInt(r.getDatasetSeqID());
                binFile.writeBytes(r.getDataEntry());
                binFile.writeBytes(r.getCaveDataSeries());
                binFile.writeBytes(r.getBiogRealm());
                binFile.writeBytes(r.getContinent());
                binFile.writeBytes(r.getBiomeClass());
                binFile.writeBytes(r.getCountry());
                binFile.writeBytes(r.getCaveSite());
                binFile.writeDouble(r.getLattitude());
                binFile.writeDouble(r.getLongitude());
                binFile.writeBytes(r.getSpeciesName());
            }
            //Then store the field length of each column from maxLengths
            for(int i  = 0; i < fileStats.maxLengths.length; i++) {
                binFile.writeInt(fileStats.maxLengths[i]);
            }
        } catch(IOException e) {
            System.out.println("Error writing to the binary file");
            e.printStackTrace();
        }

        try {
            binFile.close();
        } catch(IOException e) {
            System.out.println("Could not close binary file");
            e.printStackTrace();
        }
    }

    /*
    Method: padAllStrings
    Purpose: this method appends spaces to each field value until it is equal to the length 
    of the longest string found in that column.  
    Precondition: All field values under a specific category have a length no greater than 
    the length stored in fileStats.maxLength at the corresponding index.
    Postcondition: All field values uinder a specific category all have equal length, which is 
    exactly fileStats.maxLength at the corresponding index.
    Parameters:
        recordList: the ArrayList<Record> that we've built from the input csv file
        fileStats: a BinaryFileStats object. It's a bundle of metadata about the records
    Returns: none
    1 - DataEntry
    2 - CaveDataSeries
    3 - BiogRealm
    4 - Continent
    5 - BiomeClass
    6 - Country
    7 - CaveSite
    10 - SpeciesName
    */
    private static void padAllStrings(List<Record> recordList, BinaryFileStats fileStats) {
        for(Record r : recordList) {
            r.setDataEntry(String.format("%-" + fileStats.maxLengths[1] + "s", r.getDataEntry()));
            r.setCaveDataSeries(String.format("%-" + fileStats.maxLengths[2] + "s", r.getCaveDataSeries()));
            r.setBiogRealm(String.format("%-" + fileStats.maxLengths[3] + "s", r.getBiogRealm()));
            r.setContinent(String.format("%-" + fileStats.maxLengths[4] + "s", r.getContinent()));
            r.setBiomeClass(String.format("%-" + fileStats.maxLengths[5] + "s", r.getBiomeClass()));
            r.setCountry(String.format("%-" + fileStats.maxLengths[6] + "s", r.getCountry()));
            r.setCaveSite(String.format("%-" + fileStats.maxLengths[7] + "s", r.getCaveSite()));
            r.setSpeciesName(String.format("%-" + fileStats.maxLengths[10] + "s", r.getSpeciesName()));
        }
    }

    /*
    Name: makeRecordList
    Purpose: This method reads every line of the csv file then calls another method which 
    converts the record into a Record object.  This then adds each Record object to an 
    ArrayList of Records.
    Preconditon: the input csv file has 0 or more records
    Postcondition: We've read through all of the records from the CSV file.
    Parameters:
        buffReader - a BufferedReader that allows the reading of a new line from the csv file
        fileStats: a BinaryFileStats object. It's a bundle of metadata about the records
    Return:
        recordList - the ArrayList<Record> that we've built from the input csv file
    */
    private static List<Record> makeRecordList(BufferedReader buffReader, BinaryFileStats fileStats) throws IOException {
        List<Record> recordList = new ArrayList<Record>();
        String currentLine;
        while((currentLine = buffReader.readLine()) != null) {
            //Break down currentLine into its comma-sep data parts
            Record newRecord = makeRecordFromLine(currentLine, fileStats);
            recordList.add(newRecord);
        }
        return recordList;
    }

    /*
    Name: makeRecordFromLine
    Purpose: We have to comb through every char of the file to properly parse out the field values.
    This method does so with the the provided line from the csv file and forms a Record object from it.
    The record will then go into the recordList
    Precondition: there's another line to be read from the csv file
    Postcondition: All fields of the resultant Record object are furnished with a value
    Parameters:
        currentLine: a String representation of a record from the csv file
        fileStats: a BinaryFileStats object that will keep track of metadata regarding the records
    Return:
        newRecord: a Record object that holds info regarding the record represented by the currentLine
    */
    private static Record makeRecordFromLine(String currentLine, BinaryFileStats fileStats) {
        Record newRecord;  //The Record to be returned
        boolean inQuotes = false;  //Flag indicating a quotation
        StringBuilder sb = new StringBuilder(); //For building field values
        char currentChar; //current char in currentLine
        String[] recordStrings = new String[fileStats.numFields];  //Collection of field values from currentLine
        int recordStringsIdx = 0;  //index in recordStrings
        for(int i = 0; i < currentLine.length(); i++) {
            currentChar = currentLine.charAt(i);
            //First check to see if you're in quotes
            if(currentChar == '"') {
                //Flip the value and move on to the next char.
                inQuotes = !inQuotes;
                continue;
            }
            if(currentChar == ',') {
                if(!inQuotes) {
                    //StringBuilder complete. Add to String array and clear it
                    recordStrings[recordStringsIdx] = sb.toString();
                    //We're gonna use $null as a signal that we have to replace the current field with null or -1000
                    if(recordStrings[recordStringsIdx].length() == 0) {recordStrings[recordStringsIdx] = "$null";}
                    //Here, the length should be saved if it's greater than the current maxLength
                    //update maxLengths in fileStats
                    if(recordStrings[recordStringsIdx].length() > fileStats.maxLengths[recordStringsIdx]) {
                        fileStats.maxLengths[recordStringsIdx] = recordStrings[recordStringsIdx].length();
                    }
                    recordStringsIdx++;
                    sb.setLength(0);
                    continue;
                }
            }
            //Here, add the currentChar to sb
            sb.append(currentChar);
        }
        //take care of the last field
        recordStrings[recordStringsIdx] = sb.toString();
        if(recordStrings[recordStringsIdx].length() == 0) {recordStrings[recordStringsIdx] = "$null";}
        if(recordStrings[recordStringsIdx].length() > fileStats.maxLengths[recordStringsIdx]) {
            fileStats.maxLengths[recordStringsIdx] = recordStrings[recordStringsIdx].length();
        }
        //recordStrings complete. Convert to record
        newRecord = furnishRecordFields(recordStrings);
        return newRecord;
    }

    /*
    Method: furnishRecordFields
    Purpose: This method sets all of the fields for the a Record object based on 
    the the Strings stored in recordStrings.  If an empty String is detected, then
    the field will have either -1000 or "null" in that field if it's a numeric field 
    or alphanumeric field respectively.
    Precondition: we've saved all of the field values from the csv file into the 
    recordStrings String array.
    Postconditon: the newRecord object has all fields furnished with their proper values.
    Parameter:
        recordStrings - a String array that contains all of the field values
    Return: newRecord - a Record object that is built from the recordString values.
    */
    private static Record furnishRecordFields(String[] recordStrings) {
        Record newRecord = new Record();
        if(recordStrings[0].equals("$null")) {
            newRecord.setDatasetSeqID(-1000);
        } else {
            newRecord.setDatasetSeqID(Integer.parseInt(recordStrings[0]));
        }

        if(recordStrings[1].equals("$null")) {
            newRecord.setDataEntry("null");
        } else {
            newRecord.setDataEntry(recordStrings[1]);
        }

        if(recordStrings[2].equals("$null")) {
            newRecord.setCaveDataSeries("null");
        } else {
            newRecord.setCaveDataSeries(recordStrings[2]);
        }

        if(recordStrings[3].equals("$null")) {
            newRecord.setBiogRealm("null");
        } else {
            newRecord.setBiogRealm(recordStrings[3]);
        }

        if(recordStrings[4].equals("$null")) {
            newRecord.setContinent("null");
        } else {
            newRecord.setContinent(recordStrings[4]);
        }

        if(recordStrings[5].equals("$null")) {
            newRecord.setBiomeClass("null");
        } else {
            newRecord.setBiomeClass(recordStrings[5]);
        }

        if(recordStrings[6].equals("$null")) {
            newRecord.setCountry("null");
        } else {
            newRecord.setCountry(recordStrings[6]);
        }

        if(recordStrings[7].equals("$null")) {
            newRecord.setCaveSite("null");
        } else {
            newRecord.setCaveSite(recordStrings[7]);
        }
        
        if(recordStrings[8].equals("$null")) {
            newRecord.setLattitude(-1000);
        } else {
            newRecord.setLattitude(Double.parseDouble(recordStrings[8]));
        }
        
        if(recordStrings[9].equals("$null")) {
            newRecord.setLongitude(-1000);
        } else {
            newRecord.setLongitude(Double.parseDouble(recordStrings[9]));
        }
        
        if(recordStrings[10].equals("$null")) {
            newRecord.setSpeciesName("null");
        } else {
            newRecord.setSpeciesName(recordStrings[10]);
        }
        return newRecord;
    }

    /*
    Method: storeHeaders
    Purpose: this isnt the most essential to the project, but at least it gets the size 
    of the amount of columns.
    Precondition: A csv file should exist and we've read the first line
    Postcondition: We have a HashMap with an amount of entries equal to the total number of columns
    in the csv file.
    Parameters:
        headerLine - a String representing the first line of the csv file
    Return:
        headerMap - a HashMap<String, Integer> that maps each header string to 
        it's position in the sequence of header strings in the csv file.
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
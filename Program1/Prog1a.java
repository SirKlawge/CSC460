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
    This is really just a bundle of data about the input file that I can pass around 
    from method to method as I build the bin file.
    */
    public static class BinaryFileStats {
        private int numFields;
        private int numLines;
        private int[] maxLengths;

        public void setNumFields(int numFields) {this.numFields = numFields; return;}
        public void setNumLines(int numLines) {this.numLines = numLines; return;}
    }

    public static class Record {
        private int DatasetSeqID;
        private String DataEntry;
        private String CaveDataSeries;
        private String BiogRealm;
        private String Continent;
        private String BiomeClass;
        private String Country;
        private String CaveSite;
        private double Lattitude;
        private double Longitude;
        private String SpeciesName;

        public void setDatasetSeqID(int DatasetSeqID) {this.DatasetSeqID = DatasetSeqID; return;}
        public void setDataEntry(String DataEntry) {this.DataEntry = DataEntry; return;}
        public void setCaveDataSeries(String CaveDataSeries) {this.CaveDataSeries = CaveDataSeries; return;}
        public void setBiogRealm(String BiogRealm) {this.BiogRealm = BiogRealm; return;}
        public void setContinent(String Continent) {this.Continent = Continent; return;}
        public void setBiomeClass(String BiomeClass) {this.BiomeClass = BiomeClass; return;}
        public void setCountry(String Country) {this.Country = Country; return;}
        public void setCaveSite(String CaveSite) {this.CaveSite = CaveSite; return;}
        public void setLattitude(double Lattitude) {this.Lattitude = Lattitude; return;}
        public void setLongitude(double Longitude) {this.Longitude = Longitude; return;}
        public void setSpeciesName(String SpeciesName) {this.SpeciesName = SpeciesName; return;}
    }
    
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
            BinaryFileStats fileStats = new BinaryFileStats();
            fileStats.setNumFields(headerMap.size());
            List<Record> recordList = makeRecordList(buffReader, fileStats);
            buffReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Record> makeRecordList(BufferedReader buffReader, BinaryFileStats fileStats) throws IOException {
        List<Record> recordList = new ArrayList<Record>();
        String currentLine;
        while((currentLine = buffReader.readLine()) != null) {
            //Break down currentLine into its comma-sep data parts
            Record newRecord = makeRecordFromLine(currentLine, fileStats);
            System.out.println("Species: " + newRecord.SpeciesName);
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
        fileStats.maxLengths = new int[fileStats.numFields];
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
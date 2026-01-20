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
        String currentLine;
        while((currentLine = buffReader.readLine()) != null) {
            //Break down currentLine into its comma-sep data parts
            Record newRecord = makeRecordFromLine(currentLine, fileStats);
        }
        return null;
    }

    /*
    If you're not in quotes, you just want to scan chars until you find a comma, then you add 
    the string you built to the proper slot in a String array of size 11
    If you are in quotes, then you'll add even the commas to the string buffer until you encounter 
    another double quote.  At that point, you'll move on to the next iter, scan a comma, and be done with the field

    TODO: continue from here.
    */
    private static Record makeRecordFromLine(String currentLine, BinaryFileStats fileStats) {
        Record newRecord;
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        char currentChar;
        String[] recordStrings = new String[fileStats.numFields];
        int recordStringsIdx = 0;
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
                    //TODO: update maxLengths in fileStats
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
        //recordStrings complete. Convert to record
        newRecord = furnishRecordFields(recordStrings);
        return newRecord;
    }

    private static Record furnishRecordFields(String[] recordStrings) {
        Record newRecord = new Record();
        newRecord.setDatasetSeqID(Integer.parseInt(recordStrings[0]));
        newRecord.setDataEntry(recordStrings[1]);
        newRecord.setCaveDataSeries(recordStrings[2]);
        newRecord.setBiogRealm(recordStrings[3]);
        newRecord.setContinent(recordStrings[4]);
        newRecord.setBiomeClass(recordStrings[5]);
        newRecord.setCountry(recordStrings[6]);
        newRecord.setCaveSite(recordStrings[7]);
        newRecord.setLattitude(Double.parseDouble(recordStrings[8]));
        newRecord.setLongitude(Double.parseDouble(recordStrings[9]));
        newRecord.setSpeciesName(recordStrings[10]);
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
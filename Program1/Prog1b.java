/*
Author: Ventura Abram

The fields we care about are 
    For 4/5/4:
        DataSeqID
        Country
        Cave site
    
    Then display the total number of records

    Then the list of the 10 distinct caves that are furthest from the equator 
    (no single cave should appear more than once).  Fields we care about here:
        Country
        Cave site
        Latitude
    Lengthen list if there are ties with the 10th most distant cave
    Records are listed one per line.  Each field encased in []

    Then you gotta start a loop that takes input.  The input should be Data.entry values.
    You'll do an exponential binary search for the value.  If found, you'll display
        Country
        Cave Site
        Species name
    If not found, just display "not found".
*/

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.nio.charset.StandardCharsets;
//TODO: uncomment this import when it's time to start sorting records.
//import Prog1a.Record;

public class Prog1b {
    private static final byte SIZE_OF_INT = 4;  //an int is 4 bytes
    private static final byte NUM_COLUMNS = 11; //the csv file has 11 fields

    public static void main(String[] args) {
        String fileName = args[0];
        //Try to open the binary file
        File binFile = makeFile(fileName);
        //Use an RAF object to get the length of the binary file
        RandomAccessFile rafReader = makeRafReader(binFile);
        //Get the size of the file
        long fileSize = getFileSize(rafReader);
        //The last line of the file is the metadata array.  Should have 11 ints
        long startOfMaxLengths = fileSize - (SIZE_OF_INT * NUM_COLUMNS);
       //Store the metadata in an array
       int[] maxLengths = readMaxLengths(rafReader, startOfMaxLengths);
       //You wrote the bin file with writeBytes(): 1 byte per char.
       //Calculate the recordSize based on maxLengths
       int recordSize = calculateRecordSize(maxLengths);
       //Could add an assert to validate the record size, but I'll just do it myself.
       validateRecordSize(recordSize, startOfMaxLengths);
       long numRecords = startOfMaxLengths / recordSize;
       //Now we can get first 4, middle 4/5 and last 4 records.
       printStartMiddleEnd(rafReader, numRecords, recordSize, maxLengths);
    }

    private static File makeFile(String fileName) {
        File binFile = null;
        try {
            binFile = new File(fileName);
        } catch(Exception e) {
            System.out.println("Error while opening the file");
            e.printStackTrace();
        }
        return binFile;
    }

    private static RandomAccessFile makeRafReader(File binFile) {
        RandomAccessFile rafReader = null;
        try {
            rafReader = new RandomAccessFile(binFile, "rw");
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return rafReader;
    }

    /*
    
    */
    private static long getFileSize(RandomAccessFile rafReader) {
        long fileSize = 0;
        try {
            fileSize = rafReader.length();
            if(fileSize == 0) {
                System.exit(1);
            }
        } catch(IOException e) {
            System.out.println("Error getting the fileSize");
            e.printStackTrace();
        }
        return fileSize;
    }


    private static int[] readMaxLengths(RandomAccessFile rafReader, long startOfMaxLengths) {
        int[] maxLengths = new int[11];
        try {
            //Seek to the start of the maxLengths data
            rafReader.seek(startOfMaxLengths);
            for(int i = 0; i < maxLengths.length; i++) {
                maxLengths[i] = rafReader.readInt();
            }
        } catch(IOException e) {
            System.out.println("Error reading in maxLengths");
            e.printStackTrace();
        }
        return maxLengths;
    }

    /*
    0 - an int: 4 bytes DataSeqID
    1 - DataEntry
    2 - CaveDataSeries
    3 - BiogRealm
    4 - Continent
    5 - BiomeClass
    6 - Country
    7 - CaveSite
    8&9 - doubles each: 8 x 2 = 16 bytes 
    10 - SpeciesName
    */
    private static int calculateRecordSize(int[] maxLengths) {
        int recordSize = 20; //4 + 16 from numeric fields
        //Now add 1 x maxLengths[i] for each String field
        for(int i = 1; i < maxLengths.length; i++) {
            if(i != 8 && i != 9) {
                recordSize += maxLengths[i];
            }
        }
        return recordSize;
    }

    private static void validateRecordSize(int recordSize, long allRecordsSize) {
        long remainder = allRecordsSize % recordSize;
        if(remainder != 0) {
            System.out.println("Something went wrong with the record sizes.");
            System.exit(1);
        }
        return;
    }
    
    private static void printStartMiddleEnd(RandomAccessFile rafReader, long numRecords, int recordSize, int[] maxLengths) {
        //First seek() to 0 then read in the first four records
        seekWrapper(rafReader, 0);
        long startOfRecord = 0;
        long currentLoc = 0;
        for(int i = 0; i < 4; i++) {
            try {
                currentLoc = rafReader.getFilePointer();
            } catch(IOException e) {
                e.printStackTrace();
            }
            if((currentLoc / recordSize) < numRecords) { //Only print if we haven't reached eof
                //read and print DataSeq, Country, and CaveSite
                printSMEHelper(rafReader, maxLengths);
                //Seek to the start of the next record
                seekWrapper(rafReader, startOfRecord + recordSize);
                startOfRecord += recordSize;  //updates startOfRecord for next iteration   
            } else {
                break;
            }
        }
        //Get the middle 4 or 5
        int middleAmount = (numRecords % 2 == 0) ? 4 : 5;
        //Seek to the start of the middleRecord - 2
        startOfRecord = ((numRecords / 2) - 2) * recordSize;
        if(startOfRecord < 0) startOfRecord = 0;
        seekWrapper(rafReader, startOfRecord);
        for(int i = 0; i < middleAmount; i++) {
            try {
                currentLoc = rafReader.getFilePointer();
            } catch(IOException e) {
                e.printStackTrace();
            }
            if((currentLoc / recordSize) < numRecords) {
                printSMEHelper(rafReader, maxLengths);
                seekWrapper(rafReader, startOfRecord + recordSize);
                startOfRecord += recordSize;
            } else {
                break;
            }
        }
        //Print last 4 records
        startOfRecord = (numRecords - 4) * recordSize;
        if(startOfRecord < 0) startOfRecord = 0;
        seekWrapper(rafReader, startOfRecord);
        for(int i = 0; i < 4; i++) {
            try {
                currentLoc = rafReader.getFilePointer();
            } catch(IOException e) {
                e.printStackTrace();
            }
            if((currentLoc / recordSize) < numRecords) {
                printSMEHelper(rafReader, maxLengths);
                seekWrapper(rafReader, startOfRecord + recordSize);
                startOfRecord += recordSize;
            } else {
                break;
            }
        }
    }

    private static void seekWrapper(RandomAccessFile rafReader, long destination) {
        try {
            rafReader.seek(destination);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return;
    }

    

    /*
    Precondition: the rafReader pointer must be at the start of a record such that one should be able 
    to immediately call readInt() to get the record's DataSeqID.
    */
    private static void printSMEHelper(RandomAccessFile rafReader, int[] maxLengths) {
        byte[] countryBuffer = new byte[maxLengths[6]]; //For storing the country string
        byte[] caveSiteBuffer = new byte[maxLengths[7]]; //for storing the caveSite string
        long startOfRecord = 0;
        try {
            startOfRecord = rafReader.getFilePointer();
        } catch(IOException e) {
            e.printStackTrace();
        }
        //Get offsets for Country and CaveSite
        int countryOffset = 0, caveSiteOffset = 0, offset = 4;
        for(int i = 1; i <= 7; i++) {
            if(i == 6) countryOffset = offset;
            if(i == 7) {
                caveSiteOffset = offset;
                break;
            }
            offset += maxLengths[i];
        }
        //We simply read in and print the int that's right in front of us.
        try{
            int DataSeqID = rafReader.readInt();
            System.out.print("[" + DataSeqID + "]");
            rafReader.seek(startOfRecord + countryOffset);
            rafReader.readFully(countryBuffer);
            String country = new String(countryBuffer, StandardCharsets.UTF_8);
            System.out.print("["  + country.trim() + "]");
            rafReader.seek(startOfRecord + caveSiteOffset);
            rafReader.readFully(caveSiteBuffer);
            String caveSite = new String(caveSiteBuffer, StandardCharsets.UTF_8);
            System.out.println("["  + caveSite.trim() + "]");
        } catch(IOException e) {
            e.printStackTrace();
        }
        return;
    }

}
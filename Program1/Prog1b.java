/*
Author: Ventura Abram
CSC460
Program1b
Instructor: Professor McCann
TAs: Jianwei Shen, Muhammad Bilal
Due: 1/29/2026

This program takes the binary file produced by Program1a and pulls information from it.
Namely, it'll print info from the first four records, the middle four or five records, and 
finally the last four records.
Next, it'll print out the ten caves most distant from the equator in descending order (might
print more if there's a tie for the tenth element).
Lastly, the program will enter a loop where it will prompt the user for a Data.entry value, 
and it will print info about a cave that matches that value if it's in the database.
*/

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.Scanner;

public class Prog1b {
    private static final byte SIZE_OF_INT = 4;  //an int is 4 bytes
    private static final byte NUM_COLUMNS = 11; //the csv file has 11 fields

    /*
    Method: main
    Purpose: this drives the retrieval and printing of data from the bin file as described above
    Precondition: Prog1a has produced a .bin file with the same name as the csv file
    Postconditon: none
    Parameter:
        args - a String array of command line args.  args[0] is our csv file path/name
    */
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
        //Display numRecords
        System.out.println("Total records: " + numRecords);
        //I guess it was inevitable that I would need a Record list.
        Map<String, Record> recordMap = new HashMap<String, Record>();
        List<Record> recordList = makeRecordList(rafReader, numRecords, maxLengths, recordMap);
        List<Record> sortedByLattitude = new ArrayList<Record>(recordMap.values());
        sortedByLattitude.sort(null);
        printSortedByLat(sortedByLattitude);
        //Now get input from the user
        handleUserQueries(rafReader, numRecords, recordSize, maxLengths);
    }

    /*
    Method: handleUserQueries
    Purpose: This method runs a while loop that will prompt the user for a Data.entry value and 
    it will call a method that searches for it.  If the user enters -1000, the loop will exit.
    Precondition: there's a bin file to search through
    Postconditon: none
    Parameters:
        rafReader - a RandomAccessFile object that reads the bin file.
        numRecords - a long that represents the number of records in the file
        recordSize - the size, in bytes, of one record from the file
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
    */
    private static void handleUserQueries(RandomAccessFile rafReader, long numRecords, int recordSize, int[] maxLengths) {
        Scanner scanner = new Scanner(System.in);
        String query = null;
        while(true) {
            System.out.println("Find a cave by Data.entry value:");
            System.out.println("(Hint: Search \"BC \" then a number that's at least 002. Type -1000 to quit)");
            query = scanner.nextLine();
            if(query.equals("-1000")) {
                break;
            } else {
                //Perform exponential binary search here
                searchQuery(rafReader, query, numRecords, recordSize, maxLengths);
            }
        }
        return;
    }

    /*
    Method: searchQuery
    Purpose: this method establishes the range, [lowerBound, upperBound], in which the query target
    must live and then calls a method to do a binary search of the bin file for a record that 
    matches the query.
    Preconditon: the user has provided, at the command line, a query that begins with "BC " followed 
    by a number ranging from 002 to however many Data.entry values there are.
    Postcondition: either a match has been found or we report that not match was found
    Parameters:
        rafReader - a RandomAccessFile object that reads the bin file.
        query - a String that the user provided at the command line that we're searching for in the file.
        numRecords - a long that represents the number of records in the file
        recordSize - the size, in bytes, of one record from the file
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
    */
    private static void searchQuery(RandomAccessFile rafReader, String query, long numRecords, int recordSize, int[] maxLengths) {
        byte[] dataEntryBuffer = new byte[maxLengths[1]];
        String dataEntryStr = null;
        String[] splitResult = new String[2];
        splitResult = query.split(" ");
        long queryNumber = Long.parseLong(splitResult[1]);
        if(!splitResult[0].equals("BC")) {
            System.out.println("Record not found.");
            return;
        }
        //Find the range in which query should live
        long upperBound = 0, lowerBound = 0, dataEntryNumber = 0;
        //validate the query a little
        try {
            rafReader.seek((numRecords - 1) * recordSize + 4);
            rafReader.readFully(dataEntryBuffer);
            dataEntryStr = new String(dataEntryBuffer, StandardCharsets.UTF_8);
            splitResult = dataEntryStr.split(" ");
            dataEntryNumber = Long.parseLong(splitResult[1]);
            if(queryNumber > dataEntryNumber) {
                System.out.println("Data.entry values don't go that high.");
                return;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        /*
        upperBound 0 will put you just before the 1st record, ready to read it
        upperBound 1 will put you just before the 2nd record, ready to read it
        */
        for(int i = 0; upperBound < numRecords; i++) {
            //Scan this record's Data.entry field
            try {
                rafReader.seek((upperBound * recordSize) + 4);
                //Get the actual number from the field
                rafReader.readFully(dataEntryBuffer);
                dataEntryStr = (new String(dataEntryBuffer, StandardCharsets.UTF_8)).trim();
                splitResult = dataEntryStr.split(" ");
                dataEntryNumber = Long.parseLong(splitResult[1]);
                if(dataEntryNumber == queryNumber) {
                    //Here, the record at upperBound happened to be a search hit
                    printSearchHit(rafReader, upperBound, recordSize, maxLengths);
                    return;
                } else if(dataEntryNumber > queryNumber) {
                    //Here, we've overshot the search target. So we have an upper bound
                    //set the lower bound and exit the loop
                    lowerBound = 2 * ((long) Math.pow(2, i -1) -1);
                    break;
                } else {
                    lowerBound = 2 * ((long) Math.pow(2, i -1) -1);
                    upperBound = 2 * ((long) Math.pow(2, i + 1) -1);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        if(upperBound > numRecords - 1) upperBound = numRecords;
        //At this point, lower and upperBounds should kinda correspond to the DatasetSeq numbers
        binarySearch(rafReader, queryNumber, recordSize, maxLengths, lowerBound, upperBound);
        return;
    }

    /*
    Method: binarySearch
    Purpose: this method does a regular old binary search between the lowerBound and upperBound 
    calculated by the searchQuery method.  If there's a search hit, then it'll call a method 
    to print data relevant to the corresponding record.  Else it'll print "record not found".
    Preconditon: the lowerBound and upperBound have been calculated
    Postcondition: none
    Parameters:
        rafReader - a RandomAccessFile object that reads the bin file.
        queryNumber - a long that represents the number found in the user's search query.
        recordSize - the size, in bytes, of one record from the file
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
        lowerBound - an int that represents the lower bound of the range wherein the query target must live
        upperBound - an int that represents the upper bound of the range wherein the query target must live
    */
    private static void binarySearch(RandomAccessFile rafReader, long queryNumber, int recordSize, int[] maxLengths, long lowerBound, long upperBound) {
        if(upperBound >= lowerBound) {
            //Get the midpoint
            long midpoint = (lowerBound + upperBound) / 2;
            //Get the Data.entry value here
            byte[] dataEntryBuffer = new byte[maxLengths[1]];
            String dataEntryString = null;
            String[] splitResult = new String[2];
            long dataEntryNumber = 0;
            try {
                rafReader.seek((midpoint * recordSize) + 4);
                rafReader.readFully(dataEntryBuffer);
                dataEntryString = (new String(dataEntryBuffer, StandardCharsets.UTF_8)).trim();
                splitResult = dataEntryString.split(" ");
                dataEntryNumber = Long.parseLong(splitResult[1]);
            } catch(IOException e) {
                e.printStackTrace();
            }
            //Do the actual binary search part
            if(dataEntryNumber == queryNumber) {
                printSearchHit(rafReader, midpoint, recordSize, maxLengths);
                return;
            }
            if(dataEntryNumber > queryNumber) {
                //Search lower half
                binarySearch(rafReader, queryNumber, recordSize, maxLengths, lowerBound, midpoint - 1);
                return;
            }
            if(dataEntryNumber < queryNumber) {
                //Search upper half
                binarySearch(rafReader, queryNumber, recordSize, maxLengths, midpoint + 1, upperBound);
                return;
            }
        } else {
            System.out.println("Record not found.");
        }
        return;
    }

    /*
    Method: printSearchHit
    Purpose: this method prints out three fields associated with the search hit from the user's query, namely
    the Country, Cave Site, and the Species Name.
    Precondition: The binary search has found a record in the file that matches the search query.
    Postcondition: none
    Parameters:
        rafReader - a RandomAccessFile object that reads the bin file.
        midpoint - an int representing the midpoint between lowerBound and upperBound
        recordSize - the size, in bytes, of one record from the file
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
    */
    private static void printSearchHit(RandomAccessFile rafReader, long midpoint, int recordSize, int[] maxLengths) {
        //Seek to the midpoint record and read the country cavesite and species name
        byte[] countryBuffer = new byte[maxLengths[6]];
        byte[] caveSiteBuffer = new byte[maxLengths[7]];
        byte[] speciesNameBuffer = new byte[maxLengths[10]];
        String countryString = null, caveSiteString = null, speciesNameString = null;
        int offset = 0;
        try {
            //Seek to Country field
            offset = 4 + maxLengths[1] + maxLengths[2] + maxLengths[3] + maxLengths[4] + maxLengths[5];
            rafReader.seek((midpoint * recordSize) + offset);
            rafReader.readFully(countryBuffer);
            countryString = (new String(countryBuffer, StandardCharsets.UTF_8)).trim();
            //Seek to caveSite field
            rafReader.readFully(caveSiteBuffer);
            caveSiteString = (new String(caveSiteBuffer, StandardCharsets.UTF_8)).trim();
            offset = offset + maxLengths[6] + maxLengths[7] + 16;
            rafReader.seek((midpoint * recordSize) + offset);
            rafReader.readFully(speciesNameBuffer);
            speciesNameString = (new String(speciesNameBuffer, StandardCharsets.UTF_8)).trim();
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println("[" + countryString + "][" + caveSiteString + "][" + speciesNameString + "]");
        return;
    }

    /*
    Method: printSortedByLat
    Purpose: this method prints out, in descending order, the 10 or more most distant caves from 
    the equator.  If there are any caves that tie with the 10th item, then those are printed as well.
    Preconditions: the input List of records has already been sorted by Lattitude
    Postcondition: none. Just printing out stuff
    Parameter:
        sortedByLattitude: an ArrayList<Record> of records sorted in ascending order by Lattitude
    */
    private static void printSortedByLat(List<Record> sortedByLattitude) {
        int last = sortedByLattitude.size() -1;
        Record current = sortedByLattitude.get(last);
        double lastLat = 0;
        for(int i = 0; i < 10; i++) {
            /*
            It's entirely possible that there's a lattitude value with an absolute value greater than 90 in the 
            file, which corresponds to nothing in reality.  If that's the case then such a record has no place 
            here.  Probably best to skip it and move on to the next iteration
            */
            if(Math.abs(current.getLattitude()) > 90) {
                //Move to the next one by decrementing last
                last--;
                current = sortedByLattitude.get(last);
                i--; //Can't let i increment.
                continue;
            }
            System.out.println("[" + current.getCountry() + "][" + current.getCaveSite() + "][" + current.getLattitude() + "]");
            if(i == 9) lastLat = current.getLattitude();
            last--;
            current = sortedByLattitude.get(last);
        }
        while(Double.compare(Math.abs(current.getLattitude()), lastLat) == 0) {
            System.out.println("[" + current.getCountry() + "][" + current.getCaveSite() + "][" + current.getLattitude() + "]");
            last--;
            current = sortedByLattitude.get(last);
        }
        return;
    }

    /*
    Method: makeRecordList
    Purpose: This reads the entire binary file and stores all of records into a Record list.
    This will make it easier to sort and retrieve data for the first two parts of our output.
    Precondition: There should be a bin file
    Postcondition: We'll have an ArrayList<Record> that contains all of the records from the csv file.
    Parameters:
        rafReader - a RandomAccessFile object that reads the bin file.
        numRecords - a long representing the number of records in the csv file.
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
        recordMap - a HashMap<String, Record> that will make it easier to ensure that no duplicates are 
        printed in the result.
    */
    private static List<Record> makeRecordList(RandomAccessFile rafReader, long numRecords, int[] maxLengths, Map<String, Record> recordMap) {
        List<Record> recordList = new ArrayList<Record>();
        byte[] dataEntryBuffer = new byte[maxLengths[1]];
        byte[] caveDataSeriesBuffer = new byte[maxLengths[2]];
        byte[] biogRealmBuffer = new byte[maxLengths[3]];
        byte[] continentBuffer = new byte[maxLengths[4]];
        byte[] biomeClassBuffer = new byte[maxLengths[5]];
        byte[] countryBuffer = new byte[maxLengths[6]];
        byte[] caveSiteBuffer = new byte[maxLengths[7]];
        byte[] speciesNameBuffer = new byte[maxLengths[10]];
        seekWrapper(rafReader, 0);
        for(long i = 0; i < numRecords; i++) {
            Record newRecord = new Record();
            //Oh, just read in all the fields
            try {
                newRecord.setDatasetSeqID(rafReader.readInt());
                rafReader.readFully(dataEntryBuffer);
                rafReader.readFully(caveDataSeriesBuffer);
                rafReader.readFully(biogRealmBuffer);
                rafReader.readFully(continentBuffer);
                rafReader.readFully(biomeClassBuffer);
                rafReader.readFully(countryBuffer);
                rafReader.readFully(caveSiteBuffer);
                newRecord.setLattitude(rafReader.readDouble());
                newRecord.setLongitude(rafReader.readDouble());
                rafReader.readFully(speciesNameBuffer);
                newRecord.setDataEntry((new String(dataEntryBuffer, StandardCharsets.UTF_8).trim()));
                newRecord.setCaveDataSeries((new String(caveDataSeriesBuffer, StandardCharsets.UTF_8)).trim());
                newRecord.setBiogRealm((new String(biogRealmBuffer, StandardCharsets.UTF_8)).trim());
                newRecord.setContinent((new String(continentBuffer, StandardCharsets.UTF_8)).trim());
                newRecord.setBiomeClass((new String(biomeClassBuffer, StandardCharsets.UTF_8)).trim());
                newRecord.setCountry((new String(countryBuffer, StandardCharsets.UTF_8)).trim());
                newRecord.setCaveSite((new String(caveSiteBuffer, StandardCharsets.UTF_8)).trim());
                newRecord.setSpeciesName((new String(speciesNameBuffer, StandardCharsets.UTF_8)).trim());
                recordList.add(newRecord);
                recordMap.putIfAbsent(newRecord.getCaveSite(), newRecord);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return recordList;
    }

    /*
    Method: makeFile
    Purpose: this method makes the File object that the RandomAccessFile will travers.  It's mostly 
    to clean up the code in main(). 
    Preconditions: Prog1a has produced a file by fileName in the same directory as this java file.
    Postconditions: we have a file that we can traverse.
    Parameter:
        fileName: a String representing the name of the file creaed by Prog1a 
    Return:
        binFile: a File object that we can traverse
    */
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

    /*
    Method: makeRafReader
    Purpose: This method makes the RandomAccessFile object that actually traverses the 
    binFile object.  This is another method that was mostly made just to clean up the main() 
    method.
    Preconditon: a File object to traverse has already been made.
    Postcondition: a RandomAccessFile object is made so we can traverse the binFile
    Parameter:
        binFile: the File object that we're traversing.
    Return:
        rafReader: the RandomAccessFile object that traverses the binFile
    */
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
    Method: getFileSize
    Purpose: this method calls the length() method on the rafReader to get the file size
    in bytes.
    Precondition: a RandomAccessFile reader has been made
    Postconditon: none
    Parameter:
        rafReader: the RandomAccessFile object that traverses the binFile
    Return:
        fileSize: a long that represents the size of the binary file. 
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

    /*
    Method: readMaxLengths
    Purpose: this method navigates to the last part of the bin file that contains metadata about the 
    max length of each string column from the csv file. It then stores all of that data in an array.
    This data is needed every time we read in a string field from the bin file.
    Precondition: A bin file exists with the metadata as described above at the end of it: after the records.
    Postcondition: we have an array with a size equal to the number of columns in the csv file as 
    described above.
    Parameters:
        rafReader - a RandomAccessFile object that reads the bin file.
        startOfMaxLengths - a long representing the location in the file where the metadata begins
    Return:
        maxLengths: an int[] that represents the longest field entries for each columns in the csv file
    */
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
    Method: calculateRecordSize
    Purpose: this method calculate the size of an individual record based on the max lengths 
    of the string fields and the size of the numeric fields
    Precondition: we've already determined the size of all fields
    Postcondition: obvious
    Parameter:
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
    Return:
        recordSize - an int that represents the size of an individual record
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

    /*
    Method: validateRecordSize
    Purpose: don't asserts require extra command line args when compiling or running the program? Well, 
    if so, I'm not sure if the graders of this program will be using those args while grading, so this 
    method checks that the record size calculated is actually a factor of the total size of all records.
    Precondition: the recordSize and the size of all records, in bytes, have been calculated.
    Postcondition: this program either exits or continues
    Parameter:
        recordSize - an int that represents the size of an individual record
        allRecordSize - a long representing the size of all of the records from the csv file
    */
    private static void validateRecordSize(int recordSize, long allRecordsSize) {
        long remainder = allRecordsSize % recordSize;
        if(remainder != 0) {
            System.out.println("Something went wrong with the record sizes.");
            System.exit(1);
        }
        return;
    }
    
    /*
    Method: printStartMiddleEnd
    Purpose: This function drives the printing of the first batch of data - the first four, middle 4/5, and last
    four records of the binary file.
    Preconditon: the bin file exists and we have a means of traversing it.
    Postcondition: none
    Parameters:
        rafReader - a RandomAccessFile object that reads the bin file.
        numRecords - a long that represents the number of records in the file
        recordSize - the size, in bytes, of one record from the file
        maxLengths: an int[] that represents the longest field entries for each columns in the csv file
    */
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

    /*
    Method seekWrapper
    Purpose: this cleans up the code of having to write an unsightly try/catch 
    every time I want to actually use the rafReader.
    Precondition: obvious
    Postconditions: also obvious
    Paramters: 
         rafReader - a RandomAccessFile object that reads the bin file.
         destination: a long representing how many bytes into the file we want to seek to.
    */
    private static void seekWrapper(RandomAccessFile rafReader, long destination) {
        try {
            rafReader.seek(destination);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return;
    }

    

    /*
    Method: printSMEHelper
    Purpose: This is a helper method that offloads some of the code from the printStartMiddleEnd method.
    This is where the actual printing is done.
    Precondition: the rafReader pointer must be at the start of a record such that one should be able 
    to immediately call readInt() to get the record's DataSeqID.
    Postcondition: none
    Parameters: 
        rafReader - a RandomAccessFile object that reads the bin file.
        maxLengths: an int[] that represents the longest field entries for each columns in the csv file
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
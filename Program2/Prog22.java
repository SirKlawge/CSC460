/*
Author: Ventura Abram
CSC460
Program2
Instructor: Professor McCann
TAs: Jianwei Shen, Muhammad Bilal
Due: 02/12/2026

This program allows a user to provide Data.entry values at the command line, and, 
using lhl.idx produced by Prog21.java, it will search for a record with that value in 
the database.
    On a search hit, it'll print the country record, cave site, and species name
    associated with the Data.entry value

    On a search miss, it'll just report that it couldnt find such a record.
*/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public class Prog22 {
    private static int STRING_FIELD_LENGTH;
    private static int BUCKET_SIZE;
    private static int H;
    private static int RECORD_SIZE;

    /*
    Method: main
    This opens the file then calls the function to handle user queries.
    Preconditon: a completed index file, lhl.idx and database .bin file 
    have already been created.
    Param:
        args - a String[] where args[0] is the index file and args[1] is the bin file
    */
    public static void main(String[] args) {
        //arg[0] is index file, arg[1] is bin file
        String indexFileName = args[0], binFileName = args[1];
        File indexFile = openFile(indexFileName), binFile = openFile(binFileName);
        //Make rafReaders
        RandomAccessFile indexReader = makeRAFReader(indexFile), binReader = makeRAFReader(binFile);
        handleUserQueries(indexReader, binReader);
        //close files
        try {
            indexReader.close();
            binReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return;
    }

    /*
    Method: makeRAFReader
    Purpose: this method is called to make RandomAccessFile objects for the index file and binary file.
    Precondition: the associated binary files exist
    Param:
        file - a File object that the RandomAccessFile object will traverse
    Return:
        rafReader - a RandomAccessFile object that traverses the binary file.
    */
    private static RandomAccessFile makeRAFReader(File file) {
        RandomAccessFile rafReader = null;
        try {
            rafReader = new RandomAccessFile(file, "rw");
        } catch(IOException e) {
            System.out.println("Error in making a rafReader");
            e.printStackTrace();
        }
        return rafReader;
    }

    /*
    Method: handleUserQueries
    Purpose: this method initiates a loop that will prompt the user for Data.entry 
    search queries.  It works as detailed at the start of this java file.
    Precondition: We have two RandomAccessFile objects: one to traverse the index file 
    and another to traverse the database bin file.
    Param:
        indexReader - a RandomAcessFile object that traverses the index file
        binReader - a RandomAccessFile object that traverses the database file
    */
    private static void handleUserQueries(RandomAccessFile indexReader, RandomAccessFile binReader) {
        Scanner scanner = null;
        String query = "";
        long hashValue = 0;
        int[] maxLengths = getMaxLengths(binReader);
        RECORD_SIZE = calculateRecordSize(maxLengths);
        while(true) {
            //Prompt user for input
            System.out.println("Enter a Data.entry value or -1000 to quit:");
            scanner = new Scanner(System.in);
            query = scanner.nextLine();
            if(query.equals("-1000")) break;
            //Get the metadata from the index file
            getIndexMetadata(indexReader);
            if(query.length() < STRING_FIELD_LENGTH) query += " ";
            //Calculate the hashValue
            hashValue = Math.abs(query.hashCode()) % (long) Math.pow(2, H+1);
            //Read the bucket at the given hashValue
            Directory directory = new Directory(indexReader, BUCKET_SIZE, STRING_FIELD_LENGTH);
            Directory.Bucket bucket = directory.readBucket(hashValue);
            long addressFound = searchBucket(bucket, query);
            //Seek to the record at address in the bin file and print out the data.
            if(addressFound == -1) {
                System.out.println("The target value " + query + " was not found.");
                continue;
            } else {
                printRecord(binReader, addressFound, maxLengths);
            }
        }
        return;
    }

    /*
    Method: calculateRecordSize
    Purpose: This method calculates the size per record in the .bin file.
    Precondition: we've calculated the size of each string field in the database
    Param:
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
    Return: 
        recordSize - the size, in bytes, of one record from the file 
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
    Method: getMaxLengths
    Purpose: The reads in the max lengths of each of the string columns from the csv file.
    The info is stored as the last line in the bin file.
    Precondition: Prog1a.java stored the relevant metadata as the last bytes of data in the 
    .bin file.
    Param:
        binReader - a RandomAccessFile object that traverses the database file
    Return:
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
    */
    private static int[] getMaxLengths(RandomAccessFile binReader) {
        int[] maxLengths = new int[11];
        try {
            binReader.seek(binReader.length() - 44);
            for(int i = 0; i < 11; i++) {
                maxLengths[i] = binReader.readInt();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return maxLengths;
    }

    /*
    Method: printRecord
    Purpose: When there's a search hit on the user's query, this method prints out the 
    country record, cave site, and species name associated with the provided Data.entry value.
    Preconditon: the user has provided a search query that was found in the index file.
    Param:
        binReader - a RandomAccessFile object that traverses the database file
        addressFound - a long that represents the address of the found record in the bin file
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
    */
    private static void printRecord(RandomAccessFile binReader, long addressFound, int[] maxLengths) {
        //Calculate offsets for country record, cave site and species name
        int countryRecordOffset = 4 + maxLengths[1] + maxLengths[2] + maxLengths[3] + maxLengths[4] + maxLengths[5];
        int speciesNameOffset = countryRecordOffset + maxLengths[6] + maxLengths[7] + 16;
        byte[] countryRecordBuffer = new byte[maxLengths[6]];
        String countryRecordString = "";
        byte[] caveSiteBuffer = new byte[maxLengths[7]];
        String caveSiteString = "";
        byte[] speciesNameBuffer = new byte[maxLengths[10]];
        String speciesNameString = "";
        try {
            //Seek and start reading these fields
            binReader.seek((addressFound * RECORD_SIZE) + countryRecordOffset);
            binReader.readFully(countryRecordBuffer);
            countryRecordString = (new String(countryRecordBuffer, StandardCharsets.UTF_8)).trim();
            binReader.readFully(caveSiteBuffer);
            caveSiteString = (new String(caveSiteBuffer, StandardCharsets.UTF_8)).trim();
            binReader.seek((addressFound * RECORD_SIZE) + speciesNameOffset);
            binReader.readFully(speciesNameBuffer);
            speciesNameString = (new String(speciesNameBuffer, StandardCharsets.UTF_8)).trim();
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println("[" + countryRecordString + "][" + caveSiteString + "][" + speciesNameString + "]");
        return;
    }

    /*
    Method: searchBucket
    Purpose: this method does a linear search through the slots of the provided bucket to 
    find an entry that matches the provided query.
    Precondition: we've found a hash value for the query which matched us to a bucket in the 
    index file.
    Param:
        bucket - a Bucket object wherein our query should have a matching entry.
        query - a String representing the user's input at the command line
    Return:
        The address associated with the index record that matches the query
        or -1 if no such query is found in the bucket.
    */
    private static long searchBucket(Directory.Bucket bucket, String query) {
        Directory.BucketSlot[] bucketSlots = bucket.getBucketSlots();
        for(int i = 0; i < Directory.BLOCKING_FACTOR; i++) {
            if(bucketSlots[i].getFieldString().equals(query)) {
                return bucketSlots[i].getAddress();
            }
        }
        return -1;
    }

    /*
    Method: getIndexMetadata
    Purpose: this method retrieves the metadata appended to the index file.  The metadata are useful 
    for reading in the right bucket.
    Precondition: the metadata are at the end of the index file
    Param:
        indexReader - a RandomAcessFile object that traverses the index file
    */
    private static void getIndexMetadata(RandomAccessFile indexReader) {
        try {
            indexReader.seek(indexReader.length() - 12); //-12 b/c we're reading 3 ints
            STRING_FIELD_LENGTH = indexReader.readInt();
            BUCKET_SIZE = indexReader.readInt();
            H = indexReader.readInt();
        } catch(IOException e) {
            System.out.println("Error reading the index metadata");
            e.printStackTrace();
        }
        return;
    }

    /*
    Method: openFile
    Purpose: This method creates the File objects that the RandomAccessFile objects will 
    later traverse.
    Precondition: a file by the provided file name exists
    Param:
        fileName - a String representing the name of the file
    Return:
        file - a File object that the RandomAccessFile object will traverse
    */
    private static File openFile(String fileName) {
        File file = null;
        try {
            file = new File(fileName);
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
        return file;
    }
}
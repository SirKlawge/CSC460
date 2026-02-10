/*
Author: Ventura Abram

I wrote two ints at the end of the index file
first: the length of the 
*/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Scanner;

public class Prog22 {
    private static int STRING_FIELD_LENGTH;
    private static int BUCKET_SIZE;
    private static int H;
    private static int RECORD_SIZE;


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

    //Would be nice if we had the final H value and the size per index record
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
                System.out.println("Record not found");
                continue;
            } else {
                printRecord(binReader, addressFound, maxLengths);
            }
        }
        return;
    }

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
    Print country record 6, cave site 7, and Species name (10)
    */
    private static void printRecord(RandomAccessFile binReader, long addressFound, int[] maxLengths) {
        //Calculate offsets for country record, cave site and species name
        int countryRecordOffset = 4 + maxLengths[1] + maxLengths[2] + maxLengths[3] + maxLengths[4] + maxLengths[5];
        //TODO: keep getting 
        try {
            //
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
    If we find it, return the bucket slot
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
/*
We're making an index called lhl.idx from the bin file.

We're making the index on the Data.entry field.

The process for inserting items into the Directory is that we
    Scan the line
    use hash value on Data.entry string to get the bucket it belongs to
    Get the offset of that bucket in the .idx file
    make changes to that bucket
    Make new bucket and redistribute entries into either this bucket or overflow bucket
    store bucket/s

*/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class Prog21 {

    private static final byte NUM_COLUMNS = 11; //the csv file has 11 fields
    private static final String OUTPUT_FILE_NAME = "lhl.idx";

    public static void main(String[] args) {
        //Open the bin file
        String fileName = args[0];
        File binFile = makeFile(fileName);
        //make our RandomAccessFile to traverse the binFile
        RandomAccessFile rafReader = makeRafReader(binFile);
        //Need to get the metadata. Need fileSize, numCols, startOfMaxLengths to make maxLengths
        long fileSize = getFileSize(rafReader);
        long startOfMaxLengths = fileSize - (4 * NUM_COLUMNS);
        int maxLengths[] = readMaxLengths(rafReader, startOfMaxLengths);
        //Calculate recordSize based on maxLengths
        int recordSize = calculateRecordSize(maxLengths);
        long numRecords = startOfMaxLengths / recordSize;
        //Build the index
        File indexFile = buildIndex(numRecords, rafReader, maxLengths, recordSize);
        try {
            rafReader.close();
        } catch(IOException e) {
            System.out.println("error closing the file");
            e.printStackTrace();
        }
    }

    private static File buildIndex(long numRecords, RandomAccessFile rafReader, int[] maxLengths, int recordSize) {
        File indexFile = makeFile(OUTPUT_FILE_NAME);
        try {
            if(indexFile.exists()) indexFile.delete();
        } catch(Exception e) {
            e.printStackTrace();
        }
        Directory directory = new Directory(indexFile, maxLengths[1]);
        for(int i = 0; i < numRecords; i++) {
            //Get the Data.entry string
            String dataEntryString = getDataEntryString(rafReader, i, maxLengths, recordSize);
            directory.insert(dataEntryString, i);
        }
        System.out.println(directory.getSize());
        return indexFile;
    }

    /*
    Method: getDataEntryString
    Purpose: This method retieves the Data.entry field value from the provided record in the bin file
    Precondition: Must have a RandomAccessFile reader with something to read. Must have calculated recordSize.
    Postcondition: none
    Parameters: 
        rafReader - a RandomAccessFile object that reads the bin file.
        recordNum - a long representing which record we want from the bin file
        maxLengths - an int[] that contains the length of the longest field entry for each column from the csv file
        recordSize - the size, in bytes, of one record from the file
    Return:
        dataEntryString - a String representing the Data.entry field value for the specified record
    */
    private static String getDataEntryString(RandomAccessFile rafReader, long recordNum, int[] maxLengths, int recordSize) {
        byte[] dataEntryBuffer = new byte[maxLengths[1]];
        String dataEntryString = null;
        try {
            rafReader.seek((recordNum * recordSize) + 4);
            rafReader.readFully(dataEntryBuffer);
            dataEntryString = new String(dataEntryBuffer, StandardCharsets.UTF_8); //Don't trim for now
        } catch(IOException e) {
            System.out.println("Error reading Data.entry string");
            e.printStackTrace();
        }
        return dataEntryString;
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
        } catch(IOException e) {
            System.out.println("Error making the RandomAccessFile object");
            e.printStackTrace();
        }
        return rafReader;
    }

    /*
    Method: makeFIle
    Purpose: this attempts to open the bin file and make a File object from it
    Precondition: a .bin file exists in this directory created by Prog1a.java
    Postcondition: the bin file was found and a File object was made.
    Parameter:
        fileName: a String representing the name of the binary file
    Return:
        binFile: a File object that we can later traverse
    */
    private static File makeFile(String fileName) {
        File file = null;
        try {
            file  = new File(fileName);
        } catch(Exception e) {
            System.out.println("Problem opening bin file");
            e.printStackTrace();
        }
        return file;
    }
}
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

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
       int numRecords = startOfMaxLengths / recordSize;
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
    0 - an int: 4 bytes
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
        System.out.println(recordSize);
        return recordSize;
    }

    private static void validateRecordSize(int recordSize, int allRecordsSize) {
        int remainder = allRecordsSize % recordSize;
        if(remainder != 0) {
            System.out.println("Something went wrong with the record sizes.");
            System.exit(1);
        }
        return;
    }

}
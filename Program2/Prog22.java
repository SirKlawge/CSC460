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
    private static int INDEX_RECORD_SIZE;
    private static int H;

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
            System.out.println("hashVale: " + hashValue);
            //System.out.println("hashValue: " + hashValue);
        }
        return;
    }

    private static void getIndexMetadata(RandomAccessFile indexReader) {
        try {
            indexReader.seek(indexReader.length() - 12); //-12 b/c we're reading 3 ints
            STRING_FIELD_LENGTH = indexReader.readInt();
            INDEX_RECORD_SIZE = indexReader.readInt();
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
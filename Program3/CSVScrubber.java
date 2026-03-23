/*
Author: Ventura Abram
Description:  This file should read in a csv file, scrub the data, and 
wrtie it back out to another csv file.

It would be nice if each tuple was a String[] so that I can review data by 
attribute.

My data structure should be a String[][] to store all of the tuples.
An ArrayList of String might have more inherent features that I may need.
First read in the first line to discover how many attributes there are.

Discoveries:
    -The incident numbers are not unique, though it seems like they should be
    -Some of the values for Grade Crossing ID are blank
    -All of the tuples are of proper size: meaning they either have a value in each field or we see ,,
    -We dont have a single null incident num value

We can use a composite key: incident num + date

TODO: for whatever reason, I'm not actually writing NULLs into blank fields.
Need to fix that
*/

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

public class CSVScrubber {
    private static BufferedReader br;
    private static Map<String, Integer> attributeMap;
    private static List<List<String>> tuples;
    
    public static void main(String[] args) {
        //Open the file
        br = openFile(args[0]);
        //Read in the first line: the attribute line. Make the map
        attributeMap = makeAttributeMap();
        //Put all of the tuples in a List<List<String>>
        tuples = makeTupleList();
        //Do testing here
        

        //Ok now some some cleaning
        cleanTuples();
        for(List<String> tuple: tuples) {
            for(String value: tuple) {
                System.out.print(value + ",");
            }
            System.out.println();
        }
        //Close the file
        try {br.close();} catch(IOException e) {e.printStackTrace();}
    }
    
    //Open the file and return the BufferedReader
    private static BufferedReader openFile(String filename) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return br;
    }

    //Just making a small map.  Might be useful later. might not. Maybe reverse k,v
    private static Map<String, Integer> makeAttributeMap() {
        Map<String, Integer> attributeMap = new HashMap<String, Integer>();
        try {
            String[] attributes = br.readLine().split(",");
            for(int i = 0; i < attributes.length; i++) {
                attributeMap.put(attributes[i], i);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return attributeMap;
    }

    private static List<List<String>> makeTupleList() {
        List<List<String>> tuples = new ArrayList<>();
        try {
            String tupleString = null;
            String[] tupleArray;
            while((tupleString = br.readLine()) != null) {
                //Split on ,
                tupleArray = tupleString.split(",");
                List<String> tupleList = new ArrayList<String>();
                for(int i = 0; i < tupleArray.length; i++) {
                    tupleList.add(tupleArray[i]);
                }
                tuples.add(tupleList);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return tuples;
    }

    private static void cleanTuples() {
        for(int i = 0; i < tuples.size(); i++) {
            List<String> tuple = tuples.get(i);
            for(int j = 0; j < tuple.size(); j++) {
                String value = tuple.get(j);
                if(value.equals("")) tuple.set(j, "NULL");
                tuple.set(j, value.trim());
            }
        }
        return;
    }
}
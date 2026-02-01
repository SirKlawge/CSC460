/*
Author: Ventura Abram

We want the directory to open the index file, modify the bucket, then store 
that change to the index file.  This class will need that index file.
*/

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class Directory {

    private File indexFile;
    private List<Bucket> bucketList;
    private int H;
    
    public Directory(File indexFile) {
        this.indexFile = indexFile;
        this.bucketList = new ArrayList<Bucket>();
        this.H = 0;
    }

    public void insert(String fieldString) {
        //Calculate a hash value for the fieldString
        int hashValue = Math.abs(fieldString.hashCode()) % (int) Math.pow(2, H+1);
        //Read this bucket from the indexFile.  We have bucket num, but we also need size per bucket
        return;
    }
}
/*
Author: Ventura Abram

We want the directory to open the index file, modify the bucket, then store 
that change to the index file.  This class will need that index file.
*/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class Directory {

    public static int slotStringLength;

    private File indexFile;
    private int H;
    private RandomAccessFile bucketFetcher;
    private long numBuckets;
    private long size;
    
    public Directory(File indexFile, int maxLength) {
        this.slotStringLength = maxLength;
        this.indexFile = indexFile;
        this.H = 0;
        this.numBuckets = 2;
        this.size = 0;
        BucketSlot.setSizePerSlot(maxLength);
        Bucket.setSizePerBucket();
        try {
            this.bucketFetcher = new RandomAccessFile(indexFile, "rw");
        } catch(IOException e) {
            System.out.println("Error making the rafReader for the idx file");
            e.printStackTrace();
        }
        writeBucket(new Bucket(), 0);
        writeBucket(new Bucket(), 1);
    }

    /*
    */
    public void insert(String fieldString, long address) {
        //Calculate a hash value for the fieldString
        int hashValue = Math.abs(fieldString.hashCode()) % (int) Math.pow(2, H+1);
        //Read this bucket from the indexFile.  We have bucket num, but we also need size per bucket
        long bucketOffset = Bucket.getSizePerBucket() * hashValue;
        Bucket hashedBucket = readBucket(bucketOffset);
        Bucket overflowBucket = null, destinationBucket = hashedBucket;
        if(hashedBucket.isFull()) {
            //Expand the directory
            overflowBucket = expandDirectory(hashedBucket, hashValue);
            int newHashValue = Math.abs(fieldString.hashCode()) % (int) Math.pow(2, H+1);
            if(newHashValue != hashValue) destinationBucket = overflowBucket;
        }
        //Now actually insert the bucketSlot
        BucketSlot newBucketSlot = new BucketSlot(); 
        newBucketSlot.setDataEntryString(fieldString);
        newBucketSlot.setAddress(address);
        destinationBucket.insert(newBucketSlot);
        this.size++;
        //TODO: write the bucket back to the index file
        return;
    }

    private Bucket expandDirectory(Bucket hashedBucket, int hashValue) {
        //Double the amount of current buckets and update numBuckets
        for(long i = 0; i < this.numBuckets; i++) {
            writeBucket(new Bucket(), i + this.numBuckets);
        }
        this.numBuckets *= 2;
        //Now rehash all of the hashedBucket's entries
        this.H++;
        int bf = Bucket.getBlockingFactor();
        int newHashValue = 0;
        BucketSlot[] bucketSlots = hashedBucket.getBucketSlots();
        Bucket overflowBucket = new Bucket();
        BucketSlot removed = null;
        for(int i = 0; i < bf; i++) {
            newHashValue = Math.abs(bucketSlots[i].getDataEntryString().hashCode()) % (int)Math.pow(2, H+1);
            if(newHashValue != hashValue) {
                removed = hashedBucket.remove(i);
                //Insert the slot into the overflow bucket
                overflowBucket.insert(removed);     
            }
        }
        return overflowBucket;
    }

    /*
    I want this to be able to write a bucket where I tell it to.
    It'll take a bucketOffset, then I know where to write.
    It should also write the passed in bucket to the provided offset.
    This means that I can do writeBucket(new Bucket(), offset) to write a new, empty bucket
    or, after I've added to a bucket, I just do writeBucket(bucketReference, offset)
    
    */
    private void writeBucket(Bucket bucket, long bucketOffset) {
        try {
            long bucketSize = Bucket.getSizePerBucket();
            int bf = Bucket.getBlockingFactor();
            BucketSlot currentSlot;
            for(int i = 0; i < bf; i++) {
                //Seek to the start of the BucketSlot
                this.bucketFetcher.seek((i + bucketOffset) * bucketSize);
                currentSlot = bucket.getBucketSlots()[i];
                this.bucketFetcher.writeBytes(currentSlot.getDataEntryString());
                this.bucketFetcher.writeLong(currentSlot.getAddress());
            }
        } catch(IOException e) {
            System.out.println("Error writing a bucket");
            e.printStackTrace();
        }
        return;
    }

    /*
    Problem: in order for this method to avoid reading nothing from the idx file, the idx file 
    needs at least 1
    */
    private Bucket readBucket(long bucketOffset) {
        Bucket newBucket = new Bucket();
        try {
            //Index file could be empty, in which case there's nothing to read
            if(this.bucketFetcher.length() == 0) return newBucket;
            this.bucketFetcher.seek(bucketOffset);
            long bf = Bucket.getBlockingFactor();
            BucketSlot currentSlot = null;
            byte[] stringFieldBuffer = new byte[slotStringLength];
            for(int i = 0; i < bf; i++) {
                currentSlot = newBucket.getBucketSlots()[i];
                //Scan the string field from the index file
                bucketFetcher.readFully(stringFieldBuffer);
                currentSlot.setDataEntryString(new String(stringFieldBuffer, StandardCharsets.UTF_8));
                //Now read the long
                currentSlot.setAddress(bucketFetcher.readLong());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return newBucket;
    }

    public long getSize() {
        return this.size;
    }
}
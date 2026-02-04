/*
Author: Ventura Abram

TODO: need to get data about the directory.
We need total number of buckets. Done!
We need the number of records in the highest occupancy bucket and the lowest bucket.
Then we need the mean and median of all buckets to two decimal places

Solution: maintain a HashMap<Integer, Integer> 
    keys: bucket number
    values: occupancy of that bucket

At the end of the insert() method, you'll have at least the hashedBucket and 
maybe and overflowBucket.  Simply add the <hashValue, occupancy> pair to the HashMap
You've already calculated the hashValue and the overflowHashValue
The occupancy is the BLOCKING_FACTOR - bucket.getOpenSlots()
You'll have to check to see if the key exists in the hashmap

Once you've built up the hashmap, then you can sort the values and find their mean and median
*/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Directory {

    public static int slotStringLength;

    private File indexFile;
    private int H;
    private RandomAccessFile bucketFetcher;
    private long numBuckets;
    private long size;
    private Map<Long, Integer> bucketMap; //Should eliminate any redundancies
    
    public Directory(File indexFile, int maxLength) {
        this.slotStringLength = maxLength;
        this.indexFile = indexFile;
        this.H = 0;
        this.numBuckets = 2;
        this.size = 0;
        this.bucketMap = new HashMap<Long, Integer>();
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
        long hashValue = Math.abs(fieldString.hashCode()) % (long) Math.pow(2, H+1);
        long overFlowHashValue = hashValue + (long)Math.pow(2, H+1);
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
        this.bucketMap.put(hashValue, Bucket.getBlockingFactor() - hashedBucket.getOpenSlots());
        writeBucket(hashedBucket, hashValue);
        if(overflowBucket != null) {
            writeBucket(overflowBucket, overFlowHashValue);
            this.bucketMap.put(overFlowHashValue, Bucket.getBlockingFactor() - overflowBucket.getOpenSlots());
        }
        return;
    }

    private Bucket expandDirectory(Bucket hashedBucket, long hashValue) {
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
    Problem: When I read in a bucket, I'm making a new bucket object each time. But there's nothing
    calling insert(), and therefore there's no updating the number of occupied slots.
    That's because we're just directly setting BucketSlot values for each slot with setters.
    Instead: make a BucketSlot object and call insert() on it if the address is not -1.

    Another problem: when you build the bucket from the file, you cant just call insert() in the 
    BucketSlot/bucket because that uses a freeslotqueue.   You have to do a linear search of the
    bucket slots to find an open spot.  Else what will happen is that you'll try to insert the 
    new record in this class's insert() method, give it a number from a queue that thinks all 
    slots are free.

    Solution: make a BucketSlot object, furnish its fields, and insert() into the bucket.
    In the Bucket class's insert() method, check to see if address is -1, if it is then skip.
    Get rid of the free slot queue and insert by lineraly searching for an open slot.
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
            String currentSlotString = null;
            long currentSlotAddress = -1;
            for(int i = 0; i < bf; i++) {
                currentSlot = newBucket.getBucketSlots()[i];
                //Scan the string field from the index file
                bucketFetcher.readFully(stringFieldBuffer);
                currentSlotString = new String(stringFieldBuffer, StandardCharsets.UTF_8);
                currentSlotAddress = bucketFetcher.readLong();
                if(currentSlotAddress != -1) {
                    //Here, it's a non-empty slot, call insertToSlot
                    currentSlot.setDataEntryString(currentSlotString);
                    currentSlot.setAddress(currentSlotAddress);
                    newBucket.insertToSlot(currentSlot, i);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return newBucket;
    }

    public long getSize() {
        return this.size;
    }

    public long getNumBuckets() {
        return this.numBuckets;
    }

    public Map<Long, Integer> getBucketMap() {
        return this.bucketMap;
    }
}
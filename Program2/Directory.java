/*
Author: Ventura Abram

To get numBuckets, just print numBuckets
Every time we write a bucket, update that bucket's kv pair in bucketData
Once we're done inserting, Prog21.java can take the bucketData and sort it
to get mean/median
*/

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.LinkedList;


public class Directory {

    public static int STRING_FIELD_LENGTH;
    public static final int BLOCKING_FACTOR = 30;
    public static int INDEX_RECORD_SIZE;
    public static int BUCKET_SIZE;

    private File indexFile;
    private int H;
    private RandomAccessFile rafReader;
    private long size; //Number of index records/occupied bucket slots
    private long numBuckets;
    private Map<Long, Integer> bucketData; //bucketNumber (hashValue), bucketSize
    
    public Directory(File indexFile, int stringFieldLength) {
        this.indexFile = indexFile;
        STRING_FIELD_LENGTH = stringFieldLength;
        INDEX_RECORD_SIZE = STRING_FIELD_LENGTH + 8;
        BUCKET_SIZE = INDEX_RECORD_SIZE * BLOCKING_FACTOR;
        this.H = 0;
        this.numBuckets = 2;
        this.bucketData = new HashMap<Long, Integer>();
        try {
            this.rafReader = new RandomAccessFile(this.indexFile, "rw");
        } catch(IOException e) {
            e.printStackTrace();
        }
        this.size = 0;
        //Prime the index file by writing the first two empty buckets to it
        writeBucket(new Bucket(), 0);
        writeBucket(new Bucket(), 1);
    }

    public long getSize() {return this.size;}
    public long getNumBuckets() {return this.numBuckets;}
    public Map<Long, Integer> getBucketData() {return this.bucketData;}

    public void appendMetadata() {
        try {
            this.rafReader.seek(this.rafReader.length());
            this.rafReader.writeInt(STRING_FIELD_LENGTH);
            this.rafReader.writeInt(INDEX_RECORD_SIZE);
            this.rafReader.writeInt(this.H);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return;
    }

    public void printDirectory() {
        Bucket currentBucket = null;
        for(long bucketNum = 0; bucketNum < this.numBuckets; bucketNum++) {
            currentBucket = readBucket(bucketNum);
            System.out.println("Bucket " + bucketNum + "\n" + currentBucket);
        }
        return;
    }

    /*
    when we grow the directory, we have to rehash every last existing bucket.
    We'll have to reread hashedBucket
    */
    public void insert(String fieldString, long address) {
        BucketSlot newSlot = new BucketSlot(fieldString, address);
        //Find the right bucket to insert it into via the hashing function
        long hashValue = Math.abs(fieldString.hashCode()) % (long) Math.pow(2, H+1);
        //The hash value is also the offset in the index file for the Bucket data.
        Bucket hashedBucket = (this.size == 0)? new Bucket() : readBucket(hashValue);
        Bucket overflowBucket = null;
        long overflowBucketHashValue = hashValue + (long) Math.pow(2, H+1);
        long newHashValue = hashValue;
        if(hashedBucket.isFull()) {
            //Grow the Directory
            growDirectory(address);
            //H value should have increased, so rehash to get the destination bucket
            newHashValue = Math.abs(fieldString.hashCode()) % (long) Math.pow(2, H+1);
            overflowBucket = readBucket(overflowBucketHashValue);
            hashedBucket = readBucket(hashValue);  //Have to re-read
        }
        //Determine the bucket in which to insert
        if(newHashValue == hashValue) {
            //hashValue didnt change
            hashedBucket.insert(newSlot);
        } else {
            overflowBucket.insert(newSlot);
            writeBucket(overflowBucket, overflowBucketHashValue);
        }
        this.size++;
        //Now write the bucket/s back to the index file
        writeBucket(hashedBucket, hashValue);
        return;
    }

    /*
    If all of the slots in one bucket hash to the overflow bucket, then I have a full bucket, and I'll need to 
    grow the directory again.
    i == 3 and address 341 is failure point
    */
    private void growDirectory(long address) {
        Bucket  currentBucket = null;
        boolean haveFullBucket = false;
        this.H++;
        for(long i = 0; i < numBuckets; i++) {
            //Make an overflow bucket
            Bucket overflowBucket = new Bucket();
            //Read in bucket at offset i
            currentBucket = readBucket(i);
            for(int slot = 0; slot < BLOCKING_FACTOR; slot++) {
                if(currentBucket.bucketSlots[slot].address != -1) {
                    //Get hash value based on the record's string
                    long destination = Math.abs(currentBucket.bucketSlots[slot].fieldString.hashCode()) % (long) Math.pow(2, this.H + 1);
                    if(destination != i) {
                        //Here, the record now hashes into the overflow bucket
                        overflowBucket.insert(currentBucket.bucketSlots[slot], slot);
                        currentBucket.removeSlot(slot);
                    }
                }
            }
            //Now write currentBucket back to the file
            writeBucket(currentBucket, i);
            //Append the overflow bucket
            appendBucket(overflowBucket, i);
            if(currentBucket.isFull() || overflowBucket.isFull()) haveFullBucket = true;
        }
        this.numBuckets *= 2;
        if(haveFullBucket) growDirectory(address);
        return;
    }

    private void writeBucket(Bucket bucket, long bucketIdx) {
        try {
            BucketSlot currentSlot = null;
            this.rafReader.seek(bucketIdx * BUCKET_SIZE);
            //traverse the slots of the bucket and write them sequentially
            for(int slot = 0; slot < BLOCKING_FACTOR; slot++) {
                currentSlot = bucket.bucketSlots[slot];
                this.rafReader.writeBytes(currentSlot.fieldString);
                this.rafReader.writeLong(currentSlot.address);
            }
        } catch(IOException e) {
            System.out.println("Error writing bucket to index file");
            e.printStackTrace();
        }
        this.bucketData.put(bucketIdx, bucket.size);
        return;
    }

    private void appendBucket(Bucket bucket, long hashedBucketIndex) {
        try {
            BucketSlot currentSlot = null;
            //Seek to the end of the index file.
            this.rafReader.seek(this.rafReader.length());
            for(int slot = 0; slot < BLOCKING_FACTOR; slot++) {
                currentSlot = bucket.bucketSlots[slot];
                this.rafReader.writeBytes(currentSlot.fieldString);
                this.rafReader.writeLong(currentSlot.address);
            }
        } catch(IOException e) {
            System.out.println("Error appending the bucket to the index file");
            e.printStackTrace();
        }
        long overflowBucketIndex = hashedBucketIndex + (long) Math.pow(2, this.H);
        this.bucketData.put(overflowBucketIndex, bucket.size);
        return;
    }

    private Bucket readBucket(long hashValue) {
        Bucket bucket = new Bucket(); //Start with all initialized BucketSlots
        String stringFieldString = "";
        try {
            //Go to the start of the bucket in the file
            this.rafReader.seek(hashValue * BUCKET_SIZE);
            BucketSlot current = null;
            byte[] stringFieldBuffer = new byte[STRING_FIELD_LENGTH];
            long address = 0;
            for(int i = 0; i < BLOCKING_FACTOR; i++) {
                this.rafReader.readFully(stringFieldBuffer);
                stringFieldString = new String(stringFieldBuffer, StandardCharsets.UTF_8);
                address = this.rafReader.readLong();
                if(address != -1) {
                    //Here, the index record we scanned is an occupied slot
                    current = new BucketSlot(stringFieldString, address);
                    bucket.insert(current, i);
                }
            }
        } catch(IOException e) {
            //System.out.println("last stringFieldString read: " + stringFieldString);
            e.printStackTrace();
        }
        return bucket;
    }


    public class Bucket {
        private BucketSlot[] bucketSlots;
        private int size;
        private Queue<Integer> freeSlotQueue;

        public Bucket() {
            this.bucketSlots = new BucketSlot[BLOCKING_FACTOR];
            this.freeSlotQueue = new LinkedList<Integer>();
            for(int i = 0; i < BLOCKING_FACTOR; i++) {
                this.bucketSlots[i] = new BucketSlot();
                this.freeSlotQueue.add(i);
            }
            this.size = 0;
        }

        public void insert(BucketSlot bucketSlot) {
            //Take a number
            int slot = this.freeSlotQueue.remove();
            this.bucketSlots[slot] = bucketSlot;
            this.size++;
            return;
        }

        public void insert(BucketSlot bucketSlot, int slot) {
            this.bucketSlots[slot] = bucketSlot;
            //Remove the slot number from the freeSlotQueue
            this.freeSlotQueue.remove(slot);
            this.size++;
            return;
        }

        public void removeSlot(int slot) {
            this.freeSlotQueue.add(slot);
            this.bucketSlots[slot] = new BucketSlot();
            this.size--;
            return;
        }

        public boolean isFull() {
            return this.size == BLOCKING_FACTOR;
        }

        public String toString() {
            String bucketString = "";
            for(int i = 0; i < BLOCKING_FACTOR; i++) {
                bucketString += i + ": " + this.bucketSlots[i];
            }
            bucketString += "\n";
            System.out.println(this.freeSlotQueue);
            return bucketString;
        }
    } 

    public class BucketSlot {
        private String fieldString;
        private long address;

        BucketSlot(String fieldString, long address) {
            this.fieldString = fieldString;
            this.address = address;
        }

        BucketSlot() {
            this.fieldString = String.format("%-" + STRING_FIELD_LENGTH + "s", "");
            this.address = -1;
        }

        public String toString() {
            return "[" + this.fieldString + ", " + this.address + "]\n";
        }

    }
}
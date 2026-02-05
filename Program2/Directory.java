/*
Author: Ventura Abram


*/

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class Directory {

    public static int STRING_FIELD_LENGTH;
    public static final int BLOCKING_FACTOR = 30;
    public static int INDEX_RECORD_SIZE;
    public static int BUCKET_SIZE;

    private File indexFile;
    private int H;
    private RandomAccessFile rafReader;
    private long size;
    private long numBuckets;
    
    public Directory(File indexFile, int stringFieldLength) {
        this.indexFile = indexFile;
        STRING_FIELD_LENGTH = stringFieldLength;
        INDEX_RECORD_SIZE = STRING_FIELD_LENGTH + 8;
        BUCKET_SIZE = INDEX_RECORD_SIZE * BLOCKING_FACTOR;
        this.H = 0;
        this.numBuckets = 2;
        try {
            this.rafReader = new RandomAccessFile(this.indexFile, "rw");
        } catch(IOException e) {
            e.printStackTrace();
        }
        this.size = 0;
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
        if(hashedBucket.isFull()) {
            //Grow the Directory
            growDirectory();
            //H value should have increased, so rehash to get the destination bucket
        }

        return;
    }

    private void growDirectory() {
        for(long i = 0; i < numBuckets; i++) {
            //Make an overflow bucket
            Bucket overflowBucket = new Bucket();
            //i is the number of the current existing bucket. i + Math.pow(2, H+1) is overflow bucket
            long overflowHashVal = i + Math.pow(2, this.H + 1); //TODO: continue here
        }
    }

    private Bucket readBucket(long hashValue) {
        Bucket bucket = new Bucket(); //Start with all initialized BucketSlots
        try {
            //Go to the start of the bucket in the file
            this.rafReader.seek(hashValue * BUCKET_SIZE);
            BucketSlot current = null;
            byte[] stringFieldBuffer = new byte[STRING_FIELD_LENGTH];
            String stringFieldString = null;
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
            return;
        }

        public void insert(BucketSlot bucketSlot, int slot) {
            this.bucketSlots[slot] = bucketSlot;
            //Remove the slot number from the freeSlotQueue
            this.freeSlotQueue.remove(slot);
            this.size++;
            return;
        }

        public boolean isFull() {
            return this.size == BLOCKING_FACTOR;
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

    }
}
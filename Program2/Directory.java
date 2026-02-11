/*
Author: Ventura Abram
CSC460
Program2
Instructor: Professor McCann
TAs: Jianwei Shen, Muhammad Bilal
Due: 02/12/2026

This class defines a Directory object which allows us to group the index entries 
into Buckets which each in turn contain a number of BucketSlots.
The BucketSlots hold data about a single index entry.
Since this uses the index file, lhl.idx, as a data structure, it is also 
responsible for creating and updating that file with each insertion into the 
directory.
This also maintains data about the directory which we will later print out.
*/

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.Random;
import java.util.RandomAccess;
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
    
    /*
    This constructor is used by Prog21.java to build the main Directory object 
    and also make the index file.
    Preconditions: the max length of the primary keys has been determined.
    Params: 
        indexFile - a File object that will ultimately be our .idx file
        stringFieldLength - and int representing the length of the primary key that we'll
        be using to build this index.
    */
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

    /*
    This constructor is used by Prog22.java because that file needs the ability to 
    read in buckets, which is a method already defined here.  It won't be used to 
    make a index file, so we don't need to provide it with all the data.
    Precondition: the completed index file has already been made
    Params:
        rafReader - the RandomAccessFile object that read a bin file.
        bucketSize - an int representing the size, in bytes, of one bucket.
        stringFieldLength - and int representing the length of the primary key that we'll
        be using to build this index.
    */
    public Directory(RandomAccessFile rafReader, int bucketSize, int stringFieldLength) {
        this.rafReader = rafReader;
        BUCKET_SIZE = bucketSize;
        STRING_FIELD_LENGTH = stringFieldLength;
    }

    /*
    A fee getters for some of this class's members
    */
    public long getSize() {return this.size;}
    public long getNumBuckets() {return this.numBuckets;}
    public Map<Long, Integer> getBucketData() {return this.bucketData;}

    /*
    Method: appendMetadata
    Purpose: this method write some useful metadata at the end of the lhl.idx file.
    This data will be used by Prog22.java to help it perform its tasks.
    Precondtion: The completed lhl.idx file has been built by this Directory object.
    */
    public void appendMetadata() {
        try {
            this.rafReader.seek(this.rafReader.length());
            this.rafReader.writeInt(STRING_FIELD_LENGTH);
            this.rafReader.writeInt(BUCKET_SIZE);
            this.rafReader.writeInt(this.H);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return;
    }

    /*
    Method: printDirectory
    For testing purposes only.  This prints the directory in a readable format
    */
    public void printDirectory() {
        Bucket currentBucket = null;
        for(long bucketNum = 0; bucketNum < this.numBuckets; bucketNum++) {
            currentBucket = readBucket(bucketNum);
            System.out.println("Bucket " + bucketNum + "\n" + currentBucket);
        }
        return;
    }

    /*
    Method: insert
    Purpose: This method is called by Prog21.java to insert each primary key along with 
    an address that corresponds to the record in the database into this directory.
    Precondition: Prog21.java has read a field string to be inserted from the bin file
    Postcondition: the field string plus an address is inserted into a bucket slot
    Params:
        fieldString - a String representing the Data.entry field from a DB record
        address - a long representing the location in the bin file wherein we can find 
        the record that corresponds with the fieldString
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
    Method: growDirectory
    Purpose: when a Bucket gets full and we want to potentially insert another index record 
    into it, we need to grow the directory and rehash all of the existing entries.
    Precondition: insert() method has read in a bucket that is full.
    Postcondition: The directory has doubled in size.  All existing entries have been 
    redistributed among this increased number of buckets.
    Param:
        address - a long representing the location in the bin file wherein we can find 
        the record that corresponds with the fieldString
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

    /*
    Method: writeBucket
    Purpose: This method writes the provided bucket to the index file at the 
    provided offset
    Precondition: We've read a bucket that we now need to write.
    Postcondition: we've updated the bucket in the index file at the 
    provided offset.
    Params:
        bucket - a Bucket object that contains data about multiple index entries.
        bucketIdx - a long representing the offset at which we need to write the 
        provided bucket.
    */
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

    /*
    Method: appendBucket
    Purpose: This method appends buckets newly created by growDirectory() to the end 
    of the index file.
    Precondition: The bucket we tried to insert() a new entry into was determined to be
    full.  This also means that we've calculated the hash value that was full.
    Postcondition: a new bucket has been appended to the index file.
    Param:
        bucket - a Bucket object that contains data about multiple index entries.
        hashedBucketIndex - a long representing the location of the full bucket.
    */
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

    /*
    Method: readBucket
    Purpose: this method reads in a bucket from the index file.
    Precondition: the index file has at least hashValue + 1 bucket entries
    Postcondition: we've loaded a bucket into memory
    Param:
        hashValue - a long that represents the (hashValue+1)th bucket in the index file.
    Return:
        bucket - a Bucket object that contains data about multiple index entries
    */
    public Bucket readBucket(long hashValue) {
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

    /*
    This class defines the data structure that holds a number of BucketSlot, which 
    in turn, represent entires into the index.
    */
    public class Bucket {
        private BucketSlot[] bucketSlots;
        private int size;
        private Queue<Integer> freeSlotQueue;

        /*
        This constructor defaults all of the BucketSlots in the bucketSlots array 
        to vales that will denote an empty slot.
        */
        public Bucket() {
            this.bucketSlots = new BucketSlot[BLOCKING_FACTOR];
            this.freeSlotQueue = new LinkedList<Integer>();
            for(int i = 0; i < BLOCKING_FACTOR; i++) {
                this.bucketSlots[i] = new BucketSlot();
                this.freeSlotQueue.add(i);
            }
            this.size = 0;
        }

        //Getter for bucketSlots array.
        public BucketSlot[] getBucketSlots() {return this.bucketSlots;}

        /*
        Method: insert
        Purpose: this method inserts an index record entry, in the form of a BucketSlot 
        object, into the bucketSlot array. It also updates info about this bucket.
        Precondition: None
        Postcondition: there's a new entry in the bucket.
        Param: 
            bucketSlot - a BucketSlot object that represents an entry into the index file
        */
        public void insert(BucketSlot bucketSlot) {
            //Take a number
            int slot = this.freeSlotQueue.remove();
            this.bucketSlots[slot] = bucketSlot;
            this.size++;
            return;
        }

        /*
        Method: insert
        Purpose: this does the same as the insert method above, but this allows us 
        to insert at a particular slot rather than taking being assigned a number from the 
        freeSlotQueue.
        Postcondition: there's a new entry in the bucket.
        Param: 
            bucketSlot - a BucketSlot object that represents an entry into the index file
            slot - an int representing the target slot for the BucketSlot
        */
        public void insert(BucketSlot bucketSlot, int slot) {
            this.bucketSlots[slot] = bucketSlot;
            //Remove the slot number from the freeSlotQueue
            this.freeSlotQueue.remove(slot);
            this.size++;
            return;
        }

        /*
        Method: removeSlot
        Purpose: This method removes an index entry from this bucket.  It's called when we 
        have to grow the Directory.  Importantly, it adds the provided slot index back into 
        the freeSlotQueue.
        Precondition: the freeSlotQueue lacks the provided slot number.
        Postcondition: the freeSlotQueue contains the provided slot number.
        Param:
            slot - an int representing the target slot for the BucketSlot
        */
        public void removeSlot(int slot) {
            this.freeSlotQueue.add(slot);
            this.bucketSlots[slot] = new BucketSlot();
            this.size--;
            return;
        }

        /*
        Method: isFull
        Purpose: determines if the bucket is full
        Return:
            true if the bucket is full. False otherwise.
        */
        public boolean isFull() {
            return this.size == BLOCKING_FACTOR;
        }

        /*
        Method: toString
        For testing purposes only.  Provides a string representation of this bucket.
        */
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

    /*
    This class represents an individual entry into the Directory.
    It holds only the field string and the address associated with the database entry.
    */
    public class BucketSlot {
        private String fieldString;
        private long address;

        /*
        This constructor allows you to provide the contents of the BucketSlot.
        Param:
            fieldString - a String representing the Data.entry field from a DB record
            address - a long representing the location in the bin file wherein we can find
        */
        BucketSlot(String fieldString, long address) {
            this.fieldString = fieldString;
            this.address = address;
        }

        /*
        This constructor is used by a constructor in the Bucket class to initialize all of the 
        bucket slots.  I'm using -1 in the address field to represent an empty slot.
        */
        BucketSlot() {
            this.fieldString = String.format("%-" + STRING_FIELD_LENGTH + "s", "");
            this.address = -1;
        }

        /*
        Method: toString
        For testing purposes only.  It produces a string representation of this bucket slot
        */
        public String toString() {
            return "[" + this.fieldString + ", " + this.address + "]\n";
        }

        //Getters for the fields.
        public String getFieldString() {return this.fieldString;}
        public long getAddress() {return this.address;}

    }
}
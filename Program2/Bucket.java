/*
Author: Ventura Abram

A bucket is a collection of BucketSlots
*/

import java.util.Queue;
import java.util.LinkedList;

public class Bucket {
    private static final int BLOCKING_FACTOR = 30;
    private static long sizePerBucket;

    private BucketSlot[] bucketSlots;
    private int openSlots;
    private Queue<Integer> freeSlotQueue;

    public Bucket() {
        this.bucketSlots = new BucketSlot[BLOCKING_FACTOR];
        this.openSlots = BLOCKING_FACTOR;
        for(int i = 0; i < BLOCKING_FACTOR; i++) {
            this.bucketSlots[i]  = new BucketSlot();
        }
        this.freeSlotQueue = new LinkedList<Integer>();
        initFreeSlotQueue();
    }

    private void initFreeSlotQueue() {
        for(int i = 0; i < BLOCKING_FACTOR; i++) {
            this.freeSlotQueue.add(i);
        }
        return;
    }

    public boolean isFull() {
        return this.openSlots == 0;
    }

    public static void setSizePerBucket() {
        sizePerBucket = BucketSlot.getSizePerSlot() * BLOCKING_FACTOR;
        return;
    }

    public static long getSizePerBucket() {
        return sizePerBucket;
    }

    public static int getBlockingFactor() {
        return BLOCKING_FACTOR;
    }

    public BucketSlot[] getBucketSlots() {
        return this.bucketSlots;
    }

    public void insert(BucketSlot newBucket) {
        //Get a number from the freeSlotQueue
        int slot = this.freeSlotQueue.remove();
        this.bucketSlots[slot] = newBucket;
        this.openSlots--;
        return;
    }

    public BucketSlot remove(int slot) {
        BucketSlot removed = this.bucketSlots[slot];
        this.openSlots++;
        this.freeSlotQueue.add(slot);
        this.bucketSlots[slot] = new BucketSlot();
        return removed;
    }

}
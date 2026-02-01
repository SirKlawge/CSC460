/*
Author: Ventura Abram

A bucket is a collection of BucketSlots
*/

public class Bucket {
    private static final int BLOCKING_FACTOR = 30;

    private BucketSlot[] bucketSlots;

    public Bucket {
        this.bucketSlots = new BucketSlot[BLOCKING_FACTOR];
    }
}
/*
Author: Ventura Abram

Bucket slots need to hold the Data.entry string and an address to that entry in the bin file.
*/

public class BucketSlot {
    private static int sizePerSlot;

    private String dataEntryString;
    private long address;

    public BucketSlot() {
        this.address = -1;
        this.dataEntryString = String.format("%-" + Directory.slotStringLength + "s", "");
    }

    public void setDataEntryString(String dataEntryString) {
        this.dataEntryString = dataEntryString;
        return;
    } 

    public void setAddress(long address) {
        this.address = address;
        return;
    }

    public String getDataEntryString() {
        return this.dataEntryString;
    }

    public long getAddress() {
        return this.address;
    }

    public static void setSizePerSlot(int maxLength) {
        sizePerSlot = maxLength + 8;
        return;
    }

    public static int getSizePerSlot() {
        return sizePerSlot;
    }
}
package main;

public class Parcel {
    public enum Status {
        InQueue,
        Sorted,
        Dispatched,
        Returned
    }

    private String parcelID;
    private String destinationCity;
    private int priority;
    private String size;
    private int arrivalTick;
    private Status status;

    private int dispatchTick = -1;
    private int returnCount = 0;

    public Parcel(String parcelID, String destinationCity, int priority,
            String size, int arrivalTick) {
        this.parcelID = parcelID;
        this.destinationCity = destinationCity;
        this.priority = priority;
        this.size = size;
        this.arrivalTick = arrivalTick;
        this.status = Status.InQueue;
    }

    public String getParcelID() {
        return parcelID;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public int getPriority() {
        return priority;
    }

    public String getSize() {
        return size;
    }

    public int getArrivalTick() {
        return arrivalTick;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getDispatchTick() {
        return dispatchTick;
    }

    public void setDispatchTick(int dispatchTick) {
        this.dispatchTick = dispatchTick;
    }

    public int getReturnCount() {
        return returnCount;
    }

    public void incrementReturnCount() {
        this.returnCount++;
    }

    @Override
    public String toString() {
        return String.format("Parcel[%s → %s, Prio: %d, Size: %s, ArrivalTick: %d, Status: %s]",
                parcelID, destinationCity, priority, size, arrivalTick, status);
    }
}

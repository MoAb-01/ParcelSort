package data_sturcts;
import java.util.logging.*;

public class ParcelTracker {
    private static final Logger logger = Logger.getLogger(ParcelTracker.class.getName());
    private static final int INITIAL_CAPACITY = 30;  // Based on QUEUE_CAPACITY from config.txt
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;  // As specified in requirements

    public enum ParcelStatus {
        IN_QUEUE,
        SORTED,
        DISPATCHED,
        RETURNED
    }  
    // Node
    private class ParcelNode {
        String parcelID;
        ParcelStatus status;
        int arrivalTick;
        int dispatchTick;
        int returnCount;
        String destinationCity;
        int priority;
        String size;
        ParcelNode next;  // For chaining
        
        ParcelNode(String parcelID, ParcelStatus status, int arrivalTick, 
                  String destinationCity, int priority, String size) {
            this.parcelID = parcelID;
            this.status = status;
            this.arrivalTick = arrivalTick;
            this.dispatchTick = -1;  // Not dispatched yet
            this.returnCount = 0;
            this.destinationCity = destinationCity;
            this.priority = priority;
            this.size = size;
            this.next = null;
        }
    }
    
    // Hash table structure
    private ParcelNode[] table;
    private int size;
    private int capacity;
    
    private int currentTick = 0;  // Add current tick tracking

    public ParcelTracker() {
        this.capacity = INITIAL_CAPACITY;
        this.table = new ParcelNode[capacity];
        this.size = 0;
        logger.info(String.format("[Initialize] ParcelTracker created with initial capacity %d (based on QUEUE_CAPACITY)", capacity));
    }

    private int hash(String parcelID) {
        int hash = 0;
        for (char c : parcelID.toCharArray()) {
            hash = (hash * 31 + c) % capacity;
        }
        return Math.abs(hash);
    }
  
    public void insert(String parcelID, ParcelStatus status, int arrivalTick, 
                      String destinationCity, int priority, String size) {
        //edge cases::
        try {
            // Validate input
            if (parcelID == null || parcelID.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid parcel ID");
            }
            if (destinationCity == null || destinationCity.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid destination city");
            }
            if (priority < 1 || priority > 3) {
                throw new IllegalArgumentException("Invalid priority value");
            }
            if (size == null || !size.matches("Small|Medium|Large")) {
                throw new IllegalArgumentException("Invalid size value");
            }
         
            if (exists(parcelID)) {
                throw new IllegalStateException("Parcel already exists: " + parcelID);
            }

            if ((double) this.size / capacity >= LOAD_FACTOR_THRESHOLD) {
                resize();
            }
            
            int index = hash(parcelID);
            ParcelNode newNode = new ParcelNode(parcelID, status, arrivalTick, 
                                               destinationCity, priority, size);
            newNode.next = table[index];
            table[index] = newNode;
            this.size++;
            
            logger.info(String.format("[Insert] Parcel %s tracked with status %s", 
                parcelID, status));
        } catch (Exception e) {
            logger.severe(String.format("[Error] Failed to insert parcel %s: %s", 
                parcelID, e.getMessage()));
            throw e;
        }
    }

    public void updateStatus(String parcelID, ParcelStatus newStatus) {
        try {
            ParcelNode node = getNode(parcelID);
            if (node == null) {
                throw new IllegalArgumentException("Parcel not found: " + parcelID);
            }
            
            ParcelStatus oldStatus = node.status;
            node.status = newStatus;
            
            // Update dispatch tick if parcel is being dispatched
            if (newStatus == ParcelStatus.DISPATCHED) {
                node.dispatchTick = currentTick;
            }
            
            logger.info(String.format("[Status Update] Parcel %s: %s -> %s", 
                parcelID, oldStatus, newStatus));
                
        } catch (Exception e) {
            logger.severe(String.format("[Error] Failed to update status for parcel %s: %s", 
                parcelID, e.getMessage()));
            throw e;
        }
    }
 
    public ParcelNode get(String parcelID) {
        try {
            ParcelNode node = getNode(parcelID);
            if (node == null) {
                throw new IllegalArgumentException("Parcel not found: " + parcelID);
            }
            return node;
        } catch (Exception e) {
            logger.severe(String.format("[Error] Failed to get parcel %s: %s", 
                parcelID, e.getMessage()));
            throw e;
        }
    }

    public void incrementReturnCount(String parcelID) {
        try {
            ParcelNode node = getNode(parcelID);
            if (node == null) {
                throw new IllegalArgumentException("Parcel not found: " + parcelID);
            }
            
            node.returnCount++;
            logger.info(String.format("[Return] Parcel %s return count: %d", 
                parcelID, node.returnCount));
                
        } catch (Exception e) {
            logger.severe(String.format("[Error] Failed to increment return count for parcel %s: %s", 
                parcelID, e.getMessage()));
            throw e;
        }
    }
     public int countStatus(ParcelStatus status) {
        int count = 0;
        for (ParcelNode node : table) {
            while (node != null) {
                if (node.status == status) count++;
                node = node.next;
            }
        }
        return count;
    }

    public double getLoadFactor() {
        return (double) size / capacity;
    }

    public String getTimingStats() {
        int totalDelay = 0;
        int maxDelay = 0;
        String maxDelayParcel = "N/A";
        int processed = 0;
        int returnedMoreThanOnce = 0;

        for (ParcelNode node : table) {
            while (node != null) {
                if (node.status == ParcelStatus.DISPATCHED && node.dispatchTick >= 0) {
                    int delay = node.dispatchTick - node.arrivalTick;
                    if (delay >= 0) {  // Only count valid delays
                        totalDelay += delay;
                        processed++;
                        if (delay > maxDelay) {
                            maxDelay = delay;
                            maxDelayParcel = node.parcelID;
                        }
                    }
                }
                if (node.returnCount > 1) {
                    returnedMoreThanOnce++;
                }
                node = node.next;
            }
        }

        double avgDelay = (processed > 0) ? (double) totalDelay / processed : 0;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Average Parcel Processing Time: %.2f ticks\n", avgDelay));
        sb.append(String.format("Parcel With Longest Delay: %s (%d ticks)\n", 
            maxDelayParcel.equals("N/A") ? "N/A" : maxDelayParcel, maxDelay));
        sb.append(String.format("Parcels Returned More Than Once: %d\n\n", returnedMoreThanOnce));
        return sb.toString();
    }
    
    public boolean exists(String parcelID) {
        return getNode(parcelID) != null;
    }
    
    private ParcelNode getNode(String parcelID) {
        int index = hash(parcelID);
        ParcelNode current = table[index];
        
        while (current != null) {
            if (current.parcelID.equals(parcelID)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    private void resize() {
        int oldCapacity = capacity;
        capacity *= 2;
        ParcelNode[] oldTable = table;
        table = new ParcelNode[capacity];
        size = 0;
        for (int i = 0; i < oldCapacity; i++) {
            ParcelNode current = oldTable[i];
            while (current != null) {
                ParcelNode next = current.next;
                int newIndex = hash(current.parcelID);
                current.next = table[newIndex];
                table[newIndex] = current;
                current = next;
            }
        }
        
        logger.info(String.format("[Resize] Hash table resized to capacity %d", capacity));
    }

    public void setCurrentTick(int tick) {
        this.currentTick = tick;
    }

    private int getCurrentTick() {
        return currentTick;  // Use simulation tick instead of system time
    }
   
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("\n===+ ParcelTracker Statistics +===\n");
        
        stats.append(String.format("Total Parcels: %d\n", size));
        stats.append(String.format("Table Capacity: %d\n", capacity));
        stats.append(String.format("Load Factor: %.2f\n", (double) size / capacity));

        int[] statusCounts = new int[ParcelStatus.values().length];
        int totalReturns = countTotalReturns();
        int maxReturns = 0;
        String mostReturnedParcel = "None";
        int parcelsReturnedMoreThanOnce = 0;

        long totalProcessingTime = 0;
        int processedParcels = 0;
        int maxDelay = 0;
        String longestDelayParcel = "None";

        for (ParcelNode node : table) {
            while (node != null) {
                statusCounts[node.status.ordinal()]++;
                totalReturns += node.returnCount;
                if (node.returnCount > maxReturns) {
                    maxReturns = node.returnCount;
                    mostReturnedParcel = node.parcelID;
                }
                if (node.returnCount > 1) {
                    parcelsReturnedMoreThanOnce++;
                }

                if (node.status == ParcelStatus.DISPATCHED && node.dispatchTick != -1) {
                    int processingTime = node.dispatchTick - node.arrivalTick;
                    totalProcessingTime += processingTime;
                    processedParcels++;
                    
                    if (processingTime > maxDelay) {
                        maxDelay = processingTime;
                        longestDelayParcel = node.parcelID;
                    }
                }
                
                node = node.next;
            }
        }

        stats.append("\nStatus Breakdown:\n");
        for (ParcelStatus status : ParcelStatus.values()) {
            stats.append(String.format("  %s: %d\n", status, statusCounts[status.ordinal()]));
        }
        int parcelsInSystem = statusCounts[ParcelStatus.IN_QUEUE.ordinal()] + 
                            statusCounts[ParcelStatus.SORTED.ordinal()];
        stats.append(String.format("\nParcels Still in System: %d\n", parcelsInSystem));
        
        stats.append("\nReturn Statistics:\n");
        stats.append(String.format("  Total Returns: %d\n", totalReturns));
        stats.append(String.format("  Most Returns: %d (Parcel %s)\n", maxReturns, mostReturnedParcel));
        stats.append(String.format("  Parcels Returned More Than Once: %d\n", parcelsReturnedMoreThanOnce));
        stats.append("\nTiming Statistics:\n");
        if (processedParcels > 0) {
            double avgProcessingTime = (double) totalProcessingTime / processedParcels;
            stats.append(String.format("  Average Processing Time: %.2f ticks\n", avgProcessingTime));
            stats.append(String.format("  Longest Delay: %d ticks (Parcel %s)\n", maxDelay, longestDelayParcel));
        } else {
            stats.append("  No parcels have been processed yet\n");
        }
        
        stats.append("===+ End Statistics +===\n");
        return stats.toString();
    }

    public int countTotalReturns() {
        int totalReturns = 0;
        for (ParcelNode node : table) {
            while (node != null) {
                totalReturns += node.returnCount;
                node = node.next;
            }
        }
        return totalReturns;
    }
}

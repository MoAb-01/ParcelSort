package data_sturcts;
import java.util.logging.*;

public class ParcelTracker {
    private static final Logger logger = Logger.getLogger(ParcelTracker.class.getName());
    private static final int INITIAL_CAPACITY = 30;  // Based on QUEUE_CAPACITY from config.txt
    private static final double LOAD_FACTOR_THRESHOLD = 0.75; 

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
    private int totalGenerated = 0;  // Track total parcels generated
    private int totalEnqueued = 0;   // Track successfully enqueued parcels
    private int totalDispatched = 0; // Track total dispatched parcels
    private int totalReturned = 0;   // Track total returned parcels
    private int[] cityDispatches = new int[5]; // Track dispatches per city

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
            totalGenerated++;  // Increment total generated
            
            if (status == ParcelStatus.IN_QUEUE || status == ParcelStatus.SORTED) {
                totalEnqueued++;  // Only count as enqueued if in queue or sorted
            }
            
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
            
            // Update counts based on status change
            if (oldStatus == ParcelStatus.DISPATCHED && newStatus != ParcelStatus.DISPATCHED) {
                totalDispatched--;
                // Decrement city dispatch count
                for (int i = 0; i < 5; i++) {
                    if (node.destinationCity.equals(getCityName(i))) {
                        cityDispatches[i]--;
                        break;
                    }
                }
            } else if (oldStatus != ParcelStatus.DISPATCHED && newStatus == ParcelStatus.DISPATCHED) {
                totalDispatched++;
                // Increment city dispatch count
                for (int i = 0; i < 5; i++) {
                    if (node.destinationCity.equals(getCityName(i))) {
                        cityDispatches[i]++;
                        break;
                    }
                }
            }
            
            if (oldStatus == ParcelStatus.RETURNED && newStatus != ParcelStatus.RETURNED) {
                totalReturned--;
            } else if (oldStatus != ParcelStatus.RETURNED && newStatus == ParcelStatus.RETURNED) {
                totalReturned++;
            }
            
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
                    if (delay >= 0 && delay <= currentTick) {
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
        
        // Report statistics
        stats.append("\n=== Parcel Statistics ===\n");
        stats.append(String.format("Total Parcels Generated: %d\n", totalGenerated));
        stats.append(String.format("Successfully Enqueued Parcels: %d\n", totalEnqueued));
        stats.append(String.format("Total Dispatched Parcels: %d\n", totalDispatched));
        stats.append(String.format("Total Returned Parcels: %d\n", totalReturned));
        stats.append(String.format("Parcels Still in System: %d\n", countParcelsInSystem()));
        
        // Report city dispatches
        stats.append("\n=== City Dispatch Counts ===\n");
        int cityTotal = 0;
        for (int i = 0; i < 5; i++) {
            stats.append(String.format("%s: %d\n", getCityName(i), cityDispatches[i]));
            cityTotal += cityDispatches[i];
        }
        stats.append(String.format("Total City Dispatches: %d\n", cityTotal));
        
        // Verify counts match
        if (cityTotal != totalDispatched) {
            logger.warning(String.format("City dispatch mismatch: %d != %d",
                cityTotal, totalDispatched));
        }
        
        return stats.toString();
    }

    public int countTotalReturns() {
        int totalReturns = 0;
        for (ParcelNode node : table) {
            while (node != null) {
                if (node.status == ParcelStatus.RETURNED) {
                    totalReturns++;
                }
                node = node.next;
            }
        }
        return totalReturns;
    }

    public int countParcelsInSystem() {
        int inSystem = 0;
        for (ParcelNode node : table) {
            while (node != null) {
                if (node.status == ParcelStatus.IN_QUEUE || 
                    node.status == ParcelStatus.SORTED) {
                    inSystem++;
                }
                node = node.next;
            }
        }
        return inSystem;
    }

    private String getCityName(int index) {
        String[] cities = {"Istanbul", "Ankara", "Izmir", "Bursa", "Antalya"};
        return cities[index];
    }

    public int countCityDispatches(String city) {
        for (int i = 0; i < 5; i++) {
            if (getCityName(i).equals(city)) {
                return cityDispatches[i];
            }
        }
        return 0;
    }

    public String getCityWithMaxDispatches() {
        int maxDispatches = -1;
        String maxCity = "None";
        for (int i = 0; i < 5; i++) {
            if (cityDispatches[i] > maxDispatches) {
                maxDispatches = cityDispatches[i];
                maxCity = getCityName(i);
            }
        }
        return maxCity;
    }
}

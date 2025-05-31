package data_sturcts;
import main.*;

public class ArrivalBuffer {
    private class Node {
        Parcel data;
        Node next;

        public Node(Parcel data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node front;
    private Node rear;
    private int size;
    private final int capacity;

    public ArrivalBuffer(int capacity) {
        this.capacity = capacity;
        this.front = this.rear = null;
        this.size = 0;
    }

    // Ekleme (enqueue)
    public boolean enqueue(Parcel parcel) {
        if (isFull()) {
            System.err.println("Queue overflow! Parcel discarded: " + parcel.getParcelID());
            return false;
        }

        Node newNode = new Node(parcel);

        if (isEmpty()) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
        return true;
    }

    // Çıkarma (dequeue)
    public Parcel dequeue() {
        if (isEmpty()) {
            System.err.println("Queue underflow! No parcels to process.");
            return null;
        }

        Parcel removed = front.data;
        front = front.next;
        size--;

        if (front == null)
            rear = null; // Son elemandıysa rear da null olur

        return removed;
    }

    // Sıradaki parcel'ı göster ama çıkarma
    public Parcel peek() {
        if(isEmpty() )
        {
            return null;
        } 
        return front.data;
    }

    public boolean isFull() {
        return size >= capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    // Debug amaçlı: kuyruğu yazdır
    public void printQueue() {
        Node temp = front;
        System.out.print("ArrivalBuffer [size=" + size + "]: ");
        while (temp != null) {
            System.out.print(temp.data.getParcelID() + " -> ");
            temp = temp.next;
        }
        System.out.println("null");
    }

   //ASCII visualization of the queue::
    public void visualizeQueue() {
        System.out.println("\n=== Arrival Buffer Queue ===");
        System.out.println("+" + "-".repeat(68) + "+");
        
        if (isEmpty()) {
            System.out.println("|" + String.format("%-68s", " Queue is Empty") + "|");
        } else {
            //header::
            System.out.println("| " + String.format("%-8s", "Position") + " | " + 
                             String.format("%-10s", "Parcel ID") + " | " + 
                             String.format("%-8s", "Priority") + " | " + 
                             String.format("%-6s", "Size") + " | " + 
                             String.format("%-8s", "City") + " | " +
                             String.format("%-8s", "Arrival") + " | " +
                             String.format("%-8s", "Status") + " |");
            System.out.println("+" + "-".repeat(68) + "+");
            
            // Print queue contents::
            Node current = front;
            int position = 1;
            while (current != null) {
                String priority = switch(current.data.getPriority()) {
                    case 1 -> "Low";
                    case 2 -> "Medium";
                    case 3 -> "High";
                    default -> "Unknown";
                };
                
                String status = switch(current.data.getStatus()) {
                    case Parcel.Status.InQueue -> "Queued";
                    case Parcel.Status.Sorted -> "Sorted";
                    case Parcel.Status.Dispatched -> "Dispatched";
                    case Parcel.Status.Returned -> "Returned";
                    default -> "Unknown";
                };
                System.out.printf("| %-8d | %-10s | %-8s | %-6s | %-8s | %-8d | %-8s |\n",
                    position++,
                    current.data.getParcelID(),
                    priority,
                    current.data.getSize(),
                    current.data.getDestinationCity(),
                    current.data.getArrivalTick(),
                    status);
                current = current.next;
            }
        }
        
        System.out.println("+" + "-".repeat(68) + "+");
        System.out.printf("| Queue Size: %-3d | Capacity: %-3d |\n", 
            size, 
            capacity);
        System.out.println("+" + "-".repeat(68) + "+");
    }

    public void visualizeQueueStatistics() {
        if (!isEmpty()) {
            System.out.println("\n[Queue Statistics]");
            System.out.println("+" + "-".repeat(40) + "+");
            
            // Priority Distribution
            int highCount = countPriority(3);
            int mediumCount = countPriority(2);
            int lowCount = countPriority(1);
            
            System.out.println("| Priority Distribution:                    |");
            System.out.println("| High: " + String.format("%-3d", highCount) + " | Medium: " + String.format("%-3d", mediumCount) + " | Low: " + String.format("%-3d", lowCount) + " |");
            
            // Size Distribution
            int smallCount = countSize("Small");
            int mediumSizeCount = countSize("Medium");
            int largeCount = countSize("Large");
            
            System.out.println("| Size Distribution:                        |");
            System.out.println("| Small: " + String.format("%-3d", smallCount) + " | Medium: " + String.format("%-3d", mediumSizeCount) + " | Large: " + String.format("%-3d", largeCount) + " |");
            
            // Average Wait Time
            double avgWaitTime = getAverageWaitTime();
            System.out.println("| Average Wait Time: " + String.format("%-20.2f", avgWaitTime) + " |");
            
            System.out.println("+" + "-".repeat(40) + "+");
        }
    }

    public void visualizeSystemState(DestinationSorter destinationSorter, 
                                   ReturnStack returnStack, 
                                   String activeTerminal) {
        System.out.println("\n=== Current System State ===");
        
        // Visualize Arrival Buffer with ASCII
        visualizeQueue();
        visualizeQueueStatistics();

        // Visualize Return Stack
        returnStack.visualizeStack();

        // Visualize Active Terminal
        System.out.println("\n[Active Terminal]");
        System.out.println("+" + "-".repeat(40) + "+");
        System.out.println("|" + String.format("%-38s", " Current: " + activeTerminal) + "|");
        System.out.println("+" + "-".repeat(40) + "+");

        // Visualize City Distribution
        System.out.println("\n[City Distribution]");
        System.out.println("+" + "-".repeat(40) + "+");
        System.out.println("|" + String.format("%-20s", "City") + "|" + String.format("%-17s", "Parcel Count") + "|");
        System.out.println("+" + "-".repeat(20) + "+" + "-".repeat(17) + "+");
        
        String[] cities = {"Istanbul", "Ankara", "Izmir", "Antalya", "Bursa"};
        for (String city : cities) {
            int count = destinationSorter.totalDeliveredTo(city);
            System.out.println("|" + String.format("%-20s", city) + "|" + String.format("%-17d", count) + "|");
        }
        System.out.println("+" + "-".repeat(40) + "+");

        // Let DestinationSorter handle its own visualizations
        destinationSorter.visualizeTreeStatus();
        System.out.println("\n=======================================\n");
    }

    public int countPriority(int priority) {
        int count = 0;
        Node current = front;
        while (current != null) {
            if (current.data.getPriority() == priority) {
                count++;
            }
            current = current.next;
        }
        return count;
    }

    public int countSize(String size) {
        int count = 0;
        Node current = front;
        while (current != null) {
            if (current.data.getSize().equals(size)) {
                count++;
            }
            current = current.next;
        }
        return count;
    }

    public double getAverageWaitTime() {
        if (isEmpty()) {
            return 0.0;
        }
        
        int totalWaitTime = 0;
        int count = 0;
        Node current = front;
        int currentTick = main.Main.getCurrentTick();
        while (current != null) {
            totalWaitTime += (currentTick - current.data.getArrivalTick());
            count++;
            current = current.next;
        }
        if (count > 0) {
            return (double) totalWaitTime / count;
        } else {
            return 0.0;
        }
    }
}

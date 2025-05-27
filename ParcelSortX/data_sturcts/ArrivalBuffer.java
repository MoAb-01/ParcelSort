package data_sturcts;
import main.Parcel;

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
        return isEmpty() ? null : front.data;
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

    // Simple ASCII visualization of the queue
    public void visualizeQueue() {
        System.out.println("\n=== Arrival Buffer Queue ===");
        System.out.println("+" + "-".repeat(68) + "+");
        
        if (isEmpty()) {
            System.out.println("|" + String.format("%-68s", " Queue is Empty") + "|");
        } else {
            // Print header
            System.out.println("| " + String.format("%-8s", "Position") + " | " + 
                             String.format("%-10s", "Parcel ID") + " | " + 
                             String.format("%-8s", "Priority") + " | " + 
                             String.format("%-6s", "Size") + " | " + 
                             String.format("%-8s", "City") + " | " +
                             String.format("%-8s", "Arrival") + " | " +
                             String.format("%-8s", "Status") + " |");
            System.out.println("+" + "-".repeat(68) + "+");
            
            // Print queue contents
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
        
        // Add statistics
        if (!isEmpty()) {
            System.out.println("\nQueue Statistics:");
            System.out.println("+" + "-".repeat(40) + "+");
            
            // Priority Distribution
            int highPriority = countPriority(3);
            int mediumPriority = countPriority(2);
            int lowPriority = countPriority(1);
            
            System.out.println("| Priority Distribution:                    |");
            System.out.printf("| High: %-3d | Medium: %-3d | Low: %-3d |\n",
                highPriority, mediumPriority, lowPriority);
            
            // Size Distribution
            int smallCount = countSize("Small");
            int mediumSizeCount = countSize("Medium");
            int largeCount = countSize("Large");
            
            System.out.println("| Size Distribution:                        |");
            System.out.printf("| Small: %-3d | Medium: %-3d | Large: %-3d |\n",
                smallCount, mediumSizeCount, largeCount);
            
            // Average Wait Time
            double avgWaitTime = getAverageWaitTime();
            System.out.printf("| Average Wait Time: %-20.2f |\n", avgWaitTime);
            
            System.out.println("+" + "-".repeat(40) + "+");
        }
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
        while (current != null) {
            totalWaitTime += (current.data.getArrivalTick() - current.data.getArrivalTick());
            count++;
            current = current.next;
        }
        
        return count > 0 ? (double) totalWaitTime / count : 0.0;
    }
}

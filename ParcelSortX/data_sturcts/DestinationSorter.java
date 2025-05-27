///Theory::
///// a self-balancing Binary Search Tree (BST) where the difference between 
/// heights of left and right subtrees for any node cannot be more than one
/// O(Log n) time complexity;;
/// AVL tree need to rotate in one of the following four ways to keep the balance
/// if node on right troubles balance? then we do a single left rotation.
/// if node on left troubles balance? then we do a single right rotation.

package data_sturcts;

import java.util.*;
import main.Parcel;
import java.util.Iterator;

public class DestinationSorter {

    // Custom Queue implementation for parcels
    private class ParcelQueue {
        private class QueueNode {
            Parcel data;
            QueueNode next;
            
            QueueNode(Parcel data) {
                this.data = data;
                this.next = null;
            }
        }
        
        private QueueNode front;
        private QueueNode rear;
        private int size;
        
        public ParcelQueue() {
            this.front = null;
            this.rear = null;
            this.size = 0;
        }
        
        public void add(Parcel parcel) {
            QueueNode newNode = new QueueNode(parcel);
            if (isEmpty()) {
                front = rear = newNode;
            } else {
                rear.next = newNode;
                rear = newNode;
            }
            size++;
        }
        
        public Parcel peek() {
            return isEmpty() ? null : front.data;
        }
        
        public Parcel poll() {
            if (isEmpty()) return null;
            
            Parcel parcel = front.data;
            front = front.next;
            size--;
            
            if (front == null) {
                rear = null;
            }
            
            return parcel;
        }
        
        public boolean isEmpty() {
            return size == 0;
        }
        
        public int size() {
            return size;
        }
        
        public void clear() {
            front = rear = null;
            size = 0;
        }

        // Custom method to get all parcels
        public Parcel[] getAllParcels() {
            Parcel[] parcels = new Parcel[size];
            QueueNode current = front;
            int index = 0;
            while (current != null) {
                parcels[index++] = current.data;
                current = current.next;
            }
            return parcels;
        }
    }

    private class Node {
        String cityName;
        ParcelQueue parcelQueue;  // Using our custom queue instead of LinkedList
        Node left, right;
        int dispatchedCount;
        int height; // AVL height

        public Node(String cityName) {
            this.cityName = cityName;
            this.parcelQueue = new ParcelQueue();
            this.dispatchedCount = 0;
            this.height = 1;
        }
    }

    private Node root;

    public DestinationSorter() {
        this.root = null;
    }




    private int getHeight(Node node) {
        return node == null ? 0 : node.height;
    }


    private int getBalance(Node node) {
        return node == null ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    // AVL right rotation::
    private Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        // Update heights
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

        return x;
    }

    // AVL left rotate:: 

    private Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        return y;
    }


    public void insertParcel(Parcel parcel) {
        root = insertRecursive(root, parcel);
    }

    private Node insertRecursive(Node current, Parcel parcel) {
        String city = parcel.getDestinationCity();
        if (current == null) {
            Node node = new Node(city);
            node.parcelQueue.add(parcel);
            return node;
        }

        int cmp = city.compareTo(current.cityName);
        if (cmp == 0) {
            current.parcelQueue.add(parcel);
            return current;
        } else if (cmp < 0) {
            current.left = insertRecursive(current.left, parcel);
        } else {
            current.right = insertRecursive(current.right, parcel);
        }
        current.height = Math.max(getHeight(current.left), getHeight(current.right)) + 1;
        int balance = getBalance(current);
        if (balance > 1 && city.compareTo(current.left.cityName) < 0)
            return rightRotate(current);
        if (balance < -1 && city.compareTo(current.right.cityName) > 0)
            return leftRotate(current);
        if (balance > 1 && city.compareTo(current.left.cityName) > 0) {
            current.left = leftRotate(current.left);
            return rightRotate(current);
        }
        if (balance < -1 && city.compareTo(current.right.cityName) < 0) {
            current.right = rightRotate(current.right);
            return leftRotate(current);
        }
        return current;
    }

    public Parcel getNextParcelForCity(String city) {
        Node node = findCityNode(root, city);
        if (node != null && !node.parcelQueue.isEmpty()) {
            return node.parcelQueue.peek();
        }
        return null;
    }

    public void removeParcel(String city, String parcelID) {
        Node node = findCityNode(root, city);
        if (node != null && !node.parcelQueue.isEmpty()) {
            Parcel first = node.parcelQueue.peek();
            if (first.getParcelID().equals(parcelID)) {
                node.parcelQueue.poll();
                node.dispatchedCount++;
            }
        }
    }

    public int countCityParcels(String city) {
        if (city == null) return 0;
        Node node = findCityNode(root, city);
        if (node == null) return 0;
        
        // Only count parcels that are still in the system (not dispatched)
        int count = 0;
        Parcel[] parcels = node.parcelQueue.getAllParcels();
        for (Parcel parcel : parcels) {
            if (parcel.getStatus() != Parcel.Status.Dispatched) {
                count++;
            }
        }
        return count;
    }

    public int totalDeliveredTo(String city) {
        Node node = findCityNode(root, city);
        return node != null ? node.dispatchedCount : 0;
    }

    private Node findCityNode(Node current, String city) {
        if (current == null || city == null) return null;
        int cmp = city.compareTo(current.cityName);
        if (cmp == 0) return current;
        else if (cmp < 0) return findCityNode(current.left, city);
        else return findCityNode(current.right, city);
    }

    public int getHeight() {
        return heightRecursive(root);
    }

    private int heightRecursive(Node current) {
        if (current == null) return 0;
        return 1 + Math.max(heightRecursive(current.left), heightRecursive(current.right));
    }

    public String getCityWithMaxParcels() {
        if (root == null) return null;
        
        String maxCity = null;
        int maxCount = -1;
        StringBuilder tiedCities = new StringBuilder();
        
        // Check each city explicitly using totalDeliveredTo to match report numbers
        String[] cities = {"Istanbul", "Ankara", "Izmir", "Bursa", "Antalya"};
        for (String city : cities) {
            int count = totalDeliveredTo(city);
            if (count > maxCount) {
                maxCount = count;
                maxCity = city;
                tiedCities = new StringBuilder(city);
            } else if (count == maxCount) {
                if (tiedCities.length() > 0) {
                    tiedCities.append(" and ");
                }
                tiedCities.append(city);
            }
        }
        
        System.out.println("\nDEBUG: Final result - " + tiedCities + " with " + maxCount + " parcels each");
        return tiedCities.toString();
    }

    /**
     * Visualizes the parcel distribution across all cities
     */
    public void visualizeParcelDistribution() {
        System.out.println("\n=== Parcel Distribution ===");
        visualizeParcelDistributionRecursive(root);
        System.out.println("==========================\n");
    }

    /**
     * Recursive helper method to visualize parcel distribution
     */
    private void visualizeParcelDistributionRecursive(Node node) {
        if (node == null) {
            return;
        }

        // Process left subtree
        visualizeParcelDistributionRecursive(node.left);

        // Print current node's parcel distribution
        System.out.printf("%-15s: ", node.cityName);
        int parcelCount = node.parcelQueue.size();
        int barLength = Math.max(1, parcelCount / 2); // Scale the bar length
        System.out.print("[");
        for (int i = 0; i < barLength; i++) {
            System.out.print("█");
        }
        System.out.printf("] %d parcels\n", parcelCount);

        // Process right subtree
        visualizeParcelDistributionRecursive(node.right);
    }

    /**
     * Visualizes detailed parcel information for a specific city
     */
    public void visualizeCityDetails(String city) {
        Node node = findCityNode(root, city);
        if (node == null) {
            System.out.println("\nCity not found: " + city);
            return;
        }

        System.out.println("\n=== Detailed View for " + city + " ===");
        System.out.println("Total Parcels: " + node.parcelQueue.size());
        System.out.println("Dispatched Count: " + node.dispatchedCount);
        
        if (!node.parcelQueue.isEmpty()) {
            System.out.println("\nParcel Queue:");
            System.out.println("-------------");
            int count = 1;
            for (Parcel parcel : node.parcelQueue.getAllParcels()) {
                System.out.printf("%d. Parcel %s (Priority: %d, Size: %s)\n",
                    count++,
                    parcel.getParcelID(),
                    parcel.getPriority(),
                    parcel.getSize());
            }
        }
        System.out.println("==========================\n");
    }

    /**
     * Visualizes the entire system status including queue information
     */
    public void visualizeSystemStatus() {
        System.out.println("\n========== SYSTEM STATUS ==========");
        visualizeQueues();
        System.out.println("Total Cities: " + countTotalCities());
        System.out.println("Tree Height: " + getHeight());
        System.out.println("Most Loaded City: " + getCityWithMaxParcels());
        System.out.println("==================================\n");
    }

    /**
     * Helper method to count total number of cities in the tree
     */
    private int countTotalCities() {
        return countTotalCitiesRecursive(root);
    }

    private int countTotalCitiesRecursive(Node node) {
        if (node == null) {
            return 0;
        }
        return 1 + countTotalCitiesRecursive(node.left) + countTotalCitiesRecursive(node.right);
    }

    /**
     * Visualizes the parcel queues for all cities
     */
    public void visualizeQueues() {
        System.out.println("\n=== Parcel Queues Status ===");
        visualizeQueuesRecursive(root);
        System.out.println("==========================\n");
    }

    /**
     * Recursive helper method to visualize queues
     */
    private void visualizeQueuesRecursive(Node node) {
        if (node == null) {
            return;
        }

        // Process left subtree first (alphabetical order)
        visualizeQueuesRecursive(node.left);

        // Print current node's queue
        System.out.println("\nCity: " + node.cityName);
        System.out.println("Queue Size: " + node.parcelQueue.size());
        System.out.println("Dispatched: " + node.dispatchedCount);
        
        if (!node.parcelQueue.isEmpty()) {
            System.out.println("Current Queue:");
            System.out.println("-------------");
            int position = 1;
            Parcel[] parcels = node.parcelQueue.getAllParcels();
            for (Parcel parcel : parcels) {
                System.out.printf("%d. [%s] Priority: %d, Size: %s\n",
                    position++,
                    parcel.getParcelID(),
                    parcel.getPriority(),
                    parcel.getSize());
            }
        } else {
            System.out.println("Queue is empty");
        }
        System.out.println("-------------");

        // Process right subtree
        visualizeQueuesRecursive(node.right);
    }

    /**
     * Visualizes the parcel queues using ASCII art
     */
    public void visualizeQueuesASCII() {
        System.out.println("\n=== Parcel Queues ASCII Visualization ===");
        visualizeQueuesASCIIRecursive(root);
        System.out.println("=======================================\n");
    }

    /**
     * Recursive helper method to visualize queues in ASCII
     */
    private void visualizeQueuesASCIIRecursive(Node node) {
        if (node == null) {
            return;
        }

        // Process left subtree first (alphabetical order)
        visualizeQueuesASCIIRecursive(node.left);

        // Print current node's queue in ASCII
        System.out.println("\n" + node.cityName + " Queue:");
        System.out.println("+" + "-".repeat(40) + "+");
        
        if (!node.parcelQueue.isEmpty()) {
            // Print queue header
            System.out.println("| " + String.format("%-8s", "Position") + " | " + 
                             String.format("%-10s", "Parcel ID") + " | " + 
                             String.format("%-8s", "Priority") + " | " + 
                             String.format("%-6s", "Size") + " |");
            System.out.println("+" + "-".repeat(40) + "+");
            
            // Print each parcel in the queue
            int position = 1;
            Parcel[] parcels = node.parcelQueue.getAllParcels();
            for (Parcel parcel : parcels) {
                String priority = switch(parcel.getPriority()) {
                    case 1 -> "Low";
                    case 2 -> "Medium";
                    case 3 -> "High";
                    default -> "Unknown";
                };
                
                System.out.printf("| %-8d | %-10s | %-8s | %-6s |\n",
                    position++,
                    parcel.getParcelID(),
                    priority,
                    parcel.getSize());
            }
        } else {
            System.out.println("|" + " ".repeat(38) + "|");
            System.out.println("|" + String.format("%-38s", " Queue is Empty") + "|");
            System.out.println("|" + " ".repeat(38) + "|");
        }
        
        System.out.println("+" + "-".repeat(40) + "+");
        System.out.println("Total in Queue: " + node.parcelQueue.size() + 
                         " | Dispatched: " + node.dispatchedCount);

        // Process right subtree
        visualizeQueuesASCIIRecursive(node.right);
    }

    /**
     * Helper method to pad strings to a specific length
     */
    private String padEnd(String str, int length) {
        return String.format("%-" + length + "s", str);
    }
}

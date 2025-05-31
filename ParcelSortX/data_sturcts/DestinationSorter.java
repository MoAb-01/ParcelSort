///Theory::
///// a self-balancing Binary Search Tree (BST) where the difference between 
/// heights of left and right subtrees for any node cannot be more than one
/// O(Log n) time complexity;;
/// AVL tree need to rotate in one of the following four ways to keep the balance
/// if node on right troubles balance? then we do a single left rotation.
/// if node on left troubles balance? then we do a single right rotation.

package data_sturcts;

import java.util.logging.Logger;
import main.*;


public class DestinationSorter {
    private static final Logger logger = Logger.getLogger(DestinationSorter.class.getName());
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
            if(isEmpty()) 
            {
                return null;
            } 
            return front.data;
        }
        
        public void poll() {
            if (isEmpty()) return;
            front = front.next;
            size--;
            if (front == null) {
                rear = null;
            }

        }
        
        public boolean isEmpty() {
            return size == 0;
        }
        
        public int size() {
            return size;
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
        ParcelQueue parcelQueue; 
        Node left, right;
        int height; // AVL height

        public Node(String cityName) {
            this.cityName = cityName;
            this.parcelQueue = new ParcelQueue();
            this.height = 1;
        }
    }

    private Node root;

    public DestinationSorter() {
        this.root = null;
    }




    private int getHeight(Node node) {
        if(node == null)
        {
            return 0;
        } 
        return node.height;
    }


    private int getBalance(Node node) {
        if (node == null) return 0;
        return getHeight(node.left) - getHeight(node.right);
    }
    // AVL right rotation::
    private Node rightRotate(Node y) {
        logger.info(String.format("Performing right rotation at node: %s", y.cityName));
        Node x = y.left;
        Node T2 = x.right;
        // Perform rotation::
        x.right = y;
        y.left = T2;
        // Update heights::
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

        return x;
    }

    // AVL left rotate:: 
    private Node leftRotate(Node x) {
        logger.info(String.format("Performing left rotation at node: %s", x.cityName));
        Node y = x.right;
        Node T2 = y.left;
        // Perform rotation
        y.left = x;
        x.right = T2;
        // Update heights
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

        // Update height of current node
        current.height = 1 + Math.max(getHeight(current.left), getHeight(current.right));

        // Get balance factor
        int balance = getBalance(current);

        // Left Left Case
        if (balance > 1 && city.compareTo(current.left.cityName) < 0)
            return rightRotate(current);

        // Right Right Case
        if (balance < -1 && city.compareTo(current.right.cityName) > 0)
            return leftRotate(current);

        // Left Right Case
        if (balance > 1 && city.compareTo(current.left.cityName) > 0) {
            current.left = leftRotate(current.left);
            return rightRotate(current);
        }

        // Right Left Case
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
                logger.info(String.format("Parcel %s removed from %s",
                    parcelID, city));
            }
        }
    }

    public int countCityParcels(String city) {
        Node node = findCityNode(root, city);
        if(node != null)
        {
            return node.parcelQueue.size();
        }
        return 0;

    }

    public int getDispatchedCount(String city) {
        logger.warning("getDispatchedCount called directly - use ParcelTracker instead");
        return 0;
    }

    public int getTotalDispatched() {
      
        logger.warning("getTotalDispatched called directly");
        return 0;
    }

    public void verifyCounts() {
        logger.warning("verifyCounts called directly");
    }

    
    public int totalDeliveredTo(String city) {
        return getDispatchedCount(city);  // Use single source of truth
    }

    public void incrementDispatchedCount(String city) {
        // Remove this method as it could cause double-counting
        logger.warning("incrementDispatchedCount called - this method should not be used");
    }

    public int getUniqueDeliveredCount(String city) {
        return getDispatchedCount(city);  // Use single source of truth
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
        return 1 + Math.max(
            heightRecursive(current.left),
            heightRecursive(current.right)
        );
    }

    public String getCityWithMaxParcels() {
        if (root == null) return null;
        int maxCount = -1;
        StringBuilder tiedCities = new StringBuilder();
        
        // Check each city explicitly using dispatched count
        String[] cities = {"Istanbul", "Ankara", "Izmir", "Bursa", "Antalya"};
        for (String city : cities) {
            int count = countCityParcels(city);  // Use countCityParcels instead of getDispatchedCount
            if (count > maxCount) {
                maxCount = count;
                tiedCities = new StringBuilder(city);
            } else if (count == maxCount) {
                if (tiedCities.length() > 0) {
                    tiedCities.append(" and ");
                }
                tiedCities.append(city);
            }
        }
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
            System.out.print("x");
        }
        System.out.printf("] %d parcels\n", parcelCount);

        // Process right subtree
        visualizeParcelDistributionRecursive(node.right);
    }


    public void visualizeCityDetails(String city) {
        Node node = findCityNode(root, city);
        if (node == null) {
            System.out.println("\nCity not found: " + city);
            return;
        }

        System.out.println("\n=== Detailed View for " + city + " ===");
        System.out.println("Total Parcels: " + node.parcelQueue.size());
        
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
        System.out.println("Total in Queue: " + node.parcelQueue.size());

        // Process right subtree
        visualizeQueuesASCIIRecursive(node.right);
    }

    public boolean verifyBalance() {
        return verifyBalanceRecursive(root);
    }

    private boolean verifyBalanceRecursive(Node node) {
        if (node == null) return true;
        
        int balance = getBalance(node);
        if (Math.abs(balance) > 1) {
            logger.warning(String.format("Unbalanced node found: %s (balance: %d)", 
                node.cityName, balance));
            return false;
        }
        
        return verifyBalanceRecursive(node.left) && verifyBalanceRecursive(node.right);
    }

    // Add method to get total parcels in BST
    public int getTotalParcels() {
        return getTotalParcelsRecursive(root);
    }

    private int getTotalParcelsRecursive(Node node) {
        if (node == null) return 0;
        return node.parcelQueue.size() + 
               getTotalParcelsRecursive(node.left) + 
               getTotalParcelsRecursive(node.right);
    }

    /**
     * Performs in-order traversal of the BST, visiting cities in alphabetical order
     */
    public void inOrderTraversal() {
        System.out.println("\n=== In-Order Traversal of Cities ===");
        inOrderTraversalRecursive(root);
        System.out.println("==================================\n");
    }

    private void inOrderTraversalRecursive(Node node) {
        if (node != null) {
            // Visit left subtree
            inOrderTraversalRecursive(node.left);
            
            // Visit current node
            System.out.printf("City: %-10s | Parcels: %d\n", 
                node.cityName, 
                node.parcelQueue.size());
            
            // Visit right subtree
            inOrderTraversalRecursive(node.right);
        }
    }

    /**
     * Returns the queue of parcels for a specific city
     * @param city The city to get parcels for
     * @return The queue of parcels for the city, or null if city not found
     */
    public ParcelQueue getCityParcels(String city) {
        Node node = findCityNode(root, city);
        if(node != null)
        {
            return node.parcelQueue;
        }   
        return null;
    }

    /**
     * Visualizes all tree-related information in one method
     */
    public void visualizeTreeStatus() {
        // Visualize Parcel Distribution
        System.out.println("\n[Parcel Distribution]");
        System.out.println("+" + "-".repeat(40) + "+");
        visualizeParcelDistribution();
        System.out.println("+" + "-".repeat(40) + "+");

        // Visualize System Status
        System.out.println("\n[System Status]");
        System.out.println("+" + "-".repeat(40) + "+");
        visualizeSystemStatus();
        System.out.println("+" + "-".repeat(40) + "+");

        // Visualize Detailed Queue Status
        System.out.println("\n[Detailed Queue Status]");
        System.out.println("+" + "-".repeat(40) + "+");
        visualizeQueuesASCII();
        System.out.println("+" + "-".repeat(40) + "+");
    }
}

package data_sturcts;

import java.util.logging.*;

public class TerminalRotator {
    private static final Logger logger = Logger.getLogger(TerminalRotator.class.getName());
    
    private class Node {
        String cityName;
        Node next;
        int pendingParcels;
        
        Node(String cityName) {
            this.cityName = cityName;
            this.next = null;
            this.pendingParcels = 0;
        }
    }
    
    private class RotationEvent {
        int tick;
        String fromTerminal;
        String toTerminal;
        int pendingParcels;
        RotationEvent next;
        
        RotationEvent(int tick, String fromTerminal, String toTerminal, int pendingParcels) {
            this.tick = tick;
            this.fromTerminal = fromTerminal;
            this.toTerminal = toTerminal;
            this.pendingParcels = pendingParcels;
            this.next = null;
        }
    }
    
    private Node head;
    private Node tail;
    private Node currentActiveTerminal;
    private int rotationInterval;
    private int tickCounter;
    private boolean variableSpeedEnabled;
    private RotationEvent rotationHistoryHead;
    private RotationEvent rotationHistoryTail;
    
    public TerminalRotator(int rotationInterval) {
        this.rotationInterval = rotationInterval;
        this.tickCounter = 0;
        this.head = null;
        this.tail = null;
        this.variableSpeedEnabled = false;
        this.rotationHistoryHead = null;
        this.rotationHistoryTail = null;
        logger.info(String.format("[Initialize] TerminalRotator created with rotation interval %d", rotationInterval));
    }
    
    // Enable/disable variable speed rotation
    public void setVariableSpeedEnabled(boolean enabled) {
        this.variableSpeedEnabled = enabled;
        logger.info(String.format("[Config] Variable speed rotation %s", enabled ? "enabled" : "disabled"));
    }
    
    // Update pending parcels for a terminal
    public void updatePendingParcels(String cityName, int count) {
        if (head == null) {
            logger.warning("[Error] Terminal list not initialized");
            return;
        }
        
        Node current = head;
        do {
            if (current.cityName.equals(cityName)) {
                current.pendingParcels = count;
                logger.info(String.format("[Update] Terminal %s pending parcels: %d", cityName, count));
                return;
            }
            current = current.next;
        } while (current != head);
        
        logger.warning(String.format("[Error] Terminal %s not found", cityName));
    }
    
    // Initialize the circular list from city array
    public void initializeFromCityList(String[] cityArray) {
        if (cityArray == null || cityArray.length == 0) {
            logger.severe("[Error] City list cannot be empty");
            throw new IllegalArgumentException("City list cannot be empty");
        }
        
        // Validate each city name
        for (int i = 0; i < cityArray.length; i++) {
            if (cityArray[i] == null || cityArray[i].trim().isEmpty()) {
                logger.severe(String.format("[Error] Invalid city name at index %d", i));
                throw new IllegalArgumentException("Invalid city name at index " + i);
            }
        }
        
        // Clear existing list if any
        head = null;
        tail = null;
        
        // Create first node
        head = new Node(cityArray[0]);
        tail = head;
        
        // Create remaining nodes
        for (int i = 1; i < cityArray.length; i++) {
            Node newNode = new Node(cityArray[i]);
            tail.next = newNode;
            tail = newNode;
        }
        tail.next = head;
 
        currentActiveTerminal = head;
        
        logger.info(String.format("[Initialize] Terminal list created with %d cities", cityArray.length));
        printTerminalOrder();
    }

    public void advanceTerminal() {
        if (currentActiveTerminal == null) {
            logger.warning("[Error] Terminal list not initialized, skipping rotation");
            return;
        }
        
        String oldTerminal = currentActiveTerminal.cityName;
        
        if (variableSpeedEnabled) {

            Node highestLoad = currentActiveTerminal;
            Node current = currentActiveTerminal.next;
            while (current != currentActiveTerminal) {
                if (current.pendingParcels > highestLoad.pendingParcels) {
                    highestLoad = current;
                }
                current = current.next;
            }
            
            currentActiveTerminal = highestLoad;
            logger.info(String.format("Terminal Change: %s -> %s (Priority: %d pending parcels)", 
                oldTerminal, currentActiveTerminal.cityName, currentActiveTerminal.pendingParcels));
        } else {
            currentActiveTerminal = currentActiveTerminal.next;
            logger.info(String.format("Terminal Change: %s -> %s", oldTerminal, currentActiveTerminal.cityName));
        }
    }

    public String getActiveTerminal() {
        if (currentActiveTerminal == null) {
            logger.warning("[Error] Terminal list not initialized, skipping dispatch");
            return null;
        }
        return currentActiveTerminal.cityName;
    }

    public void printTerminalOrder() {
        if (head == null) {
            logger.warning("[Status] Terminal list is empty");
            return;
        }
        
        StringBuilder order = new StringBuilder();
        order.append("Terminal Order: ");
        
        Node current = head;
        do {
            order.append(current.cityName);
            if (current.pendingParcels > 0) {
                order.append(String.format("(%d)", current.pendingParcels));
            }
            if (current.next != head) {
                order.append(" -> ");
            }
            current = current.next;
        } while (current != head);
        
        logger.info(order.toString());
    }

    public void updateTick(int globalTick) {
        tickCounter++;
        logger.info(String.format("[Tick %d]", globalTick));
        logger.info(String.format("  Active Terminal: %s", getActiveTerminal()));
        
        if (tickCounter >= rotationInterval) {
            String oldTerminal = currentActiveTerminal.cityName;
            advanceTerminal();
            RotationEvent newEvent = new RotationEvent(globalTick, oldTerminal, 
                currentActiveTerminal.cityName, currentActiveTerminal.pendingParcels);
            
            if (rotationHistoryHead == null) {
                rotationHistoryHead = newEvent;
                rotationHistoryTail = newEvent;
            } else {
                rotationHistoryTail.next = newEvent;
                rotationHistoryTail = newEvent;
            }
            
            tickCounter = 0;
        }
    }

    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("\n===+ Terminal Rotator Statistics +===\n");
        

        int terminalCount = 0;
        int totalPendingParcels = 0;
        if (head != null) {
            Node current = head;
            do {
                terminalCount++;
                totalPendingParcels += current.pendingParcels;
                current = current.next;
            } while (current != head);
        }
        
        stats.append(String.format("Total Terminals: %d\n", terminalCount));
        stats.append(String.format("Rotation Interval: %d ticks\n", rotationInterval));
        stats.append(String.format("Variable Speed: %s\n", variableSpeedEnabled ? "Enabled" : "Disabled"));
        stats.append(String.format("Total Pending Parcels: %d\n", totalPendingParcels));
        stats.append(String.format("Current Active Terminal: %s\n", getActiveTerminal()));
           // Print rotation order with pending parcels
        stats.append("\nTerminal Rotation Order:\n");
        if (head != null) {
            Node current = head;
            do {
                stats.append(String.format("  %s%s (Pending: %d)\n", 
                    current.cityName,
                    (current == currentActiveTerminal) ? " (Active)" : "",
                    current.pendingParcels));
                current = current.next;
            } while (current != head);
        }
        
      
        if (rotationHistoryHead != null) {
            stats.append("\nRotation Timeline:\n");
            RotationEvent current = rotationHistoryHead;
            while (current != null) {
                stats.append(String.format("  Tick %d: %s -> %s (Pending: %d)\n",
                    current.tick, current.fromTerminal, current.toTerminal, current.pendingParcels));
                current = current.next;
            }
        }
        
        stats.append("===+ End Statistics +===\n");
        return stats.toString();
    }
}
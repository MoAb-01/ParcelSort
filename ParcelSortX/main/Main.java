package main;

import data_sturcts.*;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
  
    private static void QueueVis(ArrivalBuffer arrivalBuffer, DestinationSorter destinationSorter, 
                                      ReturnStack returnStack, String activeTerminal) {
        System.out.println("\n=== Current System State ===");
        
        // Visualize Arrival Buffer with ASCII
        arrivalBuffer.visualizeQueue();

        // Visualize Return Stack
        System.out.println("\n[Return Stack]");
        System.out.println("+" + "-".repeat(40) + "+");
        if (!returnStack.isEmpty()) {
            System.out.println("|" + String.format("%-38s", " Stack Size: " + returnStack.size()) + "|");
        } else {
            System.out.println("|" + String.format("%-38s", " Stack is Empty") + "|");
        }
        System.out.println("+" + "-".repeat(40) + "+");

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

        // Enhanced Queue Statistics
        System.out.println("\n[Queue Statistics]");
        System.out.println("+" + "-".repeat(40) + "+");
        
        // Priority Distribution
        int highCount = arrivalBuffer.countPriority(3);
        int mediumCount = arrivalBuffer.countPriority(2);
        int lowCount = arrivalBuffer.countPriority(1);
        
        System.out.println("| Priority Distribution:                    |");
        System.out.println("| High: " + String.format("%-3d", highCount) + " | Medium: " + String.format("%-3d", mediumCount) + " | Low: " + String.format("%-3d", lowCount) + " |");
        
        // Size Distribution
        int smallCount = arrivalBuffer.countSize("Small");
        int mediumSizeCount = arrivalBuffer.countSize("Medium");
        int largeCount = arrivalBuffer.countSize("Large");
        
        System.out.println("| Size Distribution:                        |");
        System.out.println("| Small: " + String.format("%-3d", smallCount) + " | Medium: " + String.format("%-3d", mediumSizeCount) + " | Large: " + String.format("%-3d", largeCount) + " |");
        
        // Average Wait Time
        double avgWaitTime = arrivalBuffer.getAverageWaitTime();
        System.out.println("| Average Wait Time: " + String.format("%-20.2f", avgWaitTime) + " |");
        
        System.out.println("+" + "-".repeat(40) + "+");

        System.out.println("\n=======================================\n");
    }

    public static void main(String[] args) {
        try {
            // 1. Ayarları yüke
            ConfigManager config = new ConfigManager("ParcelSortX/config.txt");

            int maxTicks = config.getMaxTicks();
            int queueCapacity = config.getQueueCapacity();
            int terminalRotationInterval = config.getTerminalRotationInterval();
            int parcelMin = config.getParcelPerTickMin();
            int parcelMax = config.getParcelPerTickMax();
            double misroutingRate = config.getMisroutingRate();
            String[] cityList = config.getCityList();

            // 2. Yapıları başlat
            ParcelGenerator generator = new ParcelGenerator(cityList, parcelMin, parcelMax);
            ArrivalBuffer arrivalBuffer = new ArrivalBuffer(queueCapacity);
            DestinationSorter destinationSorter = new DestinationSorter();
            TerminalRotator terminalRotator = new TerminalRotator(terminalRotationInterval);
            terminalRotator.initializeFromCityList(cityList);
            ReturnStack returnStack = new ReturnStack();
            ParcelTracker parcelTracker = new ParcelTracker();

            // Distribution counters
            int highPriorityCount = 0;
            int mediumPriorityCount = 0;
            int lowPriorityCount = 0;
            int smallSizeCount = 0;
            int mediumSizeCount = 0;
            int largeSizeCount = 0;

            FileWriter logWriter = new FileWriter("log.txt");

            int tick = 0;
            int maxQueueSize = 0;
            int maxStackSize = 0;

            while (tick < maxTicks) {
                tick++;
                logWriter.write("[Tick " + tick + "]\n");
                
                // Update current tick in ParcelTracker
                parcelTracker.setCurrentTick(tick);

                // Reset distribution counters for this tick
                highPriorityCount = 0;
                mediumPriorityCount = 0;
                lowPriorityCount = 0;
                smallSizeCount = 0;
                mediumSizeCount = 0;
                largeSizeCount = 0;

                // Yeni kargo oluştur
                Parcel[] newParcels = generator.generateParcelsForTick(tick);
                StringBuilder newParcelLog = new StringBuilder();
                
                // Use a fixed-size array instead of ArrayList
                String[] sortedParcelIDs = new String[newParcels.length];
                int sortedCount = 0;  // Keep track of how many parcels we've sorted
                
                for (Parcel p : newParcels) {
                    boolean added = arrivalBuffer.enqueue(p);
                    if (added) {
                        ParcelGenerator.incrementSuccessfullyEnqueuedCount();
                        parcelTracker.insert(p.getParcelID(), ParcelTracker.ParcelStatus.IN_QUEUE, p.getArrivalTick(),
                                p.getDestinationCity(), p.getPriority(), p.getSize());
                        newParcelLog.append(String.format("%s to %s (Priority %d), ", p.getParcelID(), p.getDestinationCity(), p.getPriority()));
                        
                        // Update distribution counters
                        switch(p.getPriority()) {
                            case 1 -> lowPriorityCount++;
                            case 2 -> mediumPriorityCount++;
                            case 3 -> highPriorityCount++;
                        }
                        switch(p.getSize()) {
                            case "Small" -> smallSizeCount++;
                            case "Medium" -> mediumSizeCount++;
                            case "Large" -> largeSizeCount++;
                        }
                    }
                }
                if (newParcelLog.length() > 0) {
                    newParcelLog.setLength(newParcelLog.length() - 2); // Remove last comma
                    logWriter.write("New Parcels: " + newParcelLog + "\n");
                }

                // Process parcels
                int parcelsToProcess = Math.min(arrivalBuffer.size(), 2);
                for (int i = 0; i < parcelsToProcess; i++) {
                    if (arrivalBuffer.isEmpty()) break;
                    
                    Parcel p = arrivalBuffer.dequeue();
                    destinationSorter.insertParcel(p);
                    parcelTracker.updateStatus(p.getParcelID(), ParcelTracker.ParcelStatus.SORTED);
                    if (sortedCount < sortedParcelIDs.length) {
                        sortedParcelIDs[sortedCount++] = p.getParcelID();
                    }
                }
                
                // Write sorted parcels to log
                if (sortedCount > 0) {
                    StringBuilder sortedLog = new StringBuilder();
                    for (int i = 0; i < sortedCount; i++) {
                        sortedLog.append(sortedParcelIDs[i]);
                        if (i < sortedCount - 1) {
                            sortedLog.append(", ");
                        }
                    }
                    logWriter.write("Sorted to BST: " + sortedLog + "\n");
                }

                // Update max queue size after processing
                maxQueueSize = Math.max(maxQueueSize, arrivalBuffer.size());
                logWriter.write("Queue Size: " + arrivalBuffer.size() + "\n");

                // Show queue state with distribution statistics
                if (!arrivalBuffer.isEmpty()) {
                    System.out.println("\n=== Queue State After New Arrivals ===");
                    arrivalBuffer.visualizeQueue();
                    
                    // Show distribution statistics
                    System.out.println("\n[Queue Statistics]");
                    System.out.println("+" + "-".repeat(40) + "+");
                    System.out.println("| Priority Distribution:                    |");
                    System.out.printf("| High: %-3d | Medium: %-3d | Low: %-3d |\n",
                        highPriorityCount, mediumPriorityCount, lowPriorityCount);
                    System.out.println("| Size Distribution:                        |");
                    System.out.printf("| Small: %-3d | Medium: %-3d | Large: %-3d |\n",
                        smallSizeCount, mediumSizeCount, largeSizeCount);
                    System.out.println("+" + "-".repeat(40) + "+");
                }

                // Show queue state after processing
                if (sortedCount > 0) {
                    System.out.println("\n=== Queue State After Processing ===");
                    arrivalBuffer.visualizeQueue();
                    System.out.println("All parcels have been moved to the destination sorter.");
                }

                // Aktif terminal ve dispatch
                String activeCity = terminalRotator.getActiveTerminal();
                Parcel nextParcel = destinationSorter.getNextParcelForCity(activeCity);
                if (nextParcel != null) {
                    boolean misrouted = Math.random() < misroutingRate;
                    if (misrouted) {
                        returnStack.push(nextParcel);
                        parcelTracker.updateStatus(nextParcel.getParcelID(), ParcelTracker.ParcelStatus.RETURNED);
                        parcelTracker.incrementReturnCount(nextParcel.getParcelID());
                        logWriter.write(String.format("Returned: %s misrouted -> Pushed to ReturnStack\n", nextParcel.getParcelID()));
                    } else {
                        // Remove the parcel and update status - removeParcel handles the counting
                        destinationSorter.removeParcel(activeCity, nextParcel.getParcelID());
                        parcelTracker.updateStatus(nextParcel.getParcelID(), ParcelTracker.ParcelStatus.DISPATCHED);
                        logWriter.write(String.format("Dispatched: %s from BST to %s -> Success\n", nextParcel.getParcelID(), activeCity));
                    }
                }

                // ReturnStack yeniden işleme (her 3 tickte bir)
                if (tick % 3 == 0 && !returnStack.isEmpty()) {
                    Parcel returned = returnStack.pop();
                    destinationSorter.insertParcel(returned);
                    parcelTracker.updateStatus(returned.getParcelID(), ParcelTracker.ParcelStatus.SORTED);
                    logWriter.write("Reprocessed from ReturnStack: " + returned.getParcelID() + "\n");
                }

                maxStackSize = Math.max(maxStackSize, returnStack.size());

                // Terminal rotasyonu kontrolü
                String oldTerminal = activeCity;
                terminalRotator.updateTick(tick);
                String newTerminal = terminalRotator.getActiveTerminal();
                if (!oldTerminal.equals(newTerminal)) {
                    logWriter.write("Terminal Rotated to: " + newTerminal + "\n");
                }

                // Tick log üzeti
                logWriter.write("Active Terminal: " + newTerminal + "\n");
                logWriter.write("ReturnStack Size: " + returnStack.size() + "\n");

                for (String city : cityList) {
                    int count = destinationSorter.countCityParcels(city);
                    if (count > 0) {
                        logWriter.write(String.format("  %s: %d parcel(s)\n", city, count));
                    }
                }
                // Her 5 tıklamada bir görselleştirme ekle
                if (tick % 5 == 0) {
                    QueueVis(arrivalBuffer, destinationSorter, returnStack, newTerminal);
                }

                logWriter.write("-----------------------------\n");
            }

            logWriter.close();

            // Final raporu yaz (report.txt)
            FileWriter report = new FileWriter("report.txt");
            report.write("=== Simulation Report ===\n\n");
            report.write("1. Simulation Overview\n");
            report.write("------------------------\n");
            report.write("Total Ticks Executed: " + tick + "\n");
            report.write("Total Parcels Generated: " + ParcelGenerator.getTotalGeneratedCount() + "\n");
            report.write("Successfully Enqueued Parcels: " + ParcelGenerator.getSuccessfullyEnqueuedCount() + "\n\n");

            report.write("2. Parcel Statistics\n");
            report.write("------------------------\n");
            int dispatchedCount = parcelTracker.countStatus(ParcelTracker.ParcelStatus.DISPATCHED);
            report.write("Total Dispatched Parcels: " + dispatchedCount + "\n");
            int returnedCount = parcelTracker.countTotalReturns();
            report.write("Total Returned Parcels: " + returnedCount + "\n");
            int inSystem = parcelTracker.countStatus(ParcelTracker.ParcelStatus.SORTED)
                          + parcelTracker.countStatus(ParcelTracker.ParcelStatus.IN_QUEUE);
            report.write("Parcels Still in Queue/BST/Stack: " + inSystem + "\n\n");

            report.write("3. Destination Metrics\n");
            report.write("------------------------\n");
            String[] cities = {"Istanbul", "Ankara", "Izmir", "Bursa", "Antalya"};
            int totalDispatched = 0;
            for (String city : cities) {
                int dispatched = parcelTracker.countCityDispatches(city);
                totalDispatched += dispatched;
                report.write(String.format("%s: %d parcels dispatched\n", city, dispatched));
            }
            report.write(String.format("Total Dispatched: %d\n", totalDispatched));
            report.write(String.format("Verification: Sum of city dispatches = %d\n", totalDispatched));
            report.write("Most Frequently Targeted Destination: " + 
                parcelTracker.getCityWithMaxDispatches() + "\n\n");

            report.write("4. Timing and Delay Metrics\n");
            report.write("-----------------------------\n");
            report.write(parcelTracker.getTimingStats());

            report.write("5. Data Structure Statistics\n");
            report.write("-----------------------------\n");
            report.write("Maximum Queue Size Observed: " + maxQueueSize + "\n");
            report.write("Maximum Stack Size Observed: " + maxStackSize + "\n");
            report.write("Final Height of BST: " + destinationSorter.getHeight() + "\n");
            report.write("Total Parcels in BST: " + destinationSorter.getTotalParcels() + "\n");
            report.write("BST Balance Check: " + (destinationSorter.verifyBalance() ? "Balanced" : "Unbalanced") + "\n");
            report.write("Hash Table Load Factor: " + parcelTracker.getLoadFactor() + "\n");

            report.close();
            System.out.println("Simulation completed. Check Report File For Details.");

        } catch (IOException e) {
            System.err.println("Failed to load config or write files: " + e.getMessage());
        }
    }
}

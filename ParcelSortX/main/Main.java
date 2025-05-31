package main;

import data_sturcts.*;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static int currentTick = 0;  // Add static currentTick field
  
    public static int getCurrentTick() {
        return currentTick;
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
                currentTick = tick;  // Update the static currentTick field
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
                    arrivalBuffer.visualizeSystemState(destinationSorter, returnStack, newTerminal);
                }

                logWriter.write("-----------------------------\n");
            }

            // Export final state to file
            parcelTracker.exportToFile("parcel_tracker_state.txt");
            
            // Final statistics
            logWriter.write("\n=== Final Statistics ===\n");
            logWriter.write("Max Queue Size: " + maxQueueSize + "\n");
            logWriter.write("Max Stack Size: " + maxStackSize + "\n");
            logWriter.write(parcelTracker.getStatistics());
            logWriter.write(parcelTracker.getTimingStats());
            logWriter.close();
            
            System.out.println("\nSimulation completed. Check log.txt for details.");
            System.out.println("Parcel tracker state exported to parcel_tracker_state.txt");

        } catch (IOException e) {
            System.err.println("Failed to load config or write files: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

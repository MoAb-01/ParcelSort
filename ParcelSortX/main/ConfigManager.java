package main;
import java.io.*;
import java.util.*;

public class ConfigManager {
    private int maxTicks;
    private int queueCapacity;
    private int terminalRotationInterval;
    private int parcelPerTickMin;
    private int parcelPerTickMax;
    private double misroutingRate;
    private String[] cityList;

    public ConfigManager(String filePath) throws IOException {
        loadConfig(filePath);
    }

    private void loadConfig(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
                continue;

            String[] parts = line.split("=", 2);
            if (parts.length != 2)
                continue;

            String key = parts[0].trim().toUpperCase();
            String value = parts[1].trim();

            switch (key) {
                case "MAX_TICKS":
                    maxTicks = Integer.parseInt(value);
                    break;
                case "QUEUE_CAPACITY":
                    queueCapacity = Integer.parseInt(value);
                    break;
                case "TERMINAL_ROTATION_INTERVAL":
                    terminalRotationInterval = Integer.parseInt(value);
                    break;
                case "PARCEL_PER_TICK_MIN":
                    parcelPerTickMin = Integer.parseInt(value);
                    break;
                case "PARCEL_PER_TICK_MAX":
                    parcelPerTickMax = Integer.parseInt(value);
                    break;
                case "MISROUTING_RATE":
                    misroutingRate = Double.parseDouble(value);
                    break;
                case "CITY_LIST":
                    cityList = Arrays.stream(value.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toArray(String[]::new);
                    break;
                default:
                    System.err.println("Unknown config key: " + key);
            }
        }

        reader.close();
    }

    // Getter metotları
    public int getMaxTicks() {
        return maxTicks;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public int getTerminalRotationInterval() {
        return terminalRotationInterval;
    }

    public int getParcelPerTickMin() {
        return parcelPerTickMin;
    }

    public int getParcelPerTickMax() {
        return parcelPerTickMax;
    }

    public double getMisroutingRate() {
        return misroutingRate;
    }

    public String[] getCityList() {
        return cityList;
    }
}

package main;

import java.util.Random;

public class ParcelGenerator {

    private static int parcelCounter = 0; // Benzersiz ID üretimi
    private static final Random random = new Random();

    private final String[] cityList;
    private final String[] sizeList = { "Small", "Medium", "Large" };
    private final int minParcelsPerTick;
    private final int maxParcelsPerTick;

    public ParcelGenerator(String[] cityList, int minParcelsPerTick, int maxParcelsPerTick) {
        this.cityList = cityList;
        this.minParcelsPerTick = minParcelsPerTick;
        this.maxParcelsPerTick = maxParcelsPerTick;
    }

    // Rastgele bir Parcel oluşturur
    public Parcel generateSingleParcel(int currentTick) {
        String id = "P0" + (parcelCounter++);
        String destination = cityList[random.nextInt(cityList.length)];
        String size = sizeList[random.nextInt(sizeList.length)];
        int priority = 1 + random.nextInt(3); // 1, 2 veya 3

        return new Parcel(id, destination, priority, size, currentTick);
    }

    // Bir tick için 1..N arası parcel üretir
    public Parcel[] generateParcelsForTick(int currentTick) {
        int count = minParcelsPerTick + random.nextInt(maxParcelsPerTick - minParcelsPerTick + 1);
        Parcel[] parcels = new Parcel[count];
        for (int i = 0; i < count; i++) {
            parcels[i] = generateSingleParcel(currentTick);
        }
        return parcels;
    }

    // Toplam oluşturulan parcel sayısını verir (test amaçlı)
    public static int getTotalGeneratedCount() {
        return parcelCounter;
    }
}

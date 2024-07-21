package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrafficTrackerTest2 {
    private final static String VAULT_NAME = "/test1";

    TrafficTracker trafficTracker;

    @BeforeEach
    private void setup() {
        Locale.setDefault(Locale.ENGLISH);
        trafficTracker = new TrafficTracker();

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");

        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");
    }

    @Test
    public void noCarsWithViolations() {
        int totalCarsFound = 0;

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");
        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");

        for (Violation violation : trafficTracker.getViolations()) {
            if (violation.getCar().getCarType().equals(Car.CarType.Car)) totalCarsFound++;
        }

        assertEquals(0, totalCarsFound);
    }

    @Test
    public void checkIfThereAreFinesOnEmptyTracker() {
        assertTrue(new TrafficTracker().calculateTotalFines() <= 0, "There are fines registered in the trafficTracker");
    }

    @Test
    public void noViolationsInUtrecht(){
        int totalCarsFound = 0;
        for (Violation violation : trafficTracker.getViolations()) {
            if (violation.getCity().equals("Utrecht")) totalCarsFound++;
        }
        assertEquals(0, totalCarsFound);
    }

    @Test
    public void topVioltionIsInAmsterdam(){
        assertEquals("Amsterdam", trafficTracker.topViolationsByCity(55).get(0).getCity(), "The top violation is from Amsterdam");
    }

    @Test
    public void carWithTheMostViolation(){
        assertEquals("227-HX-3",trafficTracker.topViolationsByCar(1).get(0).getCar().getLicensePlate(), "The car with the most violations in the whole data set");
    }
}

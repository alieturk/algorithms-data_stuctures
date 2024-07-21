package models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DetectionTest2 {

    @Test
    public void testValidatePurple() {
        // Create a truck with an emission category of 5
        Car truck = new Car("AB-123-CD", 5, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(2023, 1, 1));
        // Create a detection in a purple zone with the truck
        Detection detection = new Detection(truck, "Amterdam", LocalDateTime.now());
        // Ensure that the validatePurple method returns a violation
        assertNotNull(detection.validatePurple());
        // Create a coach with an emission category of 6
        Car coach = new Car("TEST-123-CD",6, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2023, 1, 1));
        // Create a detection in a purple zone with the coach
        detection = new Detection(coach, "Den Haag", LocalDateTime.now());
        // Ensure that the validatePurple method does not return a violation
        assertNull(detection.validatePurple());
    }
}

package nl.hva.ict.ads.elections.models;

import org.junit.jupiter.api.Test;

import java.util.NavigableSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AdditionalTests {

    @Test
    void getPollingStationsByZipCodeRangeShouldReturnSubset() {
        PollingStation pollingStation1 = new PollingStation("Station 1", "1091AA", "Location 1");
        PollingStation pollingStation2 = new PollingStation("Station 2", "1092BB", "Location 2");
        PollingStation pollingStation3 = new PollingStation("Station 3", "1093CC", "Location 3");

        Constituency constituency = new Constituency(1, "Test Constituency");
        constituency.getPollingStations().add(pollingStation1);
        constituency.getPollingStations().add(pollingStation2);
        constituency.getPollingStations().add(pollingStation3);

        NavigableSet<PollingStation> pollingStationsInRange1 = constituency.getPollingStationsByZipCodeRange("1091AA", "1092ZZ");
        NavigableSet<PollingStation> pollingStationsInRange2 = constituency.getPollingStationsByZipCodeRange("1091AA", "1091ZZ");
        NavigableSet<PollingStation> pollingStationsInRange3 = constituency.getPollingStationsByZipCodeRange("1000AA", "1000ZZ");

        assertEquals(Set.of(pollingStation1, pollingStation2), pollingStationsInRange1,
                "Could not retrieve the subset of polling stations within the zip code range.");

        assertEquals(Set.of(pollingStation1), pollingStationsInRange2,
                "Could not retrieve the subset of polling stations within the zip code range.");

        assertEquals(Set.of(), pollingStationsInRange3,
                "Could not retrieve the subset of polling stations within the zip code range.");
    }


    @Test
    void testEqualsAndHashCode() {
        Party party1 = new Party(1, "Party A");
        Party party2 = new Party(1, "Party A");
        Party party3 = new Party(2, "Party B");

        assertEquals(party1, party2, "Party objects with the same ID and name should be considered equal.");
        assertEquals(party1.hashCode(), party2.hashCode(), "Equal objects should have the same hash code.");

        assertNotEquals(party1, party3, "Party objects with different IDs should not be considered equal.");
        assertNotEquals(party1.hashCode(), party3.hashCode(), "Different objects should have different hash codes.");
    }

    @Test
    void testToString() {
        Party party = new Party(1, "Party A");
        String toStringResult = party.toString();
        assertEquals("Party{id=1,name='Party A'}", toStringResult, "ToString representation is incorrect.");
    }


}

package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class SummaryTest {
    private static Election election;

    @BeforeAll
    static void setup() throws IOException, XMLStreamException {
        election = Election.importFromDataFolder(PathUtils.getResourcePath("/EML_bestanden_TK2021_HvA_UvA"));
    }

    @Test
    public void testGetCandidatesWithDuplicateNames() {
        Set<Candidate> duplicateCandidates = election.getCandidatesWithDuplicateNames();

        // Verify the size of the returned set.
        assertEquals(6, duplicateCandidates.size(), "The number of candidates with duplicate names is not as expected");
    }

    @Test
    public void testGetPollingStationsByZipCodeRange() {
        Collection<PollingStation> stationsInRange = election.getPollingStationsByZipCodeRange("1091AA", "1091ZZ");

        // Verify the size of the returned collection.
        assertEquals(2, stationsInRange.size(), "The number of polling stations within the range is not as expected");
    }

    @Test
    public void testFindMostRepresentativePollingStation() {
        PollingStation mostRepresentativeStation = election.findMostRepresentativePollingStation();

        // Verify the most representative polling station.
        assertNotNull(mostRepresentativeStation, "Most representative polling station should not be null");
        assertEquals("Stembureau Hogeschool van Amsterdam, Wibauthuis", mostRepresentativeStation.getName(), "Most representative polling station name is not as expected");
    }
}

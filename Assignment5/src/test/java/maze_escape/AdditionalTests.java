package maze_escape;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class AdditionalTests {

    Country nl, be, de, lux, fr, uk, ro, hu;
    Continent europe = new Continent();

    @BeforeAll
    static void beforeAll() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeEach
    void setUp() {
        nl = new Country("NL", 18);
        be = new Country("BE", 12);
        nl.addBorder(be, 100);
        de = new Country("DE", 83);
        de.addBorder(nl, 200);
        de.addBorder(be, 30);
        lux = new Country("LUX", 1);
        lux.addBorder(be, 60);
        lux.addBorder(de, 50);
        fr = new Country("FR", 67);
        fr.addBorder(lux, 30);
        fr.addBorder(be, 110);
        fr.addBorder(de, 50);
        uk = new Country("UK", 67);
        uk.addBorder(be, 70);
        uk.addBorder(fr, 150);
        uk.addBorder(nl, 250);

        ro = new Country("RO", 19);
        hu = new Country("HU", 10);
        ro.addBorder(hu, 250);
    }

    @Test
    void getAllVertices_ShouldThrowException_WhenStartVertexIsNull() {
        assertThrows(IllegalArgumentException.class, () -> europe.getAllVertices(null));
    }
    @Test
    void depthFirstSearch_ShouldFindPath_WhenPathExists() {
        AbstractGraph.GPath path = europe.depthFirstSearch(uk, lux);
        assertNotNull(path);
        assertSame(uk, path.getVertices().peek());
        assertSame(lux, ((LinkedList<Country>) path.getVertices()).peekLast());
        assertTrue(path.getVisited().size() >= path.getVertices().size());
    }


    @Test
    void depthFirstSearch_ShouldReturnNull_WhenPathDoesNotExist() {
        AbstractGraph.GPath path = europe.depthFirstSearch(uk, hu);
        assertNull(path);
    }

}

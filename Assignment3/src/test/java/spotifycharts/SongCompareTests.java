package spotifycharts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SongCompareTests {

    @Test
    public void testCompareByHighestStreamsCountTotal() {
        Song song = new Song("Leonard Cohen", "hallelujah", Song.Language.EN);
        Assertions.assertEquals(0, song.compareByHighestStreamsCountTotal(song));
    }

    @Test
    public void testCompareForDutchNationalChart() {
        Song song1 = new Song("Leonard Cohen", "hallelujah", Song.Language.EN);
        Song song2 = new Song("Leonard Cohen", "hallelujah", Song.Language.EN);
        Assertions.assertEquals(song1.compareForDutchNationalChart(song2), song2.compareForDutchNationalChart(song1));
    }
}

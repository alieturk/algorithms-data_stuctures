/**
 * This class is written to determine if already written bubble sort and quick sort
 * algorithms can properly sort an empty list and duplicated list.
 *
 * @author Huseyin Altunbas
 */
package spotifycharts;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SorterTest {
    private SorterImpl<Integer> sorter = new SorterImpl<>();
    private List<Integer> integerList = new ArrayList<>();
    private Comparator<Integer> comparator = Comparator.naturalOrder();
    private List<Integer> sortedList;


    @Test
    public void testEmptyListWithSelInsBubSort() {
        sortedList = sorter.selInsBubSort(integerList, comparator);

        assertEquals(0, sortedList.size());
    }

    @Test
    public void testDuplicatedListWithQuickSort() {
        integerList = Arrays.asList(1, 2, 1, 3, 3, 4, 100, 100, 101, 45, 34, 34, 2900, 2900, 2900, 2901, 2, 13, 13, 15);

        sortedList = sorter.quickSort(integerList, comparator);

        Integer[] expectedArray = {1, 1, 2, 2, 3, 3, 4, 13, 13, 15, 34, 34, 45, 100, 100, 101, 2900, 2900, 2900, 2901};

        assertArrayEquals(sortedList.toArray(new Integer[0]), expectedArray);

        assertEquals(20, sortedList.size());
    }


}

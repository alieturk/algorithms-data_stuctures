package spotifycharts;

import java.util.Comparator;
import java.util.List;

public class SorterImpl<E> implements Sorter<E> {

    /**
     * Sorts all items by selection or insertion sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     * @param items
     * @param comparator
     * @return  the items sorted in place
     */
    public List<E> selInsBubSort(List<E> items, Comparator<E> comparator) {
        int n = items.size();
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (comparator.compare(items.get(j - 1), items.get(j)) > 0) {
                    // swap elements
                    E temp = items.get(j - 1);
                    items.set(j - 1, items.get(j));
                    items.set(j, temp);
                }
            }
        }

        return items;
    }


    /**
     * Sorts all items by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     * @param items
     * @param comparator
     * @return  the items sorted in place
     */
    public List<E> quickSort(List<E> items, Comparator<E> comparator) {
        quickSortPart(items, 0, items.size()-1, comparator);
        return items;   // replace as you find appropriate
    }

    private void quickSortPart(List<E> items, int lowIndex, int highIndex, Comparator<E> comparator ) {
        if (lowIndex < highIndex){
            int partitionIndex = partition(items, lowIndex, highIndex, comparator);
            quickSortPart(items, lowIndex, partitionIndex - 1, comparator);
            quickSortPart(items, partitionIndex + 1, highIndex, comparator);
        }
    }

    private int partition(List<E> items, int low, int high, Comparator<E> comparator) {
        E pivot = items.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(items.get(j), pivot) < 0) {
                i++;
                swap(items, i, j);
            }
        }
        swap(items, i + 1, high);
        return i + 1;
    }

    private <T> void swap(List<T> list, int index1, int index2) {
        T temp = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, temp);
    }



    /**
     * Identifies the lead collection of numTops items according to the ordening criteria of comparator
     * and organizes and sorts this lead collection into the first numTops positions of the list
     * with use of (zero-based) heapSwim and heapSink operations.
     * The remaining items are kept in the tail of the list, in arbitrary order.
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     * @param numTops       the size of the lead collection of items to be found and sorted
     * @param items
     * @param comparator
     * @return              the items list with its first numTops items sorted according to comparator
     *                      all other items >= any item in the lead collection
     */
    public List<E> topsHeapSort(int numTops, List<E> items, Comparator<E> comparator) {

        // the lead collection of numTops items will be organised into a (zero-based) heap structure
        // in the first numTops list positions using the reverseComparator for the heap condition.
        // that way the root of the heap will contain the worst item of the lead collection
        // which can be compared easily against other candidates from the remainder of the list
        Comparator<E> reverseComparator = comparator.reversed();

        // initialise the lead collection with the first numTops items in the list
        for (int heapSize = 2; heapSize <= numTops; heapSize++) {
            // repair the heap condition of items[0..heapSize-2] to include new item items[heapSize-1]
            heapSwim(items, heapSize, reverseComparator);
        }

        // insert remaining items into the lead collection as appropriate
        for (int i = numTops; i < items.size(); i++) {
            // loop-invariant: items[0..numTops-1] represents the current lead collection in a heap data structure
            //  the root of the heap is the currently trailing item in the lead collection,
            //  which will lose its membership if a better item is found from position i onwards
            E item = items.get(i);
            E worstLeadItem = items.get(0);
            if (comparator.compare(item, worstLeadItem) < 0) {
                // item < worstLeadItem, so shall be included in the lead collection
                items.set(0, item);
                // demote worstLeadItem back to the tail collection, at the orginal position of item
                items.set(i, worstLeadItem);
                // repair the heap condition of the lead collection
                heapSink(items, numTops, reverseComparator);
            }
        }

        // the first numTops positions of the list now contain the lead collection
        // the reverseComparator heap condition applies to this lead collection
        // now use heapSort to realise full ordening of this collection
        for (int i = numTops-1; i > 0; i--) {
            // loop-invariant: items[i+1..numTops-1] contains the tail part of the sorted lead collection
            // position 0 holds the root item of a heap of size i+1 organised by reverseComparator
            // this root item is the worst item of the remaining front part of the lead collection

            // TODO swap item[0] and item[i];
            //  this moves item[0] to its designated position
                E temp = items.get(i);
                items.set(i, items.get(0));
                items.set(0, temp);

            // TODO the new root may have violated the heap condition
            //  repair the heap condition on the remaining heap of size i
                heapSink(items, i, reverseComparator);


        }

        return items;
    }

    /**
     * Repairs the zero-based heap condition for items[heapSize-1] on the basis of the comparator
     * all items[0..heapSize-2] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     *                      all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSwim(List<E> items, int heapSize, Comparator<E> comparator) {
        int childIndex = heapSize - 1;
        int parentIndex = (childIndex - 1) / 2;
        E swimmer = items.get(childIndex);

        while (childIndex > 0 && comparator.compare(swimmer, items.get(parentIndex)) < 0) {
            // Swap swimmer with parent
            items.set(childIndex, items.get(parentIndex));
            childIndex = parentIndex;
            parentIndex = (childIndex - 1) / 2;
        }
        // Place the swimmer in its correct position
        items.set(childIndex, swimmer);
    }
    /**
     * Repairs the zero-based heap condition for its root items[0] on the basis of the comparator
     * all items[1..heapSize-1] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     *                      all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSink(List<E> items, int heapSize, Comparator<E> comparator) {
        // TODO sink items[0] down the heap until
        //      2*i+1>=heapSize || (items[i] <= items[2*i+1] && items[i] <= items[2*i+2])
        int parentIndex = 0;
        int childIndex = 2 * parentIndex + 1;
        E sinker = items.get(parentIndex);

        while (childIndex < heapSize) {
            // Check if the right child exists and is smaller
            if (childIndex + 1 < heapSize && comparator.compare(items.get(childIndex + 1), items.get(childIndex)) < 0) {
                childIndex++;
            }

            // If the sinker is smaller than or equal to the smallest child, break the loop
            if (comparator.compare(sinker, items.get(childIndex)) <= 0) {
                break;
            }

            // Swap sinker with the smallest child
            items.set(parentIndex, items.get(childIndex));
            parentIndex = childIndex;
            childIndex = 2 * parentIndex + 1;
        }

        // Place the sinker in its correct position
        items.set(parentIndex, sinker);
    }

}

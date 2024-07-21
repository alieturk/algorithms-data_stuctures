package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class OrderedArrayList<E> extends ArrayList<E> implements OrderedList<E> {

    protected Comparator<? super E> sortOrder;   // the comparator that has been used with the latest sort
    protected int nSorted;                       // the number of sorted items in the first section of the list
    // representation-invariant
    //      all items at index positions 0 <= index < nSorted have been ordered by the given sortOrder comparator
    //      other items at index position nSorted <= index < size() can be in any order amongst themselves
    //              and also relative to the sorted section

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> sortOrder) {
        super();
        this.sortOrder = sortOrder;
        this.nSorted = 0;
    }

    public Comparator<? super E> getSortOrder() {
        return this.sortOrder;
    }

    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        this.sortOrder = c;
        this.nSorted = this.size();
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        nSorted = Integer.min(nSorted, index);
    }

    @Override
    public E remove(int index) {
        E element = super.remove(index);
        if (index < this.nSorted) {
            this.nSorted--;
        }
        return element;
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = super.remove(o);
        if (removed && this.nSorted > 0) {
            this.nSorted--;
        }
        return removed;
    }


    @Override
    public void sort() {
        if (this.nSorted < this.size()) {
            this.sort(this.sortOrder);
        }
    }

    @Override
    public int indexOf(Object item) {
        // efficient search can be done only if you have provided an sortOrder for the list
        if (this.getSortOrder() != null) {
            return indexOfByIterativeBinarySearch((E) item);
        } else {
            return super.indexOf(item);
        }
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem != null) {
            // some arbitrary choice to use the iterative or the recursive version
            return indexOfByRecursiveBinarySearch(searchItem);
        } else {
            return -1;
        }
    }

    /**
     * finds the position of the searchItem by an iterative binary search algorithm in the
     * sorted section of the arrayList, using the this.sortOrder comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        //implementing an iterative binary search on the sorted section of the arrayList, 0 <= index < nSorted
        //to find the position of an item that matches searchItem (this.sortOrder comparator yields a 0 result)
        int from = 0;
        int to = nSorted -1;
        while (from <= to) {
            int mid = (from + to)/2;
            E item = this.get(mid);

            if (this.sortOrder.compare(searchItem, item) > 0){
                from = mid + 1;
            } else if (this.sortOrder.compare(searchItem, item) < 0){
                to = mid -1;
            }else{
               return mid;
            }
        }
        //if no match was found, attempt a linear search of searchItem in the section nSorted <= index < size()
        for (int i = nSorted; i < size(); i++) {
            if (this.sortOrder.compare(searchItem, this.get(i)) == 0){
                return i;
            }
        }
        return -1;  // nothing was found ???

    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.sortOrder comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.sortOrder comparator, and that need not be in agreement with the .equals test.
     * Here we follow the comparator for sorting items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.sortOrder
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {
        // implement a recursive binary search on the sorted section of the arrayList, 0 <= index < nSorted
        //   to find the position of an item that matches searchItem (this.sortOrder comparator yields a 0 result)
        int result = recursiveBinarySearch(searchItem, 0, nSorted -1);
        if (result != -1){
            return result;
        }
        // if no match was found, attempt a linear search of searchItem in the section nSorted <= index < size()
        for (int i = nSorted; i < size(); i++) {
            if (this.sortOrder.compare(searchItem, this.get(i)) == 0){
                return i;
            }
        }
        return -1;
    }

    public int recursiveBinarySearch(E searchItem, int from, int to){
        if(from > to){return -1;}
        int mid = (from  + to) / 2;
        E item = this.get(mid);
        int cmp = this.sortOrder.compare(searchItem, item);
        if (cmp > 0){
            return recursiveBinarySearch(searchItem, mid + 1, to);
        }else if (cmp < 0){
            return recursiveBinarySearch(searchItem, from, mid - 1);
        }else {
            return mid;
        }
    }


    /**
     * finds a match of newItem in the list and applies the merger operator with the newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem
     * @param merger  a function that takes two items and returns an item that contains the merged content of
     *                the two items according to some merging rule.
     *                e.g. a merger could add the value of attribute X of the second item
     *                to attribute X of the first item and then return the first item
     * @return whether a new item was added to the list or not
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null) return false;
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem);

        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
            // Retrieve the matched item and
            // replace the matched item in the list with the merger of the matched item and the newItem
            E matchedItem = this.get(matchedItemIndex);
            E mergedItem = merger.apply(matchedItem, newItem);
            this.set(matchedItemIndex, mergedItem);

            return false;
        }
    }


    /**
     * calculates the total sum of contributions of all items in the list
     * @param mapper a function that calculates the contribution of a single item
     * @return the total sum of all contributions
     */
    @Override
    public double aggregate(Function<E, Double> mapper) {
        double sum = 0.0;
        int totalNumber = 0;
        // Loop over all items and use the mapper
        // to calculate and accumulate the contribution of each item
        for (E item : this) {
            sum += mapper.apply(item);
            totalNumber++;
        }
        System.out.println("Total number of items: " + totalNumber);
        return sum;
    }

}

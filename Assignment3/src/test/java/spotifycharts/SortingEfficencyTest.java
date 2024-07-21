package spotifycharts;

import org.junit.jupiter.api.Test;

import java.util.*;

public class SortingEfficencyTest {
    private SorterImpl<Song> sorter = new SorterImpl<>();
    private List<Song> songs;

    public void setup(int size) {
        ChartsCalculator chartsCalculator = new ChartsCalculator(0L);
        songs = chartsCalculator.registerStreamedSongs(size);
    }

    @Test
    public void measureBubbleSortingEfficiency() {
        int initialSize = 100;
        int maxSize = 1600;

        for (int size = initialSize; size <= maxSize; size *= 2) {
            System.gc();

            setup(size);

            long startTime, endTime, elapsedTime;
            // Algorithm 1
            startTime = System.nanoTime();
            sorter.selInsBubSort(songs, Song::compareForDutchNationalChart);
            endTime = System.nanoTime();
            elapsedTime = endTime - startTime;
            double elapsedTimeMs1 = (double) elapsedTime / 1_000_000;
            System.out.println("Algorithm 1: Size " + size + ", Time: " + elapsedTimeMs1 + " ms");
        }
    }

    @Test
    public void measureQuickSortingEfficiency() {
        int initialSize = 100;
        int maxSize = 100000;

        for (int size = initialSize; size <= maxSize; size *= 2) {
            System.gc();
            setup(size);

            long startTime, endTime, elapsedTime;

            // Algorithm 2
            startTime = System.nanoTime();
            sorter.quickSort(songs, Song::compareForDutchNationalChart);
            endTime = System.nanoTime();
            elapsedTime = endTime - startTime;
            double elapsedTimeMs2 = (double) elapsedTime / 1_000_000;
            System.out.println("Algorithm 2: Size " + size + ", Time: " + elapsedTimeMs2 + " ms");
        }
    }

    @Test
    public void measureHeapSortingEfficiency() {
        int initialSize = 100;
        int maxSize = 100000;

        for (int size = initialSize; size <= maxSize; size *= 2) {
            System.gc();
            setup(size);
            long startTime, endTime, elapsedTime;

            // Algorithm 3
            startTime = System.nanoTime();
            sorter.topsHeapSort(songs.size(), songs, Song::compareForDutchNationalChart);
            endTime = System.nanoTime();
            elapsedTime = endTime - startTime;
            double elapsedTimeMs3 = (double) elapsedTime / 1_000_000;
            System.out.println("Algorithm 3: Size " + size + ", Time: " + elapsedTimeMs3 + " ms");

        }
    }

//    @Test
//    public void printAll(){
//        print();
//    }
//
//    public void print(){
//        System.out.println("Algorithm 1");
//        for (int i = 100; i < 1000; i += 100) {
//            System.gc();
//            setup(i);
//            System.out.println("size " + i);
//            for (int j = 0; j < 50; j++) {
//                System.out.println(songs.get(j));
//            }
//        }
//        System.out.println("Algorithm 2");
//
//        for (int i = 100; i < 1000; i += 100) {
//            System.gc();
//            setup(i);
//            System.out.println("size " + i);
//            for (int j = 0; j < 50; j++) {
//                System.out.println(songs.get(j));
//            }
//        }
//        System.out.println("Algorithm 3");
//
//        for (int i = 100; i < 1000; i += 100) {
//            System.gc();
//            setup(i);
//            System.out.println("size " + i);
//            for (int j = 0; j < 50; j++) {
//                System.out.println(songs.get(j));
//            }
//        }

    }





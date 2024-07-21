package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;


public class TrafficTracker {
    private final String TRAFFIC_FILE_EXTENSION = ".txt";
    private final String TRAFFIC_FILE_PATTERN = ".+\\" + TRAFFIC_FILE_EXTENSION;

    private OrderedList<Car> cars;// the reference list of all known Cars registered by the RDW

    private OrderedList<Violation> violations;      // the accumulation of all offences by car and by city

    public TrafficTracker() {
        // initialize cars with an empty ordered list which sorts items by licensePlate.
        this.cars = new OrderedArrayList<>(Comparator.comparing(Car::getLicensePlate));
        // initalize violations with an empty ordered list which sorts items by car and city.
        this.violations = new OrderedArrayList<>(Violation::compareByLicensePlateAndCity);
    }

    /**
     * imports all registered cars from a resource file that has been provided by the RDW
     * @param resourceName
     */
    public void importCarsFromVault(String resourceName) {
        this.cars.clear();

        // load all cars from the text file
        int numberOfLines = importItemsFromFile(this.cars,
                createFileFromURL(Objects.requireNonNull(TrafficTracker.class.getResource(resourceName))),
                Car::fromLine);

        // sort the cars for efficient later retrieval
        this.cars.sort();

        System.out.printf("Imported %d cars from %d lines in %s.\n", this.cars.size(), numberOfLines, resourceName);
    }

    /**
     * imports and merges all raw detection data of all entry gates of all cities from the hierarchical file structure of the vault
     * accumulates any offences against purple rules into this.violations
     * @param resourceName
     */
    public void importDetectionsFromVault(String resourceName) {
        this.violations.clear();

        int totalNumberOfOffences =
            this.mergeDetectionsFromVaultRecursively(
                    createFileFromURL(Objects.requireNonNull(TrafficTracker.class.getResource(resourceName))));

        System.out.printf("Found %d offences among detections imported from files in %s.\n",
                totalNumberOfOffences, resourceName);
    }

    /**
     * traverses the detections vault recursively and processes every data file that it finds
     * @param file
     */
    private int mergeDetectionsFromVaultRecursively(File file) {
        int totalNumberOfOffences = 0;

        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory)
            // retrieve a list of all files and subfolders in this directory
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);

            // recursively process all files and subfolders from the filesInDirectory list
            // and track the total number of offences found
            for (File subFile : filesInDirectory) {
                totalNumberOfOffences += mergeDetectionsFromVaultRecursively(subFile);
            }

        } else if (file.getName().matches(TRAFFIC_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw detection files
            // process the content of this file and merge the offences found into this.violations
            totalNumberOfOffences += this.mergeDetectionsFromFile(file);
        }

        return totalNumberOfOffences;
    }

    /**
     * imports another batch detection data from the filePath text file
     * and merges the offences into the earlier imported and accumulated violations
     * @param file
     */
    private int mergeDetectionsFromFile(File file) {
        // re-sort the accumulated violations for efficient searching and merging
        this.violations.sort();

        // use a regular ArrayList to load the raw detection info from the file
        List<Detection> newDetections = new ArrayList<>();

        //  import all detections from the specified file into the newDetections list
        //  using the importItemsFromFile helper method and the Detection.fromLine parser.
        importItemsFromFile(newDetections, file, line -> Detection.fromLine(line, getCars()));

        System.out.printf("Imported %d detections from %s.\n", newDetections.size(), file.getPath());

        int totalNumberOfOffences = 0; // tracks the number of offences that emerges from the data in this file

        //  validate all detections against the purple criteria and
        //  merge any resulting offences into this.violations, accumulating offences per car and per city
        //  also keep track of the totalNumberOfOffences for reporting

        for (Detection detection: newDetections){
            Violation violation = detection.validatePurple();
                if (violation != null){
                    this.violations.merge(violation, Violation::combineOffencesCounts);
                    totalNumberOfOffences++;
                }
        }
        return totalNumberOfOffences;
    }

    /**
     * calculates the total revenue of fines from all violations,
     * Trucks pay €25 per offence, Coaches €35 per offence
     * @return the total amount of money recovered from all violations
     */
    public double calculateTotalFines() {
        return this.violations.aggregate((violation) -> {
            if (violation.getCar().getCarType() == Car.CarType.Truck)
                return 25d * violation.getOffencesCount();
            return 35d * violation.getOffencesCount();
        });
    }
    /**
     * Returns all the violations grouped/aggregated by car type in reversed order.
     * @return all violations grouped together by car type in descending order of offence count
     */
    public List<Violation> groupViolationsByCarType() {
        //Instantiate a new list that groups violations by car type
        OrderedArrayList<Violation> groupedViolations = new OrderedArrayList<>(Comparator.comparing(Violation::getCar));

        //Loop through the global violations list, aggregating items into the new array
        // based on car type
        for (Violation violation : violations) {
            groupedViolations.merge(violation, Violation::combineOffencesCounts);
        }

        //Sort the list in reverse order based on offence count and return it
        groupedViolations.sort(Comparator.comparing(Violation::getOffencesCount).reversed());
        return groupedViolations;
    }

    /**
     * Returns all the violations grouped/aggregated by car type in reversed order.
     * @return all violations grouped together by city type in descending order of offence count
     */
    public List<Violation> groupViolationsByCityType() {
        //Instantiate a new list that groups violations by car type
        OrderedArrayList<Violation> groupedViolations = new OrderedArrayList<>(Comparator.comparing(Violation::getCity));

        //Loop through the global violations list, aggregating items into the new array
        // based on city type
        for (Violation violation : violations) {
            groupedViolations.merge(violation, Violation::combineOffencesCounts);
        }

        //Sort the list in reverse order based on offence count and return it
        groupedViolations.sort(Comparator.comparing(Violation::getOffencesCount).reversed());
        return groupedViolations;
    }


    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by car across all cities.
     * @param topNumber     the requested top number of violations in the result list
     * @return              a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCar(int topNumber) {
        //First, we group the violations based on car type
        List<Violation> groupedViolations = groupViolationsByCarType();

        //Then, we return the top violations based on offence count
        return groupedViolations.subList(0, Math.min(topNumber, groupedViolations.size()));
    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by city across all cars.
     * @param topNumber     the requested top number of violations in the result list
     * @return              a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCity(int topNumber) {
        //First, we group the violations based on city type
        List<Violation> groupedViolations = groupViolationsByCityType();

        //Then, we return the top violations based on offence count
        return groupedViolations.subList(0, Math.min(topNumber, groupedViolations.size()));
    }


    /**
     * imports a collection of items from a text file which provides one line for each item
     * @param items         the list to which imported items shall be added
     * @param file          the source text file
     * @param converter     a function that can convert a text line into a new item instance
     * @param <E>           the (generic) type of each item
     */
    public static <E> int importItemsFromFile(List<E> items, File file, Function<String, E> converter) {
        int numberOfLines = 0;

        Scanner scanner = createFileScanner(file);

        // read all source lines from the scanner,
        // convert each line to an item of type E
        // and add each successfully converted item into the list
        while (scanner.hasNext()) {
            // input another line with author information
            String line = scanner.nextLine();
            numberOfLines++;
            //convert the line to an instance of E
            E item = converter.apply(line);
            //add a successfully converted item to the list of items
            items.add(item);
        }
        //System.out.printf("Imported %d lines from %s.\n", numberOfLines, file.getPath());
        return numberOfLines;
    }

    /**
     * helper method to create a scanner on a file and handle the exception
     * @param file
     * @return
     */
    private static Scanner createFileScanner(File file) {
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + file.getPath());
        }
    }
    private static File createFileFromURL(URL url) {
        try {
            return new File(url.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax error found on URL: " + url.getPath());
        }
    }

    public OrderedList<Car> getCars() {
        return this.cars;
    }

    public OrderedList<Violation> getViolations() {
        return this.violations;
    }
}

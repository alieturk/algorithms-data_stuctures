package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Holds all election data per consituency
 * Provides calculation methods for overall election results
 */
public class Election {

    private String name;

    // all (unique) parties in this election, organised by Id
    // will be build from the XML
    protected Map<Integer, Party> parties;

    // all (unique) constituencies in this election, identified by Id
    protected Set<Constituency> constituencies;

    public Election(String name) {
        this.name = name;

        //  initialise this.parties and this.constituencies with an appropriate Map implementations
        this.parties = new HashMap<>();
        this.constituencies = new HashSet<>();

    }

    /**
     * finds all (unique) parties registered for this election
     *
     * @return all parties participating in at least one constituency, without duplicates
     */
    public Collection<Party> getParties() {
        // return all parties that have been registered for the election
        return parties.values();
    }

    /**
     * finds the party with a given Id
     *
     * @param Id
     * @return the party with given Id, or null if no such party exists.
     */
    public Party getParty(int Id) {
        // find the party with the given Id
        return parties.getOrDefault(Id, null);
    }

    public Set<? extends Constituency> getConstituencies() {
        return this.constituencies;
    }

    /**
     * finds all unique candidates across all parties across all constituencies
     * organised by increasing party-id and then by increasing candidate id.
     *
     * @return alle unique candidates organised in an ordered set.
     */
    public List<Candidate> getAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();

        constituencies.stream()
                .flatMap(constituency -> constituency.getParties().stream())
                .flatMap(party -> party.getCandidates().stream())
                .forEach(candidate -> {
                    if (!candidates.contains(candidate)) {
                        candidates.add(candidate);
                    }
                });

        candidates.sort(Comparator.comparingInt(c -> c.getParty().getId()));
        return candidates;
    }

    /**
     * Retrieve for the given party the number of Candidates that have been registered per Constituency
     *
     * @param party
     * @return
     */
    public Map<Constituency, Integer> numberOfRegistrationsByConstituency(Party party) {
        // Create a map that maps each Constituency object to the number of candidate registrations
        // for the given party in that constituency
        return constituencies.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        constituency -> constituency.getCandidates(party).size(),
                        (prev, next) -> next, HashMap::new)
                );
    }

    /**
     * HINTS:
     * getCandidatesWithDuplicateNames:
     *  Approach-1: first build a Map that counts the number of candidates per given name
     *              then build the collection from all candidates, excluding those whose name occurs only once.
     *  Approach-2: build a stream that is sorted by name
     *              apply a mapMulti that drops unique names but keeps the duplicates
     *              this approach probably requires complex lambda expressions that are difficult to justify
     */

    /**
     * Finds all Candidates that have a duplicate name against another candidate in the election
     * (can be in the same party or in another party)
     *
     * @return
     */
    public Set<Candidate> getCandidatesWithDuplicateNames() {

        return getParties().stream()
                .flatMap(party -> party.getCandidates().stream())

                // Group the candidates by their full name
                .collect(groupingBy(Candidate::getFullName,
                        // Count the number of candidates with each full name
                        counting()))

                // Filter the names with more than one candidate
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)

                // Map the entries to the set of full names
                .map(Map.Entry::getKey)
                .flatMap(name -> getParties().stream()
                        .flatMap(party -> party.getCandidates().stream())
                        .filter(candidate -> candidate.getFullName().equals(name)))

                .collect(toSet());
    }

    /**
     * Retrieve from all constituencies the combined sub set of all polling stations that are located within the area of the specified zip codes
     * i.e. firstZipCode <= pollingStation.zipCode <= lastZipCode
     * All valid zip codes adhere to the pattern 'nnnnXX' with 1000 <= nnnn <= 9999 and 'AA' <= XX <= 'ZZ'
     *
     * @param firstZipCode
     * @param lastZipCode
     * @return the sub set of polling stations within the specified zipCode range
     */
    public Collection<PollingStation> getPollingStationsByZipCodeRange(String firstZipCode, String lastZipCode) {
        //retrieves all polling stations within the area of the given range of zip codes (inclusively)

        return constituencies.stream()
                // Use flatMap() to flatten the list of polling stations
                .flatMap(constituency -> constituency.getPollingStationsByZipCodeRange(firstZipCode, lastZipCode).stream())
                .collect(toSet());
    }

    /**
     * Retrieves per party the total number of votes across all candidates, constituencies and polling stations
     *
     * @return
     */
    public Map<Party, Integer> getVotesByParty() {
        //calculates the total number of votes per party
        return constituencies.stream()
                .flatMap(constituency -> constituency.getVotesByParty().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
    }

    /**
     * Retrieves per party the total number of votes across all candidates,
     * that were cast in one out of the given collection of polling stations.
     * This method is useful to prepare an election result for any sub-area of a Constituency.
     * Or to obtain statistics of special types of voting, e.g. by mail.
     *
     * @param pollingStations the polling stations that cover the sub-area of interest
     * @return
     */
    public Map<Party, Integer> getVotesByPartyAcrossPollingStations(Collection<PollingStation> pollingStations) {
        // calculate the total number of votes per party across the given polling stations
        return pollingStations.stream()
                .flatMap(station -> station.getVotesByParty().entrySet().stream())
                // Group the entries by party and sum the values
                .collect(groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));
    }


    /**
     * Transforms and sorts decreasingly vote counts by party into votes percentages by party
     * The party with the highest vote count shall be ranked upfront
     * The votes percentage by party is calculated from  100.0 * partyVotes / totalVotes;
     *
     * @return the sorted list of (party,votesPercentage) pairs with the highest percentage upfront
     */
    public static List<Map.Entry<Party, Double>> sortedElectionResultsByPartyPercentage(int tops, Map<Party, Integer> votesCounts) {
        //transforms the voteCounts input into a sorted list of entries holding votes percentage by party

        // Calculate the total number of votes
        double totalVotes = votesCounts.values().stream()
                .mapToDouble(votes -> votes)
                .sum();

        // Calculate the percentage of votes for each party
        Map<Party, Double> percentageVotes = votesCounts.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue()) / totalVotes * 100));

        // Sort the entries by percentage and number of votes and return the top N entries
        return percentageVotes.entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    int result = e2.getValue().compareTo(e1.getValue());
                    if (result == 0) {
                        result = e2.getValue().compareTo(e1.getValue());
                    }
                    return result;
                })
                .limit(tops)
                .collect(Collectors.toList());
    }

    /**
     * Find the most representative Polling Station, which has got its votes distribution across all parties
     * the most alike the distribution of overall total votes.
     * A perfect match is found, if for each party the percentage of votes won at the polling station
     * is identical to the percentage of votes won by the party overall in the election.
     * The most representative Polling Station has the smallest deviation from that perfect match.
     * <p>
     * There are different metrics possible to calculate a relative deviation between distributions.
     * You may use the helper method {@link #euclideanVotesDistributionDeviation(Map, Map)}
     * which calculates a relative least-squares deviation between two distributions.
     *
     * @return the most representative polling station.
     */
    public PollingStation findMostRepresentativePollingStation() {
        //calculate the overall total votes count distribution by Party
        //and finds the PollingStation with the lowest relative deviation between

        // Get the all votes by party
        Map<Party, Integer> allVotes = this.getVotesByParty();

        // get the polling station with the lowest differences
        return this.constituencies.stream()
                .map(Constituency::getPollingStations).flatMap(Collection::stream)
                .min(Comparator.comparingDouble(pollingStation -> euclideanVotesDistributionDeviation(allVotes, pollingStation.getVotesByParty())))
                .orElse(null);
    }

    /**
     * Calculates the Euclidian distance between the relative distribution across parties of two voteCounts.
     * If the two relative distributions across parties are identical, then the distance will be zero
     * If some parties have relatively more votes in one distribution than the other, the outcome will be positive.
     * The lower the outcome, the more alike are the relative distributions of the voteCounts.
     * ratign of votesCounts1 relative to votesCounts2.
     * see https://towardsdatascience.com/9-distance-measures-in-data-science-918109d069fa
     *
     * @param votesCounts1 one distribution of votes across parties.
     * @param votesCounts2 another distribution of votes across parties.
     * @return de relative distance between the two distributions.
     */
    private double euclideanVotesDistributionDeviation(Map<Party, Integer> votesCounts1, Map<Party, Integer> votesCounts2) {
        // calculate  number of votes
        int totalVotes01 = integersSum(votesCounts1.values());
        int totalVotes2 = integersSum(votesCounts2.values());

        return votesCounts1.entrySet().stream()
                .mapToDouble(object -> Math.pow(object.getValue() / (double) totalVotes01 -
                        votesCounts2.getOrDefault(object.getKey(), 0) / (double) totalVotes2, 2))
                .sum();

    }

    /**
     * auxiliary method to calculate the total sum of a collection of integers
     *
     * @param integers
     * @return
     */
    public static int integersSum(Collection<Integer> integers) {
        return integers.stream().reduce(0, Integer::sum);
    }


    public String prepareSummary(int partyId) {
        Party party = this.getParty(partyId);


        StringBuilder summary = new StringBuilder()
                .append("\nSummary of ").append(party).append(":\n")
                .append("Total number of candidates = %d\n".formatted(party.getCandidates().size()))
                .append(party.getCandidates()).append("\n")
                .append("Total number of registrations = %d\n".formatted(constituencies.stream().mapToLong(c -> c.getCandidates(party).size()).sum()))
                .append("Number of registrations per constituency: ")
                .append(constituencies.stream()
                        .collect(Collectors.toMap(
                                constituency -> constituency,
                                constituency -> constituency.getCandidates(party).size()
                        ))).append("\n");


        return summary.toString();
    }

    public String prepareSummary() {

        Map<Integer, Party> partiesSorted = parties.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        int totalNumberOfPollingStations = (int) constituencies.stream()
                .map(Constituency::getPollingStations).count();

        List<Candidate> allDuplicatePeople = getCandidatesWithDuplicateNames().stream().toList();

        List<Map.Entry<Party, Double>> partyPairs = sortedElectionResultsByPartyPercentage(parties.size(), getVotesByParty());

        List<Map.Entry<Party, Double>> partyPairsSorted = partyPairs.stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());

        List<Map.Entry<Party, Double>> sortedElectionsDesc = sortedElectionResultsByPartyPercentage(20, findMostRepresentativePollingStation().getVotesByParty());


        StringBuilder summary = new StringBuilder()
                .append("\nElection summary of %s\n\n%d Participating parties:\n%s\n".formatted(this.name, parties.size(), partiesSorted))

                .append("Total number of constituencies = %d\n".formatted(constituencies.size()))
                .append("Total number of polling stations = %d\n".formatted(totalNumberOfPollingStations))
                .append("Total number of candidates in the election = %d\n\n".formatted(getAllCandidates().size()))

                .append("Different candidates with duplicate names across different parties are:\n%s\n\n".formatted(allDuplicatePeople.toString()))
                .append("Overall election results by party percentage:\n%s\n\n".formatted(partyPairsSorted))

                .append("Polling stations in Amsterdam Wibautstraat area with zip codes 1091AA-1091ZZ:\n%s\n".formatted(getPollingStationsByZipCodeRange("1091AA", "1091ZZ")))

                .append("Top 10 election results by party percentage in Amsterdam area with zip codes 1091AA-1091ZZ:\n%s\n\n".formatted(sortedElectionResultsByPartyPercentage(10,
                        getVotesByPartyAcrossPollingStations(getPollingStationsByZipCodeRange("1091AA", "1091ZZ")))))

                .append("Most representative polling station is:\n%s\n".formatted(findMostRepresentativePollingStation()))

                .append(sortedElectionsDesc);


        final var navigableSetStream = constituencies.stream()
                .map(Constituency::getPollingStations)
                .flatMap(Collection::stream)
                .toList();
        navigableSetStream.forEach(System.out::println);


        return summary.toString();
    }

    /**
     * Reads all data of Parties, Candidates, Contingencies and PollingStations from available files in the given folder and its subfolders
     * This method can cope with any structure of sub folders, but does assume the file names to comply with the conventions
     * as found from downloading the files from https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021
     * So, you can merge folders after unpacking the zip distributions of the data, but do not change file names.
     *
     * @param folderName the root folder with the data files of the election results
     * @return een Election met alle daarbij behorende gegevens.
     * @throws XMLStreamException bij fouten in een van de XML bestanden.
     * @throws IOException        als er iets mis gaat bij het lezen van een van de bestanden.
     */
    public static Election importFromDataFolder(String folderName) throws XMLStreamException, IOException {
        System.out.println("Loading election data from " + folderName);
        Election election = new Election(folderName);
        int progress = 0;
        Map<Integer, Constituency> kieskringen = new HashMap<>();
        for (Path constituencyCandidatesFile : PathUtils.findFilesToScan(folderName, "Kandidatenlijsten_TK2021_")) {
            XMLParser parser = new XMLParser(new FileInputStream(constituencyCandidatesFile.toString()));
            Constituency constituency = Constituency.importFromXML(parser, election.parties);
            //election.constituenciesM.put(constituency.getId(), constituency);
            election.constituencies.add(constituency);
            showProgress(++progress);
        }
        System.out.println();
        progress = 0;
        for (Path votesPerPollingStationFile : PathUtils.findFilesToScan(folderName, "Telling_TK2021_gemeente_")) {
            XMLParser parser = new XMLParser(new FileInputStream(votesPerPollingStationFile.toString()));
            election.importVotesFromXml(parser);
            showProgress(++progress);
        }
        System.out.println();
        return election;
    }

    protected static void showProgress(final int progress) {
        System.out.print('.');
        if (progress % 50 == 0) System.out.println();
    }

    /**
     * Auxiliary method for parsing the data from the EML files
     * This methode can be used as-is and does not require your investigation or extension.
     */
    public void importVotesFromXml(XMLParser parser) throws XMLStreamException {
        if (parser.findBeginTag(Constituency.CONSTITUENCY)) {

            int constituencyId = 0;
            if (parser.findBeginTag(Constituency.CONSTITUENCY_IDENTIFIER)) {
                constituencyId = parser.getIntegerAttributeValue(null, Constituency.ID, 0);
                parser.findAndAcceptEndTag(Constituency.CONSTITUENCY_IDENTIFIER);
            }

            //Constituency constituency = this.constituenciesM.get(constituencyId);
            final int finalConstituencyId = constituencyId;
            Constituency constituency = this.constituencies.stream()
                    .filter(c -> c.getId() == finalConstituencyId)
                    .findFirst()
                    .orElse(null);

            //parser.findBeginTag(PollingStation.POLLING_STATION_VOTES);
            while (parser.findBeginTag(PollingStation.POLLING_STATION_VOTES)) {
                PollingStation pollingStation = PollingStation.importFromXml(parser, constituency, this.parties);
                if (pollingStation != null) constituency.add(pollingStation);
            }

            parser.findAndAcceptEndTag(Constituency.CONSTITUENCY);
        }
    }

    /**
     * HINTS:
     * getCandidatesWithDuplicateNames:
     *  Approach-1: first build a Map that counts the number of candidates per given name
     *              then build the collection from all candidates, excluding those whose name occurs only once.
     *  Approach-2: build a stream that is sorted by name
     *              apply a mapMulti that drops unique names but keeps the duplicates
     *              this approach probably requires complex lambda expressions that are difficult to justify
     */

}

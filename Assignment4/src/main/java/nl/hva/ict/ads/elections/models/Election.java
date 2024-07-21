package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        this.parties = new HashMap<>();
        // TODO initialise this.parties and this.constituencies with an appropriate Map implementations
        this.constituencies = new HashSet<>();



    }

    /**
     * finds all (unique) parties registered for this election
     * @return all parties participating in at least one constituency, without duplicates
     */
    public Collection<Party> getParties() {
        // TODO: return all parties that have been registered for the election
        //  hint: there is no need to build a new collection; just return what you have got...
        return parties.values(); // replace by a proper outcome
    }

    /**
     * finds the party with a given id
     * @param id
     * @return  the party with given id, or null if no such party exists.
     */
    public Party getParty(int id) {
        // TODO find the party with the given id
        return parties.getOrDefault(id,null); // replace by a proper outcome
    }

    public Set<? extends Constituency> getConstituencies() {
        return this.constituencies;
    }

    /**
     * finds all unique candidates across all parties across all constituencies
     * organised by increasing party-id
     * @return all unique candidates organised by increasing party-id
     */
    public List<Candidate> getAllCandidates() {
        // find all candidates organised by increasing party-id
        return parties.values().stream()
                .flatMap(party -> party.getCandidates().stream())
                .sorted(Comparator.comparing(candidate -> candidate.getParty().getId()))
                .collect(Collectors.toList()); // replace by a proper outcome
    }

    /**
     * Retrieve for the given party the number of Candidates that have been registered per Constituency
     * @param party
     * @return
     */
    public Map<Constituency, Integer> numberOfRegistrationsByConstituency(Party party) {
        Map<Constituency, Integer> registrationsByConstituency = new HashMap<>();

        for (Constituency constituency : constituencies) {
            int registrations = constituency.getCandidates(party).size();
            registrationsByConstituency.put(constituency, registrations);
        }

        return registrationsByConstituency;
    }


    /**
     * Finds all Candidates that have a duplicate name against another candidate in the election
     * (can be in the same party or in another party)
     * @return
     */
    public Set<Candidate> getCandidatesWithDuplicateNames() {
        Map<String, Integer> nameCountMap = new HashMap<>();

        // Count the number of candidates per name
        for (Party party : parties.values()) {
            for (Candidate candidate : party.getCandidates()) {
                String name = candidate.getFullName();
                nameCountMap.put(name, nameCountMap.getOrDefault(name, 0) + 1);
            }
        }

        // Filter the candidates with duplicate names
        Set<Candidate> candidatesWithDuplicateNames = new HashSet<>();
        for (Party party : parties.values()) {
            for (Candidate candidate : party.getCandidates()) {
                String name = candidate.getFullName();
                if (nameCountMap.get(name) > 1) {
                    candidatesWithDuplicateNames.add(candidate);
                }
            }
        }

        return candidatesWithDuplicateNames;
    }


    /**
     * Retrieve from all constituencies the combined sub set of all polling stations that are located within the area of the specified zip codes
     * i.e. firstZipCode <= pollingStation.zipCode <= lastZipCode
     * All valid zip codes adhere to the pattern 'nnnnXX' with 1000 <= nnnn <= 9999 and 'AA' <= XX <= 'ZZ'
     * @param firstZipCode
     * @param lastZipCode
     * @return      the sub set of polling stations within the specified zipCode range
     */
    public Collection<PollingStation> getPollingStationsByZipCodeRange(String firstZipCode, String lastZipCode) {
        // retrieve all polling stations within the area of the given range of zip codes (inclusively)
        Set<PollingStation> pollingStationsInRange = new HashSet<>();

        for (Constituency constituency : constituencies) {
            for (PollingStation pollingStation : constituency.getPollingStations()) {
                if (pollingStation.getZipCode().compareTo(firstZipCode) >= 0
                        && pollingStation.getZipCode().compareTo(lastZipCode) <= 0) {
                    pollingStationsInRange.add(pollingStation);
                }
            }
        }

        return pollingStationsInRange;
    }

    /**
     * Retrieves per party the total number of votes across all candidates, constituencies and polling stations
     * @return
     */
    public Map<Party, Integer> getVotesByParty() {
        return constituencies.stream()
                .flatMap(constituency -> constituency.getVotesByParty().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
    }

    /**
     * Retrieves per party the total number of votes across all candidates,
     * that were cast in one out of the given collection of polling stations.
     * This method is useful to prepare an election result for any sub-area of a Constituency.
     * Or to obtain statistics of special types of voting, e.g. by mail.
     * @param pollingStations the polling stations that cover the sub-area of interest
     * @return
     */
    public Map<Party, Integer> getVotesByPartyAcrossPollingStations(Collection<PollingStation> pollingStations) {
        //  calculate the total number of votes per party across the given polling stations
        if (pollingStations == null || pollingStations.isEmpty()) {
            return null;
        }

        // Calculate the total number of votes per party across the given polling stations
        return pollingStations.stream()
                .map(PollingStation::getVotesByParty)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));
    }


    /**
     * Transforms and sorts decreasingly vote counts by party into votes percentages by party
     * The party with the highest vote count shall be ranked upfront
     * The votes percentage by party is calculated from  100.0 * partyVotes / totalVotes;
     * @return  the sorted list of (party,votesPercentage) pairs with the highest percentage upfront
     */
    public static List<Map.Entry<Party,Double>> sortedElectionResultsByPartyPercentage(int tops, Map<Party, Integer> votesCounts) {
        // calculate total votes
        int totalVotes = votesCounts.values().stream().mapToInt(Integer::intValue).sum();

        // transform the voteCounts input into a sorted list of entries holding votes percentage by party
        return votesCounts.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), 100.0 * e.getValue() / totalVotes))
                .sorted(Map.Entry.<Party, Double>comparingByValue().reversed())
                .limit(tops)
                .collect(Collectors.toList());
    }

    /**
     * Find the most representative Polling Station, which has got its votes distribution across all parties
     * the most alike the distribution of overall total votes.
     * A perfect match is found, if for each party the percentage of votes won at the polling station
     * is identical to the percentage of votes won by the party overall in the election.
     * The most representative Polling Station has the smallest deviation from that perfect match.
     *
     * There are different metrics possible to calculate a relative deviation between distributions.
     * You may use the helper method {@link #euclidianVotesDistributionDeviation(Map, Map)}
     * which calculates a relative least-squares deviation between two distributions.
     *
     * @return the most representative polling station.
     */
    public PollingStation findMostRepresentativePollingStation() {
        //  calculate the overall total votes count distribution by Party
        //  and find the PollingStation with the lowest relative deviation between
        //  its votes count distribution and the overall distribution.
        Map<Party, Integer> overallVotes = constituencies.stream()
                .flatMap(constituency -> constituency.getVotesByParty().entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));

        // Find the PollingStation with the lowest relative deviation between
        // its votes count distribution and the overall distribution.
        return constituencies.stream()
                .flatMap(constituency -> constituency.getPollingStations().stream())
                .min(Comparator.comparing(pollingStation ->
                        euclidianVotesDistributionDeviation(pollingStation.getVotesByParty(), overallVotes)))
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
    private double euclidianVotesDistributionDeviation(Map<Party, Integer> votesCounts1, Map<Party, Integer> votesCounts2) {
        // calculate total number of votes in both distributions
        int totalNumberOfVotes1 = integersSum(votesCounts1.values());
        int totalNumberOfVotes2 = integersSum(votesCounts2.values());

        // we calculate the distance as the sum of squares of relative voteCount distribution differences per party
        // if we compare two voteCounts that have the same relative distribution across parties, the outcome will be zero
        return votesCounts1.entrySet().stream()
                .mapToDouble(e -> Math.pow(e.getValue()/(double)totalNumberOfVotes1 -
                        votesCounts2.getOrDefault(e.getKey(),0)/(double)totalNumberOfVotes2, 2))
                .sum();
    }

    /**
     * auxiliary method to calculate the total sum of a collection of integers
     * @param integers
     * @return
     */
    public static int integersSum(Collection<Integer> integers) {
        return integers.stream().reduce(Integer::sum).orElse(0);
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

        int totalNumberOfPollingStations = constituencies.stream()
                .mapToInt(con -> con.getPollingStations().size())
                .sum();

        List<Candidate> allDuplicatePeople = getCandidatesWithDuplicateNames().stream().toList();

        List<Map.Entry<Party, Double>> partyPairs = sortedElectionResultsByPartyPercentage(parties.size(), getVotesByParty());

        List<Map.Entry<Party, Double>> partyPairsSorted = partyPairs.stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .toList();

        List<Map.Entry<Party, Double>> sortedElectionsDesc = sortedElectionResultsByPartyPercentage(20, findMostRepresentativePollingStation().getVotesByParty());

        StringBuilder summary = new StringBuilder()
                .append(String.format("\nElection summary of %s\n\n%d Participating parties:\n%s\n", this.name, parties.size(), partiesSorted))
                .append(String.format("Total number of constituencies = %d\n", constituencies.size()))
                .append(String.format("Total number of polling stations = %d\n", totalNumberOfPollingStations))
                .append(String.format("Total number of candidates in the election = %,d\n\n", getAllCandidates().size()))
                .append(String.format("Different candidates with duplicate names across different parties are:\n%s\n\n", allDuplicatePeople))
                .append(String.format("Overall election results by party percentage:\n%s\n\n", partyPairsSorted))
                .append(String.format("Polling stations in Amsterdam Wibautstraat area with zip codes 1091AA-1091ZZ:\n%s\n", getPollingStationsByZipCodeRange("1091AA", "1091ZZ")))
                .append(String.format("Top 10 election results by party percentage in Amsterdam area with zip codes 1091AA-1091ZZ:\n%s\n\n", sortedElectionResultsByPartyPercentage(10, getVotesByPartyAcrossPollingStations(getPollingStationsByZipCodeRange("1091AA", "1091ZZ")))))
                .append(String.format("Most representative polling station is:\n%s\n", findMostRepresentativePollingStation()))
                .append(sortedElectionsDesc);

        return summary.toString();
    }


    /**
     * Reads all data of Parties, Candidates, Contingencies and PollingStations from available files in the given folder and its subfolders
     * This method can cope with any structure of sub folders, but does assume the file names to comply with the conventions
     * as found from downloading the files from https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021
     * So, you can merge folders after unpacking the zip distributions of the data, but do not change file names.
     * @param folderName    the root folder with the data files of the election results
     * @return een Election met alle daarbij behorende gegevens.
     * @throws XMLStreamException bij fouten in een van de XML bestanden.
     * @throws IOException als er iets mis gaat bij het lezen van een van de bestanden.
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
        for (Path votesPerPollingStationFile : PathUtils.findFilesToScan(folderName, "Telling_TK2021_gemeente")) {
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

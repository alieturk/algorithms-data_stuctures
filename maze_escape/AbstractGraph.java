package maze_escape;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class AbstractGraph<V> {

    /**
     * Graph representation:
     * this class implements graph search algorithms on a graph with abstract vertex type V
     * for every vertex in the graph, its neighbours can be found by use of abstract method getNeighbours(fromVertex)
     * this abstraction can be used for both directed and undirected graphs
     **/

    public AbstractGraph() {
    }

    /**
     * retrieves all neighbours of the given fromVertex
     * if the graph is directed, the implementation of this method shall follow the outgoing edges of fromVertex
     *
     * @param fromVertex
     * @return
     */
    public abstract Set<V> getNeighbours(V fromVertex);

    /**
     * retrieves all vertices that can be reached directly or indirectly from the given firstVertex
     * if the graph is directed, only outgoing edges shall be traversed
     * firstVertex shall be included in the result as well
     * if the graph is connected, all vertices shall be found
     *
     * @param firstVertex the start vertex for the retrieval
     * @return
     */
    public Set<V> getAllVertices(V firstVertex) {
        if (firstVertex == null) {
            throw new IllegalArgumentException("The starting vertex cannot be null.");
        }

        Deque<V> queue = new ArrayDeque<>(); // queue for the Breadth First Search algorithm
        Set<V> visited = new LinkedHashSet<>(); // to keep track of visited vertices

        queue.offer(firstVertex);
        visited.add(firstVertex);

        while (!queue.isEmpty()) {
            V currentVertex = queue.poll();
            // get the neighbours of the current vertex
            for (V neighbor : getNeighbours(currentVertex)) {
                // check if the neighbour is visited
                if (!visited.contains(neighbor)) {
                    // add the neighbour to the queue
                    queue.offer(neighbor);

                    visited.add(neighbor);
                }
            }
        }

        return visited; // return the set of all the vertices that can be reached from the starting vertex

    }


    /**
     * Formats the adjacency list of the subgraph starting at the given firstVertex
     * according to the format:
     * vertex1: [neighbour11,neighbour12,…]
     * vertex2: [neighbour21,neighbour22,…]
     * …
     * Uses a pre-order traversal of a spanning tree of the sub-graph starting with firstVertex as the root
     * if the graph is directed, only outgoing edges shall be traversed
     * , and using the getNeighbours() method to retrieve the roots of the child subtrees.
     *
     * @param firstVertex
     * @return
     */
    public String formatAdjacencyList(V firstVertex) {

        return String.format("Graph adjacency list:\n%s\n",
                getAllVertices(firstVertex).stream()
                        // Map vertex and all it's neighbors
                        .map(vertex -> String.format("%s: %s", vertex, this.getNeighbours(vertex).toString().replaceAll(" ", "")))
                        .collect(Collectors.joining("\n")));

    }


    /**
     * represents a directed path of connected vertices in the graph
     */
    public class GPath {
        private Deque<V> vertices = new LinkedList<>();
        private double totalWeight = 0.0;
        private Set<V> visited = new HashSet<>();

        /**
         * representation invariants:
         * 1. vertices contains a sequence of vertices that are neighbours in the graph,
         * i.e. FOR ALL i: 1 < i < vertices.length: getNeighbours(vertices[i-1]).contains(vertices[i])
         * 2. a path with one vertex equal start and target vertex
         * 3. a path without vertices is empty, does not have a start nor a target
         * totalWeight is a helper attribute to capture total path length from a function on two neighbouring vertices
         * visited is a helper set to be able to track visited vertices in searches, only for analysis purposes
         **/
        private static final int DISPLAY_CUT = 10;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    String.format("Weight=%.2f Length=%d visited=%d (",
                            this.totalWeight, this.vertices.size(), this.visited.size()));
            String separator = "";
            int count = 0;
            final int tailCut = this.vertices.size() - 1 - DISPLAY_CUT;
            for (V v : this.vertices) {
                // limit the length of the text representation for long paths.
                if (count < DISPLAY_CUT || count > tailCut) {
                    sb.append(separator + v.toString());
                    separator = ", ";
                } else if (count == DISPLAY_CUT) {
                    sb.append(separator + "...");
                }
                count++;
            }
            sb.append(")");
            return sb.toString();
        }

        /**
         * recalculates the total weight of the path from a given weightMapper that calculates the weight of
         * the path segment between two neighbouring vertices.
         *
         * @param weightMapper
         */
        public void reCalculateTotalWeight(BiFunction<V, V, Double> weightMapper) {
            this.totalWeight = 0.0;
            V previous = null;
            for (V v : this.vertices) {
                // the first vertex of the iterator has no predecessor and hence no weight contribution
                if (previous != null) this.totalWeight += weightMapper.apply(previous, v);
                previous = v;
            }
        }

        public Queue<V> getVertices() {
            return this.vertices;
        }

        public void setVertices(Deque<V> vertices) {
            this.vertices = vertices;
        }

        public double getTotalWeight() {
            return this.totalWeight;
        }

        public Set<V> getVisited() {
            return this.visited;
        }
    }

    /**
     * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath depthFirstSearch(V startVertex, V targetVertex) {
        // calculate the path from start to target by recursive depth-first-search

        GPath path = new GPath();
        boolean pathExists = depthFirstSearch(path, startVertex, targetVertex);

        return pathExists ? path : null;
    }

    /**
     * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param path
     * @param currentVertex
     * @param targetVertex
     * @return
     */
    private boolean depthFirstSearch(GPath path, V currentVertex, V targetVertex) {
        // Exit the method if the path is invalid or the currentVertex has already been visited
        if (path == null || path.getVisited().contains(currentVertex)) return false;

        // Mark the currentVertex as visited
        path.getVisited().add(currentVertex);

        // If the current and target vertex are the same, add it to the path and return true
        if (currentVertex.equals(targetVertex)) {
            path.getVertices().offer(currentVertex);
            return true;
        }

        boolean foundPath = false;

        // Recursively search the neighbors of the currentVertex
        for (V neighbour : this.getNeighbours(currentVertex)) {
            foundPath = depthFirstSearch(path, neighbour, targetVertex);

            // Add the currentVertex to the path if path has been found.
            if (foundPath) {

                //   path.vertices.addFirst(currentVertex);
                // Can also be done like this instead of the while loop but doesn't use the getter method
                //   path.vertices.addFirst(current);
                Deque<V> tempVertices = new LinkedList<>();
                tempVertices.offer(currentVertex);
                while (!path.getVertices().isEmpty()) {
                    tempVertices.offer(path.getVertices().poll());
                }
                path.setVertices(tempVertices);

                break;
            }
        }

        return foundPath;
    }


    /**
     * Uses a breadth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath breadthFirstSearch(V startVertex, V targetVertex) {
        if (startVertex == null || targetVertex == null) return null;

        GPath path = new GPath();

        path.getVertices().offer(targetVertex);
        path.getVisited().add(startVertex);

        // If the startVertex is the targetVertex it gets added to
        if (startVertex.equals(targetVertex)) {
            return path;
        }

        Queue<V> fifoQueue = new LinkedList<>();
        Map<V, V> visitedFrom = new HashMap<>();


        //Initialise the queue with the start vertex, Mark start vertex as visited, but without a preceding vertex on its path
        visitedFrom.put(startVertex, null);
        fifoQueue.offer(startVertex);

        V current = fifoQueue.poll();

        while (current != null) {
            // Shallow processing of all neighbour of the current vertex from the queue
            for (V neighbour : this.getNeighbours(current)) {

                path.getVisited().add(neighbour);

                if (neighbour.equals(targetVertex)) {
                    while (current != null) {

                        //  Can also be done like this instead of the while loop but doesn't use the getter method
                        //  path.vertices.addFirst(current);
                        Deque<V> tempVertices = new LinkedList<>();
                        tempVertices.offer(current);
                        while (!path.getVertices().isEmpty()) {
                            tempVertices.offer(path.getVertices().poll());
                        }
                        path.setVertices(tempVertices);

                        current = visitedFrom.get(current);
                    }

                    return path;

                } else if (!visitedFrom.containsKey(neighbour)) {

                    visitedFrom.put(neighbour, current);
                    fifoQueue.offer(neighbour);
                }

            }
            current = fifoQueue.poll();
        }
        return null;
    }

    // helper class to build the spanning tree of visited vertices in dijkstra's shortest path algorithm
    // your may change this class or delete it altogether follow a different approach in your implementation
    private class MSTNode implements Comparable<MSTNode> {
        protected V vertex;                // the graph vertex that is concerned with this MSTNode
        protected V parentVertex = null;     // the parent's node vertex that has an edge towards this node's vertex
        protected boolean isMarked = false;  // indicates DSP processing has been marked complete for this vertex
        protected double weightSumTo = Double.MAX_VALUE;   // sum of weights of current shortest path towards this node's vertex

        private MSTNode(V vertex) {
            this.vertex = vertex;
        }

        // comparable interface helps to find a node with the shortest current path, sofar
        @Override
        public int compareTo(MSTNode otherMSTNode) {
            return Double.compare(weightSumTo, otherMSTNode.weightSumTo);
        }
    }

    /**
     * Calculates the edge-weighted shortest path from the startVertex to targetVertex in the subgraph
     * according to Dijkstra's algorithm of a minimum spanning tree
     *
     * @param startVertex
     * @param targetVertex
     * @param weightMapper provides a function(v1,v2) by which the weight of an edge from v1 to v2
     *                     can be retrieved or calculated
     * @return the shortest path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath dijkstraShortestPath(V startVertex, V targetVertex, BiFunction<V, V, Double> weightMapper) {
        if (startVertex == null || targetVertex == null) return null;
        // check if startVertex or targetVertex is null, if true, return null

        GPath path = new GPath();
        Map<V, MSTNode> minimumSpanningTree = new HashMap<>();

        MSTNode startNode = new MSTNode(startVertex);
        startNode.weightSumTo = 0.0;
        minimumSpanningTree.put(startVertex, startNode);
        Stack<V> pathStack = new Stack<>();

        PriorityQueue<MSTNode> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.weightSumTo));
        queue.add(startNode);
        // initialize a priority queue to store the MSTNodes and sort them based on their weight

        while (!queue.isEmpty()) {
            MSTNode currentNode = queue.poll();
            currentNode.isMarked = true;

            if (currentNode.vertex.equals(targetVertex)) {
                V curr = targetVertex;
                while (curr != null) {
                    pathStack.push(curr);
                    path.visited.add(curr);
                    curr = minimumSpanningTree.get(curr).parentVertex;
                }
                while (!pathStack.isEmpty()) {
                    path.vertices.add(pathStack.pop());
                }
                //pop all elements from the pathStack and add them to the GPath object's vertices list
                return path;
            }

            getNeighbours(currentNode.vertex).forEach(neighbor -> {
                MSTNode neighborNode = minimumSpanningTree.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new MSTNode(neighbor);
                    minimumSpanningTree.put(neighbor, neighborNode);
                }
                // check if the neighbor is marked or not, if not, calculate the new distance and update
                if (!neighborNode.isMarked) {
                    double newDistance = currentNode.weightSumTo + weightMapper.apply(currentNode.vertex, neighbor);
                    if (newDistance < neighborNode.weightSumTo) {
                        neighborNode.weightSumTo = newDistance;
                        neighborNode.parentVertex = currentNode.vertex;
                        path.totalWeight = newDistance;
                        queue.add(neighborNode);
                    }
                }
            });
        }

        return null;
    }

}

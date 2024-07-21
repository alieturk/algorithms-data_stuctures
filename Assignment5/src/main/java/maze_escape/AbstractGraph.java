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
        StringBuilder stringBuilder = new StringBuilder();
        Set<V> visitedVertices = new LinkedHashSet<>();
        Queue<V> queue = new LinkedList<>();

        queue.offer(firstVertex);
        while (!queue.isEmpty()) {
            V currentVertex = queue.poll();

            if (!visitedVertices.contains(currentVertex)) {
                visitedVertices.add(currentVertex);

                // Convert neighbours set to string using the join() method
                Set<V> neighbours = getNeighbours(currentVertex);
                String neighboursString = neighbours.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));

                // Append formatted string to stringBuilder
                stringBuilder.append(currentVertex.toString())
                        .append(": [")
                        .append(neighboursString)
                        .append("]\n");

                // Add all unvisited neighbours to the queue
                for (V neighbour : neighbours) {
                    if (!visitedVertices.contains(neighbour)) {
                        queue.offer(neighbour);
                    }
                }
            }
        }
        return "Graph adjacency list:\n" + stringBuilder;
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
                    sb.append(separator).append(v.toString());
                    separator = ", ";
                } else if (count == DISPLAY_CUT) {
                    sb.append(separator).append("...");
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

        public double getTotalWeight() {
            return this.totalWeight;
        }

        public Set<V> getVisited() {
            return this.visited;
        }
        public void setVertices(Deque<V> vertices) {
            this.vertices = vertices;
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
        // Check if either the start or target vertex is null, if so return null
        if (startVertex == null || targetVertex == null) {
            return null;
        }

        GPath path = new GPath();

        // Add the start vertex to the path's vertices and visited list
        path.getVertices().add(startVertex);
        path.getVisited().add(startVertex);

        return dfsRecursion(targetVertex, path) ? path : null;
    }

    /**
     * This is a helper method for the depth first search, which is called recursively
     * @param targetVertex
     * @param path
     * @return
     */
    private boolean dfsRecursion(V targetVertex, GPath path) {
        // Get the last vertex added to the path, which will be the current vertex for this recursive call
        LinkedList<V> vertices = (LinkedList<V>) path.getVertices();
        V currentVertex = vertices.getLast();

        // If the current vertex is the target vertex, return true to indicate that the path is found
        if (currentVertex.equals(targetVertex)) {
            return true;
        }

        for (V neighbour : getNeighbours(currentVertex)) {
            // If the neighbour has not been visited before
            if (!path.getVisited().contains(neighbour)) {
                // Add the neighbour to the path's vertices and visited list
                vertices.add(neighbour);
                path.getVisited().add(neighbour);

                // Make a recursive call with the neighbour as the new current vertex
                if (dfsRecursion(targetVertex, path)) {
                    return true;
                }
                vertices.removeLast();
            }
        }
        // dead end
        return false;
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

        // If the start or target vertices are null, we cannot proceed.
        if (startVertex == null || targetVertex == null) {
            return null;
        }

        // Initializations:
        // - 'path' will store the path we discover.
        // - 'fifoQueue' will be our queue of vertices to visit.
        // - 'visitedFrom' records for each vertex, which vertex we visited it from.
        GPath path = new GPath();
        Queue<V> fifoQueue = new LinkedList<>();
        Map<V, V> visitedFrom = new HashMap<>();

        // Initialize our path and visitedFrom with the start vertex.
        path.getVertices().offer(targetVertex);
        path.getVisited().add(startVertex);
        visitedFrom.put(startVertex, null);

        // Edge case: if start and target vertices are same, return the path as it is.
        if (startVertex.equals(targetVertex)) {
            return path;
        }

        // Start BFS with the startVertex.
        fifoQueue.offer(startVertex);

        // Main loop: continue until there are no more vertices to visit.
        V current = fifoQueue.poll();
        while (current != null) {
            // For each neighbour of the current vertex...
            for (V neighbour : this.getNeighbours(current)) {
                // Add neighbour to the visited set.
                path.getVisited().add(neighbour);

                // If we found the target, reconstruct the path from start to target and return it.
                if (neighbour.equals(targetVertex)) {
                    // We rebuild the path in reverse: from target to start.
                    while (current != null) {
                        // Temporarily store the vertices in a Deque.
                        Deque<V> tempVertices = new LinkedList<>();
                        tempVertices.offer(current);
                        while (!path.getVertices().isEmpty()) {
                            tempVertices.offer(path.getVertices().poll());
                        }
                        path.setVertices(tempVertices);
                        current = visitedFrom.get(current);
                    }
                    return path;

                    // If the neighbour hasn't been visited yet, mark it as visited and enqueue it.
                } else if (!visitedFrom.containsKey(neighbour)) {
                    visitedFrom.put(neighbour, current);
                    fifoQueue.offer(neighbour);
                }
            }
            // Proceed to next vertex in the queue.
            current = fifoQueue.poll();
        }

        // If we reach here, there is no path from start to target.
        return null;
    }




    // helper class to build the spanning tree of visited vertices in dijkstra's shortest path algorithm
    // your may change this class or delete it altogether follow a different approach in your implementation
    private class MSTNode implements Comparable<MSTNode> {
        protected V vertex;                // the graph vertex that is concerned with this MSTNode
        protected V parentVertex = null;     // the parent's node vertex that has an edge towards this node's vertex
        protected boolean marked = false;  // indicates DSP processing has been marked complete for this vertex
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

        // Edge case: if either vertex is null, we cannot proceed.
        if (startVertex == null || targetVertex == null) {
            return null;
        }

        // Initializations:
        // - 'path' will store the shortest path we discover.
        // - 'minimumSpanningTree' stores each vertex and metadata about it as we explore the graph.
        // - 'pathStack' is a temporary storage for the path from the start vertex to the target vertex.
        // - 'queue' is our priority queue, which will give us the next vertex to explore (i.e., the one with smallest weightSumTo).
        GPath path = new GPath();
        Map<V, MSTNode> minimumSpanningTree = new HashMap<>();
        Stack<V> pathStack = new Stack<>();
        PriorityQueue<MSTNode> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.weightSumTo));

        // Set up the start vertex.
        MSTNode startNode = new MSTNode(startVertex);
        startNode.weightSumTo = 0.0;
        minimumSpanningTree.put(startVertex, startNode);
        queue.add(startNode);

        // Main loop: continue until there are no more vertices to explore.
        while (!queue.isEmpty()) {

            // Get the vertex with the smallest weightSumTo.
            MSTNode currentNode = queue.poll();

            // Mark the current node as processed.
            currentNode.marked = true;

            // If we have reached the target, reconstruct the path from start to target and return it.
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
                return path;
            }

            // For each neighbour of the current vertex, update the shortest path to that neighbour and add it to the queue.
            getNeighbours(currentNode.vertex).forEach(neighbor -> {
                MSTNode neighborNode = minimumSpanningTree.getOrDefault(neighbor, new MSTNode(neighbor));
                minimumSpanningTree.put(neighbor, neighborNode);

                // If the neighbour has not yet been marked, possibly update shortest path to it.
                if (!neighborNode.marked) {
                    double newDistance = currentNode.weightSumTo + weightMapper.apply(currentNode.vertex, neighbor);

                    // If this path to neighbour is shorter than previously known path, update it.
                    if (newDistance < neighborNode.weightSumTo) {
                        neighborNode.weightSumTo = newDistance;
                        neighborNode.parentVertex = currentNode.vertex;
                        path.totalWeight = newDistance;
                        queue.add(neighborNode);
                    }
                }
            });
        }

        // If we reach here, there is no path from start to target.
        return null;
    }

}








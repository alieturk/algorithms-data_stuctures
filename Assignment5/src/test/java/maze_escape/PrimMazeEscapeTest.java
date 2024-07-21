package maze_escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PrimMazeEscapeTest {

    private static final long SEED = 20221203L;
    private static final int WIDTH = 100;
    private static final int HEIGHT = WIDTH;
    private static final int REMOVE = 250;

    private Maze maze;
    private Set<Integer> vertices;

    @BeforeEach
    void setup() {
        Maze.reSeedRandomizer(SEED);
        maze = new Maze(WIDTH, HEIGHT);
        maze.generateRandomizedPrim();
        maze.configureInnerEntry();
        maze.removeRandomWalls(REMOVE);
        vertices = maze.getAllVertices(maze.getStartNode());
    }

    @Test
    public void testDepthFirstSearch() {
        // Perform the depth-first search
        Maze.GPath path = maze.depthFirstSearch(maze.getStartNode(), maze.getExitNode());

        // Assert that a path is found
        assertNotNull(path);
        // Assert that the path starts from the start vertex
        assertEquals(maze.getStartNode(), path.getVertices().peek());
        // Assert that the path ends at the target vertex
        assertEquals(maze.getExitNode(), ((Deque<Integer>) path.getVertices()).peekLast());
    }

    @Test
    public void testBreadthFirstSearch() {
        // Perform the breadth-first search
        Maze.GPath path = maze.breadthFirstSearch(maze.getStartNode(), maze.getExitNode());

        // Assert that a path is found
        assertNotNull(path);
        // Assert that the path starts from the start vertex
        assertEquals(maze.getStartNode(), path.getVertices().peek());
        // Assert that the path ends at the target vertex
        assertEquals(maze.getExitNode(), ((Deque<Integer>) path.getVertices()).peekLast());
    }

    @Test
    public void testDijkstraShortestPath() {
        // Perform Dijkstra's shortest path algorithm
        Maze.GPath path = maze.dijkstraShortestPath(maze.getStartNode(), maze.getExitNode(), maze::manhattanTime);

        // Assert that a path is found
        assertNotNull(path);
        // Assert that the path starts from the start vertex
        assertEquals(maze.getStartNode(), path.getVertices().peek());
        // Assert that the path ends at the target vertex
        assertEquals(maze.getExitNode(), ((Deque<Integer>) path.getVertices()).peekLast());
    }
}

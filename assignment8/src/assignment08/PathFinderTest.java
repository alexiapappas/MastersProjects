package assignment08;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class PathFinderTest {
    private static final String testDirectory = "testOutput/";

    @BeforeEach
    void setUp() throws IOException {
        // Create the output directory for file writing tests
        Files.createDirectories(Path.of(testDirectory));
    }

    @AfterEach
    void tearDown() throws IOException {
        // Delete the output directory and files after each test
        Files.walk(Path.of(testDirectory))
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testSolveBasicMaze() throws IOException {
        String input = "5 5\n" +
                "S   X\n" +
                " XXX \n" +
                "     \n" +
                " XXX \n" +
                "  G  \n";

        // Write the input maze to a temporary file
        Path inputFile = Files.createTempFile("inputMaze", ".txt");
        Files.write(inputFile, input.getBytes());

        // Output file where the result will be written
        Path outputFile = Path.of(testDirectory, "outputMaze.txt");

        // Run solveMaze
        PathFinder.solveMaze(inputFile.toString(), outputFile.toString());

        // Read the output file
        String output = Files.readString(outputFile);

        // Check if the output maze contains the expected path ('.')
        assertTrue(output.contains("."));
        assertTrue(output.contains("S"));
        assertTrue(output.contains("G"));
        assertTrue(output.contains(" "));
        assertTrue(output.contains("X"));

        // Cleanup
        Files.delete(inputFile);
    }

    @Test
    void testSolveNoPath() throws IOException {
        String input = "5 5\n" +
                "S   X\n" +
                " XXX \n" +
                "     \n" +
                " XXX \n" +
                "     \n";

        // Write the input maze to a temporary file
        Path inputFile = Files.createTempFile("inputMaze", ".txt");
        Files.write(inputFile, input.getBytes());

        // Output file where the result will be written
        Path outputFile = Path.of(testDirectory, "outputMaze.txt");

        // Run solveMaze
        PathFinder.solveMaze(inputFile.toString(), outputFile.toString());

        // Read the output file
        String output = Files.readString(outputFile);

        // Check if there's no path found (i.e., no dots in the output)
        assertFalse(output.contains("."));

        // Cleanup
        Files.delete(inputFile);
    }

    @Test
    void testSolveMultiplePaths() throws IOException {
        String input = "5 5\n" +
                "S   X\n" +
                " XXX \n" +
                "   X \n" +
                " XXX \n" +
                "  G  \n";

        // Write the input maze to a temporary file
        Path inputFile = Files.createTempFile("inputMaze", ".txt");
        Files.write(inputFile, input.getBytes());

        // Output file where the result will be written
        Path outputFile = Path.of(testDirectory, "outputMaze.txt");

        // Run solveMaze
        PathFinder.solveMaze(inputFile.toString(), outputFile.toString());

        // Read the output file
        String output = Files.readString(outputFile);

        // Check if the output contains the path (i.e., dots in the maze)
        assertTrue(output.contains("."));
        assertTrue(output.contains("S"));
        assertTrue(output.contains("G"));

        // Cleanup
        Files.delete(inputFile);
    }

    @Test
    void testAStarEmpty() {
        char[][] maze = {
                {'X', 'X', 'X', 'X', 'X'},
                {'X', ' ', ' ', ' ', 'X'},
                {'X', ' ', ' ', ' ', 'X'},
                {'X', ' ', ' ', ' ', 'X'},
                {'X', 'X', 'X', 'X', 'X'},
        };

        Node start = null;
        Node goal = null;

        assertThrows(IllegalArgumentException.class, () -> PathFinder.aStar(maze, start, goal));
    }

    @Test
    void testAStarNoPath() {
        char[][] maze = {
                {'S', ' ', 'X', ' ', ' '},
                {'X', 'X', 'X', ' ', ' '},
                {' ', ' ', ' ', ' ', 'X'},
                {' ', 'X', 'X', 'X', ' '},
                {'G', ' ', ' ', ' ', ' '},
        };

        Node start = new Node(0, 0, 0, 0, null);
        Node goal = new Node(4, 0, 0, 0, null);

        List<Node> path = PathFinder.aStar(maze, start, goal);

        // Assert that no path is found (null)
        assertNull(path);
    }

    @Test
    void testAStarBasicPath() {
        char[][] maze = {
                {'S', ' ', 'X', ' ', ' '},
                {'X', ' ', 'X', ' ', ' '},
                {'X', ' ', ' ', ' ', ' '},
                {'X', ' ', 'X', 'X', ' '},
                {'G', ' ', ' ', ' ', ' '},
        };

        Node start = new Node(0, 0, 0, 0, null);
        Node goal = new Node(4, 0, 0, 0, null);

        List<Node> path = PathFinder.aStar(maze, start, goal);

        // Assert that the path is found
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(path.getFirst().x_, 0);
        assertEquals(path.getFirst().y_, 0);
        assertEquals(path.getLast().x_, 4);
        assertEquals(path.getLast().y_, 0);
    }

    @Test
    void testAStarNoStartNode() {
        char[][] maze = {
                {' ', ' ', 'X', ' ', ' '},
                {'X', 'X', 'X', ' ', ' '},
                {' ', ' ', ' ', ' ', 'X'},
                {' ', 'X', 'X', 'X', ' '},
                {'G', ' ', ' ', ' ', ' '},
        };

        Node start = null;
        Node goal = new Node(4, 0, 0, 0, null);

        assertThrows(IllegalArgumentException.class, () -> PathFinder.aStar(maze, start, goal));
    }

    @Test
    void testAStarNoGoalNode() {
        char[][] maze = {
                {'S', ' ', 'X', ' ', ' '},
                {'X', 'X', 'X', ' ', ' '},
                {' ', ' ', ' ', ' ', ' '},
                {' ', 'X', 'X', 'X', ' '},
                {' ', ' ', ' ', ' ', ' '},
        };

        Node start = new Node(0, 0, 0, 0, null);
        Node goal = null;

        assertThrows(IllegalArgumentException.class, () -> PathFinder.aStar(maze, start, goal));
    }

    @Test
    void testInvalidInputFile() {
        String invalidFilePath = "invalidFilePath.txt";
        String outputFile = testDirectory + "outputMaze.txt";

        // Expect the method to handle the error gracefully
        assertDoesNotThrow(() -> PathFinder.solveMaze(invalidFilePath, outputFile));
    }

    @Test
    void testInvalidOutputFile() throws IOException {
        String input = "5 5\n" +
                "S   X\n" +
                " XXX \n" +
                "     \n" +
                " XXX \n" +
                "  G  \n";

        Path inputFile = Files.createTempFile("inputMaze", ".txt");
        Files.write(inputFile, input.getBytes());

        // Invalid output file path (non-writable)
        Path outputFile = Path.of("/non/existent/directory/outputMaze.txt");

        // Expect the method to handle the error gracefully
        assertDoesNotThrow(() -> PathFinder.solveMaze(inputFile.toString(), outputFile.toString()));

        Files.delete(inputFile);
    }
}
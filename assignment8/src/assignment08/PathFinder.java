package assignment08;

import java.io.*;
import java.util.*;

public class PathFinder {
    // Directions for up, down, left, right
    private static final int[] directionX = {-1, 1, 0, 0};
    private static final int[] directionY = {0, 0, -1, 1};

    /**
     * Solves a maze using the A* algorithm.
     *
     * @param inputFile The input file path that contains the maze layout. The file must start with the height
     *                 and width of the maze, followed by the maze itself.
     * @param outputFile The output file path where the maze with the found path will be written. The path will
     *                  be marked with '.'.
     * @throws IOException if there's an issue reading from the input file or writing to the output file.
     *
     * This method reads the maze from the input file, applies the A* algorithm to find the shortest path from
     * the start ('S') to the goal ('G'), marks the path with dots ('.'), and writes the updated maze to the output file.
     */
    public static void solveMaze(String inputFile, String outputFile) {
        try (Scanner reader = new Scanner(new File(inputFile))) {
            // Get maze dimensions
            int height = reader.nextInt();
            int width = reader.nextInt();
            reader.nextLine(); // move reader to following line

            // Read the maze layout from the file
            char[][] maze = new char[height][width];
            Node start = null, goal = null;
            for (int i = 0; i < height; i++) {
                String line = reader.nextLine();
                for (int j = 0; j < width; j++) {
                    maze[i][j] = line.charAt(j);
                    if (maze[i][j] == 'S') {
                        start = new Node(i, j, 0, 0, null); // Start node
                    } else if (maze[i][j] == 'G') {
                        goal = new Node(i, j, 0, 0, null); // Goal node
                    }
                }
            }

            // Perform A* to find the shortest path
            if (start != null && goal != null) {
                List<Node> path = aStar(maze, start, goal);
                if (path != null) {
                    // Mark the path with dots "."
                    for (Node node : path) {
                        if (maze[node.x_][node.y_] == ' ') {
                            maze[node.x_][node.y_] = '.';
                        }
                    }
                }
            }

            // Write the output to the file
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println(height + " " + width);
                for (int i = 0; i < height; i++) {
                    writer.println(new String(maze[i]));
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error writing to file: " + outputFile + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + inputFile + e.getMessage());
        }
    }

    /**
     * Finds the shortest path from the start node to the goal node using the A* algorithm.
     *
     * @param maze  A 2D array representing the maze where 'S' is the start, 'G' is the goal, and 'X' represents walls.
     * @param start The start node. The algorithm will begin searching from this node.
     * @param goal  The goal node. The algorithm will stop when this node is reached.
     * @return A list of nodes representing the path from start to goal. Returns null if no path exists.
     *
     * @throws IllegalArgumentException if either start or goal is null.
     *
     * The algorithm uses A* search, which combines the actual cost from the start node with a heuristic estimate
     * (Manhattan distance) to the goal node. It uses a priority queue to explore the least-cost nodes first and
     * reconstructs the path once the goal is reached.
     *
     * Special Cases:
     * - If either start or goal is null, the method will throw an IllegalArgumentException.
     * - If no path is found, the method returns null.
     */
    public static List<Node> aStar(char[][] maze, Node start, Node goal) {
        if (start == null || goal == null) {
            throw new IllegalArgumentException("Start/Goal cannot be null");
        }

        int height = maze.length;
        int width = maze[0].length;

        // Priority queue for A* search (min-heap based)
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getCost));
        Set<Node> visitedSet = new HashSet<>();

        // Add the start node to the open set
        openSet.offer(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // If the goal is reached, reconstruct the path
            if (current.x_ == goal.x_ && current.y_ == goal.y_) {
                List<Node> path = new ArrayList<>();
                while (current != null) {
                    path.add(current);
                    current = current.previous;
                }
                // Reverse the path in order to begin from the Start node
                Collections.reverse(path);
                return path;
            }

            visitedSet.add(current);

            // Explore adjacent cells (up, down, left, right)
            for (int i = 0; i < 4; i++) {
                int nextX = current.x_ + directionX[i];
                int nextY = current.y_ + directionY[i];

                // Check bounds for if it's an open space
                if (nextX >= 0 && nextX < height && nextY >= 0 && nextY < width && maze[nextX][nextY] != 'X') {
                    Node neighbor = new Node(nextX, nextY, current.getCost() + 1, 0, current); // generalCost + 1 for moving to the adjacent node

                    // Skip if the node is already visited
                    if (visitedSet.contains(neighbor)) continue;

                    // Heuristic: Manhattan distance from neighbor to goal
                    neighbor.estimatedCost_ = Math.abs(neighbor.x_ - goal.x_) + Math.abs(neighbor.y_ - goal.y_);

                    // If the neighbor is not in the open set or a better path is found
                    if (!openSet.contains(neighbor) || neighbor.getCost() < current.getCost()) {
                        openSet.offer(neighbor);
                    }
                }
            }
        }

        // If no path is found
        return null;
    }

    public static void main(String[] args) throws IOException {
        solveMaze("demoMaze.txt", "demoMazeOutput.txt");
    }
}

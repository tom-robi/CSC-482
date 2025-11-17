import java.util.*;

public class DFSmaze {

    private static final int[] dr = {-1, 1, 0, 0};  // change in row for N, S, W, E
    private static final int[] dc = {0, 0, -1, 1};  // change in column for N, S, W, E
    private static char[][] hintMaze = null;
    private static boolean hintPathComputed = false;
    private static int goalRow;
    private static int goalCol;

    public static void main (String [] args) {
        Random rand = new Random();

        Scanner s = new Scanner(System.in);

        while (true) {

            int[] size = getMazeSizeFromUser(s);
            int rows = size[0];
            int cols = size[1];

            char[][] maze = generateMaze(rows, cols, rand);

            playGame(maze, s);

            System.out.println("Would you like to play again? (Y, N)");
            char choice = Character.toLowerCase(s.next().charAt(0));
            if (choice == 'y') continue;
            else break;
        }
    }

    private static int[] getMazeSizeFromUser(Scanner s) {
        System.out.println("1) Small  (11 x 11)");
        System.out.println("2) Medium (21 x 21)");
        System.out.println("3) Large  (31 x 31)");
        System.out.println();
        System.out.print("Choose maze size: ");

        int choice = s.nextInt();

        // Return the selected size
        if (choice == 1) {
            return new int[] {11, 11};
        } else if (choice == 2) {
            return new int[] {21, 21};
        } else if (choice == 3) {
            return new int[] {31, 31};
        } else {
            // Bad input, ask again
            System.out.println("Invalid choice. Please try again.");
            return getMazeSizeFromUser(s);
        }
    }

    private static char[][] generateMaze(int rows, int cols, Random rand) {
        // create maze filled with walls (#), and carve out path using DFS
        //  helper function
        char[][] maze = new char[rows][cols];
        //
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                maze[i][j] = '#';
            }
        }
        dfsCarve(1, 1, maze, rand);

        // save exit row for convenience
        goalRow = rows - 2;
        goalCol = cols - 1;

        // open "entry" and "exit" cells
        maze[1][0] = '.';
        maze[goalRow][goalCol] = '.';

        return maze;
    }

    private static void dfsCarve(int r, int c, char[][] maze, Random rand) {
        // create start point
        maze[r][c] = '.';
        // directions representing N, S, E and W
        int[] directions = {0, 1, 2, 3};

        // Randomize cardinal directions using Fisher-Yates Algorithm
        // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
        for (int i = directions.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int t = directions[i];
            directions[i] = directions[j];
            directions[j] = t;
        }

        // choose cell 2 steps away in random direction 
        // if valid (within bounds and not visited), remove wall between old and
        //     new node, and repeat the process
        for (int i = directions.length - 1; i >= 0; i--) {
            int dir = directions[i];
            int newR = r + 2*dr[dir];
            int newC = c + 2*dc[dir];
            // check if it's in bounds
            if ((newR >=0) && (newR < maze.length) && (newC >= 0) && (newC < maze[0].length)) {
                // check if not visited
                if (maze[newR][newC] != '.') {
                    maze[r + dr[dir]][c + dc[dir]] = '.';
                    maze[newR][newC] = '.';
                    dfsCarve(newR, newC, maze, rand);
                }
            }
        }
    }

    private static boolean dfsHintPath(char[][] maze, int r, int c, 
                                   boolean[][] visited, 
                                   char[][] hintMaze) {
        // check if within bounds, is wall, or visited
        if (r < 0 || r >= maze.length || c < 0 || c >= maze[0].length) return false;
        if (maze[r][c] == '#') return false;
        if (visited[r][c]) return false;

        visited[r][c] = true;

        if (r == goalRow && c == goalCol) {
            hintMaze[r][c] = '+';
            return true;
        }

        for (int k = 0; k < 4; k++) {
            int nr = r + dr[k];
            int nc = c + dc[k];

            if (dfsHintPath(maze, nr, nc, visited, hintMaze)) {
                // on path to goal
                hintMaze[r][c] = '+';
                return true;
            }
        }

        // no path
        return false;
    } 

    private static void computeHintPath(char[][] maze) {
        if (hintPathComputed) return; // we are finished

        hintMaze = copyMaze(maze);
        boolean[][] visited = new boolean[maze.length][maze[0].length];

        int startRow = 1;
        int startCol = 0; // entry point

        boolean ok = dfsHintPath(maze, startRow, startCol, visited, hintMaze);
        if (!ok) {
            System.out.println("No path from start to goal.");
        } else {
            hintPathComputed = true;
        }
    }

    private static void printMaze(char[][] maze, int playerRow, int playerCol) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (i == playerRow && j == playerCol) {
                    System.out.print("@ ");
                } else {
                    System.out.print(maze[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    private static void printView(char[][] maze, int pr, int pc, boolean showHint) {
        for (int rOffset = -1; rOffset <= 1; rOffset++) {
            for (int cOffset = -1; cOffset <= 1; cOffset++) {
                int r = pr + rOffset;
                int c = pc + cOffset;

                boolean inBounds = (r >= 0 && r < maze.length &&
                c >= 0 && c < maze[0].length);

                if (inBounds && r == pr && c == pc) {
                    // Player in the center (or wherever their actual position is)
                    System.out.print("@ ");
                } else if (inBounds) {
                    // hint overlay if requested
                    if (showHint && hintMaze != null && hintMaze[r][c] == '+') {
                        System.out.print("+ ");
                    } else {
                        // Show the actual maze cell
                        System.out.print(maze[r][c] + " ");
                    }
                } else {
                    // Outside the maze: treat as solid wall
                    System.out.print("# ");
                }
            }
            System.out.println();
        }
    }

    private static void playGame(char[][] maze, Scanner s) {
        int playerRow= 1;
        int playerCol = 0;
        boolean showHint = false;

        clearScreen();

        // game loop
        while (true) {
            printView(maze, playerRow, playerCol, showHint);

            System.out.print("Move (W/A/S/D, H for hint, Q to quit): ");
            char move = Character.toLowerCase(s.next().charAt(0));
            
            clearScreen();

            if (move == 'q') {
                System.out.println("Quitting game. Goodbye!");
                break;
            }

            if (move == 'h') {
                // compute one, then toggle view
                if (!hintPathComputed) {
                    computeHintPath(maze);
                }
                showHint = !showHint; // can turn off if desired
                continue;
            }

            int dRow = 0, dCol = 0;
            if (move == 'w') dRow = -1;     // up
            else if (move == 's') dRow = 1; // down
            else if (move == 'a') dCol = -1; // left
            else if (move == 'd') dCol = 1; // right
            else {
                System.out.println("Invalid key. Use W/A/S/D.");
                continue;
            }

            int newRow = playerRow + dRow;
            int newCol = playerCol + dCol;

            // bounds check
            if (newRow < 0 || newRow >= maze.length ||
            newCol < 0 || newCol >= maze[0].length) {
                System.out.println("You can't walk off the map!");
                continue;
            }

            // wall check
            if (maze[newRow][newCol] == '#') {
                System.out.println("You ran into a wall.");
                continue;
            }

            // move is valid
            playerRow = newRow;
            playerCol = newCol;

            // check if victory condition
            if (playerRow == maze.length - 2 && playerCol == maze[0].length - 1) {
                printView(maze, playerRow, playerCol, showHint);
                System.out.println("You won!");
                break;
            }
        }
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static char[][] copyMaze(char[][] maze) {
        int rows = maze.length;
        int cols = maze[0].length;
        char[][] copy = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                copy[i][j] = maze[i][j];
            }
        }
        return copy;
    }

}


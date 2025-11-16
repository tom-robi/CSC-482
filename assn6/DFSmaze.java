import java.util.*;

public class DFSmaze {

    private static final int[] dr = {-1, 1, 0, 0};  // change in row for N, S, W, E
    private static final int[] dc = {0, 0, -1, 1};  // change in column for N, S, W, E

    
    public static void main (String [] args) {
        Random rand = new Random();

        Scanner s = new Scanner(System.in);
        int[] size = getMazeSizeFromUser(s);
        int rows = size[0];
        int cols = size[1];

        char[][] maze = generateMaze(rows, cols, rand);

        playGame(maze, s);
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

        // open "entry" and "exit" cells
        maze[1][0] = '.';
        maze[rows-2][cols-1] = '.';
        
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

    private static void playGame(char[][] maze, Scanner s) {
        int playerRow= 1;
        int playerCol = 0;

        // game loop
        while (true) {
            printMaze(maze, playerRow, playerCol);

            System.out.print("Move (W/A/S/D, Q to quit): ");
            char move = Character.toLowerCase(s.next().charAt(0));

            if (move == 'q') {
                System.out.println("Quitting game. Goodbye!");
                break;
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
                printMaze(maze, playerRow, playerCol);
                System.out.println("You won!");
                break;
            }
        }
    }

}

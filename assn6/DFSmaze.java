import java.util.*;

public class DFSmaze {

    private static final int[] dr = {-1, 1, 0, 0};  // change in row for N, S, W, E
    private static final int[] dc = {0, 0, -1, 1};  // change in column for N, S, W, E

    
    public static void main (String [] args) {
        int rows = 11;
        int cols = 21;

        Random rand = new Random();


        char[][] maze = generateMaze(rows, cols, rand);
        printMaze(maze);

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
    
    private static void printMaze(char[][] maze) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                System.out.print(maze[i][j] + " ");
            }
            System.out.println();
        }
    }

}

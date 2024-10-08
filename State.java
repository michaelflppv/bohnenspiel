import java.util.Arrays;
import java.util.Collection;

public class State {
    private final int[] board;  // The current state of the game board.
    private final int p1;       // Player 1's score.
    private final int p2;       // Player 2's score.
    private final boolean redPlayer; // If true, the current player is the red player.

    /**
     * Initializes the State object with default row and column counts.
     */
    public State() {
        this.board = new int[12];
        Arrays.fill(this.board, 6); // Each field starts with 6 beans
        this.p1 = 0;
        this.p2 = 0;
        this.redPlayer = true;  // Red player starts the game
    }

    public State(int[] board, int p1, int p2, boolean redPlayer) {
        this.board = board;
        this.p1 = p1;
        this.p2 = p2;
        this.redPlayer = redPlayer;
    }



    /**
     * Returns a binary array indicating valid moves for the current state.
     */
    private boolean[] getValidMoves() {
        boolean[] validMoves = new boolean[12];
        if (this.redPlayer) {
            for (int i = 0; i < 6; i++) {
                if (this.board[i] > 0) {
                    validMoves[i] = true;
                }
            }
        } else {
            for (int i = 6; i < this.board.length; i++) {
                if (this.board[i] > 0) {
                    validMoves[i] = true;
                }
            }
        }
        return validMoves;
    }

    public State[] expandState() {


    }



    /**
     * Return true if the red player wins, false if the blue player wins.
     */
    public boolean checkWin() {
        if (this.p1 > 36) {
            return true;
        }
        if (this.p2 > 36) {
            return false;
        }
        // Heuristic for the player with the most beans in their store
        return this.p1 >= this.p2;
    }


    // Additional getters for score (p1 and p2)
    public int getP1Score() {
        return this.p1;
    }

    public int getP2Score() {
        return this.p2;
    }
    /**
     * Returns the initial state of the game board.
     */
    public int[] getBoard() {
        return this.board;
    }

    public boolean getCurrentPlayer() {
        return this.redPlayer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player 1: ").append(this.p1).append("\n");
        for (int i = 0; i < 6; i++) {
            sb.append(this.board[i]).append(" ");
        }
        sb.append("\n");
        for (int i = 6;  i < this.board.length; i++) {
            sb.append(this.board[i]).append(" ");
        }
        sb.append("\n");
        sb.append("Player 2: ").append(this.p2).append("\n");
        return sb.toString();
    }

}


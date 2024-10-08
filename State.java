import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class State {
    private final int[] board;  // The current state of the game board.
    private final int scoreRed;       // Player 1's score.
    private final int scoreBlue;       // Player 2's score.
    private final boolean redPlayersTurn; // If true, the current player is the red player.

    /**
     * Initializes the State object with default row and column counts.
     */
    public State() {
        this.board = new int[12];
        Arrays.fill(this.board, 6); // Each field starts with 6 beans
        this.scoreRed = 0;
        this.scoreBlue = 0;
        this.redPlayersTurn = true;  // Red player starts the game
    }

    public State(int[] board, int p1, int p2, boolean redPlayer) {
        this.board = board;
        this.scoreRed = p1;
        this.scoreBlue = p2;
        this.redPlayersTurn = redPlayer;
    }

    /**
     * Returns a binary array indicating valid moves for the current state.
     */
    public List<Integer> getPossibleActions() {
        List<Integer> validMoves = new ArrayList<>();
        if (this.redPlayersTurn) {
            for (int i = 0; i < 6; i++) {
                if (this.board[i] > 0) {
                    validMoves.add(i);
                }
            }
        } else {
            for (int i = 6; i < this.board.length; i++) {
                if (this.board[i] > 0) {
                    validMoves.add(i);
                }
            }
        }
        return validMoves;
    }

    /**
     * Returns true if the current state is terminal, i.e., no more valid moves are possible.
     */
    public boolean isTerminal() {
        return getPossibleActions().isEmpty();
    }

    /**
     * Returns the next state of the game after applying the given action.
     * @param action The action to apply.
     * @throws IllegalArgumentException if the action is invalid for the current state.
     */
    public State applyAction(int action) throws IllegalArgumentException {
        if (action < 0 || action > 11 || (this.redPlayersTurn && action > 5) || (!this.redPlayersTurn && action < 6)){
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        int[] newBoard = this.board.clone();
        int newScoreRed = this.scoreRed;
        int newScoreBlue = this.scoreBlue;
        boolean newPlayer = !this.redPlayersTurn;

        int beansToPropagate = newBoard[action];
        newBoard[action] = 0;

        // Propagate the beans forward
        int i = action;
        while (beansToPropagate > 0) {
            i = (i + 1) % 12;
            newBoard[i]++;
            beansToPropagate--;
        }

        // Check if the last bean fills a hole up to 2, 4, or 6 beans
        while (newBoard[i] == 2 || newBoard[i] == 4 || newBoard[i] == 6) {
            if (this.redPlayersTurn) {
                newScoreRed += newBoard[i];
            } else {
                newScoreBlue += newBoard[i];
            }
            newBoard[i] = 0;
            i = (i == 0) ? 11 : i - 1;
        }

        return new State(newBoard, newScoreRed, newScoreBlue, newPlayer);
    }


    /**
     * Return true if the red player (presumably) wins, false if the blue player (presumably) wins.
     */
    public boolean getResult() {
        if (this.scoreRed > 36) {
            return true;
        }
        if (this.scoreBlue > 36) {
            return false;
        }
        // Heuristic for the player with the most beans in their store
        int redScore = this.scoreRed + Arrays.stream(this.board).limit(6).sum();
        int blueScore = this.scoreBlue + Arrays.stream(this.board).skip(6).sum();
        return redScore >= blueScore;
    }


    // Additional getters for score (p1 and p2)
    public int getP1Score() {
        return this.scoreRed;
    }

    public int getP2Score() {
        return this.scoreBlue;
    }
    /**
     * Returns the initial state of the game board.
     */
    public int[] getBoard() {
        return this.board;
    }

    public boolean getCurrentPlayer() {
        return this.redPlayersTurn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player 1: ").append(this.scoreRed).append("\n");
        for (int i = 0; i < 6; i++) {
            sb.append(this.board[i]).append(" ");
        }
        sb.append("\n");
        for (int i = 6;  i < this.board.length; i++) {
            sb.append(this.board[i]).append(" ");
        }
        sb.append("\n");
        sb.append("Player 2: ").append(this.scoreBlue).append("\n");
        return sb.toString();
    }

}


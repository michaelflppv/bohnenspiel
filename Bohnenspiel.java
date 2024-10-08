import java.util.Arrays;

public class Bohnenspiel {
    private int[] board;  // The current state of the game board.
    private int p1;       // Player 1's score.
    private int p2;       // Player 2's score.


    /**
     * Initializes the Bohnenspiel object with default row and column counts.
     */
    public Bohnenspiel() {
        this.board = new int[12];
        Arrays.fill(this.board, 6); // Each field starts with 6 beans
        this.p1 = 0;
        this.p2 = 0;
    }

    @Override
    public String toString() {
        return "Bohnenspiel";
    }

    /**
     * Returns the initial state of the game board.
     */
    public int[] getInitialState() {
        return this.board;
    }

    /**
     * Returns the next state of the game board after a player takes an action.
     */
    public int[] getNextState(int[] state, int action) {
        this.board = state;  // Update the current board with the given state.
        this.updateBoard(action);  // Update the board with the action.
        return this.board;
    }

    /**
     * Returns a binary array indicating valid moves for the current state.
     */
    public int[] getValidMoves(int[] state) {
        int[] validMoves = new int[state.length];
        for (int i = 0; i < state.length; i++) {
            validMoves[i] = state[i] > 0 ? 1 : 0;  // A move is valid if the field has beans.
        }
        return validMoves;
    }

    /**
     * Checks if the given action results in a win for the current player.
     */
    public boolean checkWin() {
        return this.p1 > 36 || this.p2 > 36;
    }

    /**
     * Returns the value of the given board state.
     */
    public int getValue(int[] board) {
        if (checkWin()) {
            return 1;
        }
        return 0;
    }

    /**
     * Returns the termination status of the game.
     */
    public boolean getTerminated(int[] board) {
        if (checkWin()) {
            return true;
        }
        return Arrays.stream(getValidMoves(board)).sum() == 0;
    }

    /**
     * Returns the opponent player.
     */
    public int getOpponent(int player) {
        return -player;
    }

    /**
     * Returns the value from the opponent's perspective.
     */
    public float getOpponentValue(float value) {
        return -value;
    }

    /**
     * Changes the perspective of the game state for the given player.
     */
    public int[] changePerspective(int[] state, int player) {
        int[] newState = new int[state.length];
        for (int i = 0; i < state.length; i++) {
            newState[i] = state[i] * player;  // Change perspective by multiplying by player.
        }
        return newState;
    }

    /**
     * Returns the encoded state of the game board.
     */
    public int[][] getEncodedState(int[] state) {
        int[][] encodedState = new int[1][state.length];
        System.arraycopy(state, 0, encodedState[0], 0, state.length);
        return encodedState;
    }

    /**
     * Updates the game board after a move.
     */
    public void updateBoard(int field) {
        int startField = field;
        int value = this.board[field];
        this.board[field] = 0;

        // Distribute the beans
        while (value > 0) {
            field = (field + 1) % 12;
            this.board[field]++;
            value--;
        }

        // Check for captures
        while (this.board[field] == 2 || this.board[field] == 4 || this.board[field] == 6) {
            if (startField < 6) {
                this.p1 += this.board[field];
            } else {
                this.p2 += this.board[field];
            }
            this.board[field] = 0;
            field = (field == 0) ? 11 : field - 1;
        }

    }
}

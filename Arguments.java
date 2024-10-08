/**
 * The Arguments class represents the hyperparameters for the MCTS algorithm.
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 02.04.2024
 */
public class Arguments {
    private final int numMCTSSearches; // number of MCTS searches
    private final double c; // exploration constant

    /**
     * Constructor for the Arguments class.
     */
    public Arguments() {
        // default values for the hyperparameters
        this.numMCTSSearches = 100;
        this.c = 2;

    }

    /**
     * Getter for the number of MCTS searches.
     *
     * @return number of MCTS searches
     */
    public int getNumMCTSSearches() {
        return numMCTSSearches;
    }

    /**
     * Getter for the exploration constant.
     *
     * @return exploration constant
     */
    public double getC() {
        return c;
    }
}

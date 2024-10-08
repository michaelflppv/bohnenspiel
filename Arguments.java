/**
 * The Arguments class represents the hyperparameters for the MCTS algorithm.
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 02.04.2024
 */
public class Arguments {
    public static final int numMCTSSearches = 2500; // number of MCTS searches
    public static final double C = Math.sqrt(2); // exploration constant
}

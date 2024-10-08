/**
 * The Arguments class represents the hyperparameters for the MCTS algorithm.
 *
 * @author mfilippo (Mikhail Filippov)
 * @version 02.04.2024
 */
public class Arguments {
    public static final int NUM_MCTS_SEARCHES = 2500; // number of MCTS searches
    public static final int MAX_SIMULATION_DEPTH = 50; // maximum depth of the simulation
    public static final double MAX_SIMULATION_TIME = 2500; // maximum time for the simulation in Milliseconds
    public static final double C = Math.sqrt(2); // exploration constant
}

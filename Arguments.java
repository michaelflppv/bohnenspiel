/**
 * The Arguments class represents the hyperparameters for the MCTS algorithm.
 */
public class Arguments {
    //public static final int NUM_MCTS_SEARCHES = 2900; // number of MCTS searches
    public static final int MAX_SIMULATION_DEPTH = 50; // maximum depth of the simulation
    public static final double MAX_SIMULATION_TIME = 2500; // maximum time for the simulation in Milliseconds
    public static final double C = Math.sqrt(2); // exploration constant
    public static final boolean USE_STARVATION = true; // use starvation mechanism
}

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * The MonteCarloTreeSearch class is responsible for performing the Monte Carlo Tree Search algorithm.
 * It uses a neural network model to predict the policy and value of a given state.
 * The MCTS algorithm is used to search the game tree and find the best move to play.
 */
public class MonteCarloTreeSearch {
    // Game on which the MCTS algorithm is applied
    private final State game;
    // Hyperparameters for the MCTS algorithm
    private final Arguments args;

    /**
     * Constructor for the MonteCarloTreeSearch class.
     *
     * @param game {@link State} the game on which the MCTS algorithm is applied
     * @param args   {@link Arguments} map for hyperparameters of MCTS
     */
    public MonteCarloTreeSearch(State game, Arguments args) {
        this.game = game;
        this.args = args;
    }

    /**
     * The parallelSearch method performs the Monte Carlo Tree Search algorithm in parallel using multiple threads.
     * It creates a pool of 4 threads and submits the search task to each thread.
     * The results of the search are then retrieved and returned as a 2D float array.
     *
     * @param board {@link int[]} the current state of the game
     * @return float[][] the probabilities of each action
     */
    public float[][] parallelSearch(int[] board) {
        ExecutorService executor = Executors.newFixedThreadPool(4); // create a pool of 4 threads
        List<Future<float[]>> futures = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Callable<float[]> callable = () -> search(board); // define the task
            Future<float[]> future = executor.submit(callable); // submit the task for execution
            futures.add(future);
        }

        float[][] results = new float[4][];
        for (int i = 0; i < 4; i++) {
            try {
                results[i] = futures.get(i).get(); // retrieve the result
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown(); // shut down the executor service

        return results;
    }

    /**
     * The search method performs the Monte Carlo Tree Search algorithm to find the best move to play.
     *
     * @param board {@link int[]} the current state of the game
     * @return float[] the probabilities of each action
     */
    public float[] search(int[] board) {
        // Create the root node of the search tree
        Node root = new Node(game, args, board, null, 0, 0, 1);

        // Initialize the valid moves for the current state
        int[] validMoves = game.getValidMoves(board);

        // Perform the MCTS algorithm for a given number of searches
        for (int search = 0; search < args.getNumMCTSSearches(); search++) {
            Node node = root;

            // Selection: Traverse the tree, selecting nodes with the highest UCB (Upper Confidence Bound)
            while (node.isExpanded() && !game.getTerminated(node.getBoard())) {
                node = node.select();
            }

            // Expansion: If the node is not terminal, we expand it
            if (!game.getTerminated(node.getBoard())) {
                validMoves = game.getValidMoves(node.getBoard());
                node.expand(validMoves);
            }

            // Simulation: Perform a random playout from the current node until the game terminates
            int[] simulatedBoard = node.getBoard().clone();

            while (!game.getTerminated(simulatedBoard)) {
                validMoves = game.getValidMoves(simulatedBoard);
                int randomMove = selectRandomMove(validMoves);
                simulatedBoard = game.getNextState(simulatedBoard, randomMove);
            }

            // Backpropagation: Once the game reaches a terminal state, backpropagate the result
            float value = game.getValue(simulatedBoard);
            value = game.getOpponentValue(value); // Adjust for the perspective of the current player
            node.backpropagate(value);
        }

        float[] actionProbs = new float[game.getActionSize()];
        for (Node child : root.getChildren()) {
            actionProbs[child.getMove()] = child.getVisitCount();
        }
        actionProbs = normalize(actionProbs);

        return actionProbs;
    }

    /**
     * The selectRandomMove method selects a random move from the given list of valid moves.
     *
     * @param validMoves {@link int[]} the list of valid moves
     * @return int the selected random move
     */
    private int selectRandomMove(int[] validMoves) {
        List<Integer> possibleMoves = new ArrayList<>();
        for (int i = 0; i < validMoves.length; i++) {
            if (validMoves[i] == 1) {
                possibleMoves.add(i);
            }
        }
        return possibleMoves.get((int) (Math.random() * possibleMoves.size()));
    }

    /**
     * The normalize method normalizes a given array.
     * Normalization means adjusting the values in the array so that the sum of all values is 1.
     *
     * @param a {@link float[]} the array to be normalized
     * @return float[] the normalized array
     */
    public float[] normalize(float[] a) {
        float sum = 0;
        for (float v : a) {
            sum += v;
        }
        for (int i = 0; i < a.length; i++) {
            a[i] /= sum;
        }
        return a;
    }
}

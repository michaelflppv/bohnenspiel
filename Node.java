import java.util.ArrayList;
import java.util.List;

/**
 * The Node class represents a node in the search tree of MCTS algorithm.
 */
public class Node {
    // AI Client
    private final State game;
    // Hyperparameters for the MCTS algorithm
    private final Arguments args;
    // Current state of the game
    private final int[] board;
    // Reference to the parent node
    private final Node parent;

    // Action taken to reach the node
    private final int move;
    // prior probability of the current node
    private final float prior;
    // number of times the node has been visited
    private int visitCount;
    // sum of the values of all nodes visited from the current node
    private double valueSum;

    // list of child nodes of the current node
    private final List<Node> children;

    /**
     * Constructor for the Node class.
     *
     * @param game    {@link State} the game on which the MCTS algorithm is applied
     * @param arguments map for hyperparameters of MCTS
     * @param board     {@link int[]} the current state of the game
     * @param parent    {@link Node} the parent node
     * @param move      {@link int} the action taken to reach the node
     * @param prior     {@link float} the prior probability of the current node
     * @param visitCount {@link int} the number of times the node has been visited
     */
    public Node(State game, Arguments arguments, int[] board, Node parent, int move, float prior, int visitCount) {
        this.game = game;
        this.args = arguments;
        this.board = board;
        this.parent = parent;

        this.move = move;
        this.prior = prior;
        this.visitCount = visitCount;
        this.valueSum = 0;

        this.children = new ArrayList<>();
    }

    /**
     * This method checks if the node is fully expanded, i.e., if all its children have been visited.
     *
     * @return true if the node is fully expanded, false otherwise
     */
    public boolean isExpanded() {
        return !this.children.isEmpty();
    }

    /**
     * This method selects the child node with the highest Upper Confidence Bound (UCB) value.
     *
     * @return the best child node
     */
    public Node select() {
        Node bestChild = null;
        double bestUcb = Double.NEGATIVE_INFINITY;

        for (Node child : this.children) {
            double ucb = this.getUcb(child);
            if (ucb > bestUcb) {
                bestChild = child;
                bestUcb = ucb;
            }
        }
        return bestChild;
    }

    /**
     * This method calculates the Upper Confidence Bound (UCB) value of a given child node.
     *
     * @param child {@link Node} the child node for which the UCB value is calculated
     * @return the UCB value of the child node
     */
    public double getUcb(Node child) {
        double qValue;
        if (child.visitCount == 0) {
            qValue = 0;
        } else {
            qValue = 1 - ((child.valueSum / child.visitCount) + 1) / 2;
        }
        double exploration = this.args.getC() * (Math.sqrt(this.visitCount) / (child.visitCount + 1)) * child.prior;
        return qValue + exploration;
    }

    /**
     * This method expands the current node by adding a new child node for each possible action.
     *
     * @param policy {@link double[]} the policy of the current node
     */
    public void expand(int[] policy) {
        // Get all the possible next states using the expandState method of the State class
        State[] nextStates = game.expandState();

        for (int i = 0; i < nextStates.length; i++) {
            // Get the next state and its corresponding board
            State nextState = nextStates[i];
            int[] childBoard = nextState.getBoard();

            // Create a new child node based on the action that generated this state
            Node child = new Node(nextState, args, childBoard, this, i, policy[i], 0);

            // Add the new child node to the list of children
            this.children.add(child);
        }

    }

    /**
     * This method back-propagates the value of the current node to all its ancestors.
     *
     * @param value {@link int} the value to be back-propagated
     */
    public void backpropagate(float value) {
        this.valueSum += value;
        this.visitCount++;

        if (this.parent != null) {
            // Reverse the value for the opponent (as we're switching players)
            float reversedValue = this.game.checkWin() ? value : -value;

            // Backpropagate to the parent
            this.parent.backpropagate(reversedValue);
        }
    }

    /**
     * This method returns the move that led to the current node.
     *
     * @return the move that led to the current node
     */
    public int getMove() {
        return this.move;
    }

    /**
     * This method returns the list of children of the current node.
     *
     * @return the list of children of the current node
     */
    public List<Node> getChildren() {
        return this.children;
    }

    /**
     * This method returns the number of times the current node has been visited.
     *
     * @return the number of times the current node has been visited
     */
    public int getVisitCount() {
        return this.visitCount;
    }

    /**
     * This method returns the state of the current node.
     *
     * @return the state of the current node
     */
    public int[] getBoard() {
        return this.board;
    }
}

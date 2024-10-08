import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Node class represents a node in the search tree of MCTS algorithm.
 */
public class Node {
    // Reference to the parent node
    private final Node parent;
    // Current state of the game
    private final State state;
    // number of times the node has been visited
    private int visitCount;
    // Number of wins for the red player from the current node
    private int sumWinsRed;
    // Number of wins for the blue player from the current node
    private int sumWinsBlue;
    // list of child nodes of the current node
    private List<Node> childNodes;
    private List<Integer> possibleActions;
    private int action; // the action that led to this node

    /**
     * Constructor for the root node.
     */
    public Node(State state) {
        this.parent = null;
        this.state = state;
        this.sumWinsRed = 0;
        this.sumWinsBlue = 0;
        this.childNodes = new ArrayList<>();
        this.action = -1;
    }

    /**
     * Constructor for a child node.
     */
    public Node(Node parentNode, State state, int action) {
        this.parent = parentNode;
        this.state = state;
        this.sumWinsRed = 0;
        this.sumWinsBlue = 0;
        this.childNodes = new ArrayList<>();
        this.action = action;
    }


    /**
     * This method checks if the node is fully expanded, i.e., if all its children have been visited.
     *
     * @return true if the node is fully expanded, false otherwise
     */
    public boolean isFullyExpanded() {
        return this.childNodes.size() == this.state.getPossibleActions().size();
    }

    public boolean isTerminal() {
        return state.isTerminal();
    }

    /**
     * This method selects the child node with the highest Upper Confidence Bound (UCB) value.
     *
     * @return the best child node
     */
    public Node select() {
        return this.childNodes.parallelStream().max((child1, child2) -> {
            double ucb1 = child1.calculateUCB();
            double ucb2 = child2.calculateUCB();
            return Double.compare(ucb1, ucb2);
        }).orElse(null);
    }

    /**
     * This method calculates the Upper Confidence Bound (UCB) value of a given child node.
     *
     * @return the UCB value of the child node
     */
    public double calculateUCB() {
        double winRate = parent.state.getCurrentPlayer() ? ((double) sumWinsRed) / visitCount : ((double) sumWinsBlue) / visitCount;
        double exploration = Math.sqrt(Math.log(parent.visitCount) / visitCount);
        return winRate + Arguments.C * exploration;
    }

    /**
     * This method expands the current node by adding a new child node for a random action that has not been explored yet.
     */
    public Node expand() {
        if (possibleActions == null) {
            possibleActions = state.getPossibleActions();
        }
        int action = possibleActions.remove(new Random().nextInt(possibleActions.size()));
        State nextState = state.applyAction(action);
        Node childNode = new Node(this, nextState, action);
        this.childNodes.add(childNode);
        return childNode;
    }


    /**
     * This method selects a random child node of the current node.
     * This Method is used in the simulation phase of the MCTS algorithm.
     * @return the randomly selected child node
     */
    public Node getRandomChild() {
        List<Integer> actions = state.getPossibleActions();
        int action = actions.get(new Random().nextInt(actions.size()));
        State nextState = state.applyAction(action);
        return new Node(this, nextState, action);
    }


    public int getAction() {
        return action;
    }

    public boolean getResult() {
        return state.getResult();
    }

    public void updateStats(boolean isRedWin) {
        if (isRedWin) {
            sumWinsRed++;
        } else {
            sumWinsBlue++;
        }
        visitCount++;
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildNodes() {
        return childNodes;
    }

    public int getVisitCount() {
        return visitCount;
    }

}

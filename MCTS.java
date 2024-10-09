import java.util.Comparator;

/**
 * The MCTS class contains the implementation of the Monte Carlo Tree Search algorithm.
 */
public class MCTS {

  /**
   * Returns the action of the child node with the highest visit count from a finished simulation root node.
   *
   * @param node The root node of the finished simulation.
   * @return The action of the child node with the highest visit count.
   */
  public static int getBestActionFromFinishedSimulationRootNode(Node node) {
    return node.getChildNodes().stream().max(Comparator.comparing(Node::getVisitCount)).get().getAction();
  }

  /**
   * Runs the MCTS algorithm for a specified number of iterations.
   *
   * @param root The root node to start the search from.
   * @param iterations The number of iterations to run the search for.
   * @return The root node after running the search.
   */
  public static Node runMCTS(Node root, int iterations) {
    double startTime = System.currentTimeMillis();
    while(System.currentTimeMillis() - startTime < Arguments.MAX_SIMULATION_TIME) {
      Node node = selection(root);
      node = expansion(node);
      boolean result = simulation(node);
      backpropagation(node, result);
    }
    return root;
  }

  /**
   * Selects a node using the UCT formula until a non-terminal or non-fully expanded node is found.
   *
   * @param node The node to start the selection from.
   * @return The selected node.
   */
  public static Node selection(Node node) {
    while(!node.isTerminal() && node.isFullyExpanded()) {
      node = node.select();
    }
    return node;
  }

  /**
   * Expands a node by adding a new child node for an untried action.
   *
   * @param node The node to expand.
   * @return The expanded node if the node is not terminal, otherwise the original node.
   */
  public static Node expansion(Node node) {
    if(!node.isTerminal()) {
      return node.expand();
    }
    return node;
  }

  /**
   * Simulates a random playout from a node and returns the result.
   *
   * @param node The node to start the simulation from.
   * @return The result of the simulation.
   */
  public static boolean simulation(Node node) {
    for(int i = 0; i < Arguments.MAX_SIMULATION_DEPTH && !node.isTerminal(); i++) {
      node = node.getRandomChild();
    }
    return node.getResult();
  }

  /**
   * Updates the statistics of all nodes in the path from a node to the root.
   *
   * @param node The node to start the backpropagation from.
   * @param result The result of the simulation to update the statistics with.
   */
  public static void backpropagation(Node node, boolean result) {
    while(node != null) {
      node.updateStats(result);
      node = node.getParent();
    }
  }

}

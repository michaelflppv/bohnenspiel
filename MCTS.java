import java.util.Comparator;

public class MCTS {

  public static int getBestActionFromFinishedSimulationRootNode(Node node) {
    return node.getChildNodes().stream().max(Comparator.comparing(Node::getVisitCount)).get().getAction();
  }

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

  public static Node selection(Node node) {
    while(!node.isTerminal() && node.isFullyExpanded()) {
      node = node.select();
    }
    return node;
  }

  public static Node expansion(Node node) {
    if(!node.isTerminal()) {
      return node.expand();
    }
    return node;
  }

  public static boolean simulation(Node node) {
    for(int i = 0; i < Arguments.MAX_SIMULATION_DEPTH && !node.isTerminal(); i++) {
      node = node.getRandomChild();
    }
    return node.getResult();
  }


  public static void backpropagation(Node node, boolean result) {
    while(node != null) {
      node.updateStats(result);
      node = node.getParent();
    }
  }

}

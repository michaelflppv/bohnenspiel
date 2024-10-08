import java.util.Comparator;

public class MCTS {

  public int getBestAction(Node root) {
    Node node = runMCTS(root, 1000);
    return node.getChildNodes().stream().max(Comparator.comparing(Node::getVisitCount)).get().getAction();
  }

  public Node runMCTS(Node root, int iterations) {
    for(int i = 0; i < iterations; i++) {
      //System.out.println("Iteration: " + i);
      Node node = selection(root);
      node = expansion(node);
      boolean result = simulation(node);
      backpropagation(node, result);
    }
    return root;
  }

  public Node selection(Node node) {
    while(!node.isTerminal() && node.isFullyExpanded()) {
      node = node.select();
    }
    return node;
  }

  public Node expansion(Node node) {
    if(!node.isTerminal()) {
      return node.expand();
    }
    return node;
  }

  public boolean simulation(Node node) {
    for(int i = 0; i < 10 && !node.isTerminal(); i++) {
      System.out.println("Simulation: " + i);
      node = node.getRandomChild();
    }
    return node.getResult();
  }


  public void backpropagation(Node node, boolean result) {
    while(node != null) {
      node.updateStats(result);
      node = node.getParent();
    }
  }

}

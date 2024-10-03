import numpy as np
import math


class Node:
    """
    Node class for Monte Carlo Tree Search (MCTS).

    Attributes:
        game (Game): The game object that provides game-specific logic.
        args (dict): A dictionary of arguments and hyperparameters for MCTS.
        state (np.ndarray): The current state of the game board.
        parent (Node): The parent node.
        action_taken (int): The action taken to reach this node.
        prior (float): The prior probability of selecting this node.
        children (list): The list of child nodes.
        visit_count (int): The number of times this node has been visited.
        value_sum (float): The sum of values backpropagated to this node.
    """
    def __init__(self, game, args, state, parent=None, action_taken=None, prior=0, visit_count=0):
        """
        Initializes the Node object with the given parameters.

        Args:
            game (Game): The game object.
            args (dict): A dictionary of arguments and hyperparameters.
            state (np.ndarray): The current state of the game board.
            parent (Node, optional): The parent node. Defaults to None.
            action_taken (int, optional): The action taken to reach this node. Defaults to None.
            prior (float, optional): The prior probability of selecting this node. Defaults to 0.
            visit_count (int, optional): The number of times this node has been visited. Defaults to 0.
        """
        self.game = game
        self.args = args
        self.state = state
        self.parent = parent
        self.action_taken = action_taken
        self.prior = prior
        self.children = []

        self.visit_count = visit_count
        self.value_sum = 0

    def is_expanded(self):
        """
        Checks if the node has been expanded (i.e., has child nodes).

        Returns:
            bool: True if the node has child nodes, False otherwise.
        """
        return len(self.children) > 0

    def select(self):
        """
        Selects the child node with the highest Upper Confidence Bound (UCB).

        Returns:
            Node: The child node with the highest UCB.
        """
        best_child = None
        best_ucb = -np.inf

        for child in self.children:
            ucb = self.get_ucb(child)
            if ucb > best_ucb:
                best_child = child
                best_ucb = ucb

        return best_child

    def get_ucb(self, child):
        """
        Calculates the Upper Confidence Bound (UCB) for a child node.

        Args:
            child (Node): The child node.

        Returns:
            float: The UCB value for the child node.
        """
        if child.visit_count == 0:
            q_value = 0
        else:
            q_value = 1 - ((child.value_sum / child.visit_count) + 1) / 2
        return q_value + self.args['C'] * (math.sqrt(self.visit_count) / (child.visit_count + 1)) * child.prior

    def expand(self, policy):
        """
        Expands the node by creating child nodes based on the given policy.
        """
        for action, prob in enumerate(policy):
            if prob > 0:
                child_state = self.state.copy()
                child_state = self.game.get_next_state(child_state, action, 1)
                child_state = self.game.change_perspective(child_state, player=-1)

                child = Node(self.game, self.args, child_state, self, action, prob)
                self.children.append(child)

    def backpropagate(self, value):
        """
        Backpropagates the value through the tree, updating visit counts and value sums.

        Args:
            value (float): The value to backpropagate.
        """
        self.value_sum += value
        self.visit_count += 1

        if self.parent is not None:
            value = self.game.get_opponent_value(value)
            self.parent.backpropagate(value)

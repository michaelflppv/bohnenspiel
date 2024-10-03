import numpy as np
import torch
from node import Node


class MCTS:
    """
    Monte Carlo Tree Search (MCTS) class for performing MCTS on a given game.

    Attributes:
        model (torch.nn.Module): The neural network model used for policy and value predictions.
        game (Game): The game object that provides game-specific logic.
        args (dict): A dictionary of arguments and hyperparameters for MCTS.
    """

    def __init__(self, model, game, args):
        """
        Initializes the MCTS object with the given model, game, and arguments.

        Args:
            model (torch.nn.Module): The neural network model.
            game (Game): The game object.
            args (dict): A dictionary of arguments and hyperparameters.
        """
        self.model = model
        self.game = game
        self.args = args

    @torch.no_grad()
    def search(self, state):
        """
        Performs MCTS starting from the given state.

        Args:
            state (np.ndarray): The initial state of the game.

        Returns:
            np.ndarray: The action probabilities after performing MCTS.
        """
        root = Node(self.game, self.args, state, visit_count=1)

        # Get the policy from the model and apply Dirichlet noise for exploration
        policy, _ = self.model(
            torch.tensor(self.game.get_encoded_state(state), device=self.model.device).unsqueeze(0)
        )
        policy = torch.softmax(policy, axis=1).squeeze(0).cpu().numpy()
        policy = (1 - self.args['dirichlet_epsilon']) * policy + self.args['dirichlet_epsilon'] \
                 * np.random.dirichlet([self.args['dirichlet_alpha']] * self.game.action_size)

        # Mask invalid moves and normalize the policy
        valid_moves = self.game.get_valid_moves(state)
        policy *= valid_moves
        policy /= np.sum(policy)
        root.expand(policy)

        # Perform the specified number of MCTS searches
        for search in range(self.args['num_mcts_searches']):
            node = root

            # Traverse the tree until an unexpanded node is found
            while node.is_expanded():
                node = node.select()

            # Get the value and terminal status of the node
            value, is_terminal = self.game.get_value_and_terminated(node.state, node.action_taken)
            value = self.game.get_opponent_value(value)

            if not is_terminal:
                # Expand the node using the model's policy and value predictions
                policy, value = self.model(
                    torch.tensor(self.game.get_encoded_state(node.state), device=self.model.device).unsqueeze(0)
                )
                policy = torch.softmax(policy, axis=1).squeeze(0).cpu().numpy()
                valid_moves = self.game.get_valid_moves(node.state)
                policy *= valid_moves
                policy /= np.sum(policy)

                value = value.item()

                node.expand(policy)

            # Backpropagate the value up the tree
            node.backpropagate(value)

        # Calculate the action probabilities based on visit counts
        action_probs = np.zeros(self.game.action_size)
        for child in root.children:
            action_probs[child.action_taken] = child.visit_count
        action_probs /= np.sum(action_probs)
        return action_probs

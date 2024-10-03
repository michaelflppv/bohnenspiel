import numpy as np
import random
import torch
import torch.nn.functional as F
from tqdm import trange
from mcts import MCTS


class Train:
    """
    Training class for the AlphaZero algorithm.

    Attributes:
        model (torch.nn.Module): The neural network model.
        optimizer (torch.optim.Optimizer): The optimizer for training the model.
        game (Game): The game object that provides game-specific logic.
        args (dict): A dictionary of arguments and hyperparameters for training.
        mcts (MCTS): The Monte Carlo Tree Search object.
    """

    def __init__(self, model, optimizer, game, args):
        """
        Initializes the Train object with the given model, optimizer, game, and arguments.

        Args:
            model (torch.nn.Module): The neural network model.
            optimizer (torch.optim.Optimizer): The optimizer for training the model.
            game (Game): The game object.
            args (dict): A dictionary of arguments and hyperparameters.
        """
        self.model = model
        self.optimizer = optimizer
        self.game = game
        self.args = args
        self.mcts = MCTS(model, game, args)

    def selfPlay(self):
        """
        Performs self-play to generate training data.

        Returns:
            list: A list of tuples containing the state, action probabilities, and outcome.
        """
        memory = []
        player = 1
        state = self.game.get_initial_state()

        while True:
            neutral_state = self.game.change_perspective(state, player)
            action_probs = self.mcts.search(neutral_state)

            memory.append((neutral_state, action_probs, player))

            temperature_action_probs = action_probs ** (1 / self.args['temperature'])
            temperature_action_probs /= np.sum(temperature_action_probs)
            action = np.random.choice(self.game.action_size, p=temperature_action_probs)

            state = self.game.get_next_state(state, action, player)

            value, is_terminal = self.game.get_value_and_terminated(state, action)

            if is_terminal:
                returnMemory = []
                for hist_neutral_state, hist_action_probs, hist_player in memory:
                    hist_outcome = value if hist_player == player else self.game.get_opponent_value(value)
                    returnMemory.append((
                        self.game.get_encoded_state(hist_neutral_state),
                        hist_action_probs,
                        hist_outcome
                    ))
                return returnMemory

            player = self.game.get_opponent(player)

    def train(self, memory):
        """
        Trains the model using the generated self-play data.

        Args:
            memory (list): A list of tuples containing the state, action probabilities, and outcome.
        """
        random.shuffle(memory)
        for batchIdx in range(0, len(memory), self.args['batch_size']):
            sample = memory[batchIdx:batchIdx + self.args['batch_size']]
            state, policy_targets, value_targets = zip(*sample)

            state, policy_targets, value_targets = np.array(state), np.array(policy_targets), np.array(
                value_targets).reshape(-1, 1)

            state = torch.tensor(state, dtype=torch.float32, device=self.model.device)
            policy_targets = torch.tensor(policy_targets, dtype=torch.float32, device=self.model.device)
            value_targets = torch.tensor(value_targets, dtype=torch.float32, device=self.model.device)

            out_policy, out_value = self.model(state)

            policy_loss = F.cross_entropy(out_policy, policy_targets)
            value_loss = F.mse_loss(out_value, value_targets)
            loss = policy_loss + value_loss

            self.optimizer.zero_grad()
            loss.backward()
            self.optimizer.step()

    def learn(self):
        """
        Executes the learning process, including self-play and training.

        Saves the model and optimizer states after training.
        """
        for iteration in range(self.args['num_iterations']):
            memory = []

            self.model.eval()
            for selfPlay_iteration in trange(self.args['num_selfPlay_iterations']):
                memory += self.selfPlay()

            self.model.train()
            for epoch in trange(self.args['num_epochs']):
                self.train(memory)

        torch.save(self.model.state_dict(), f"Models/model.pt")
        torch.save(self.optimizer.state_dict(), f"Models/optimizer.pt")

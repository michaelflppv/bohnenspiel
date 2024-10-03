import numpy as np


class Bohnenspiel:
    """
    Bohnenspiel game class that provides game-specific logic and state management.

    Attributes:
        row_count (int): The number of rows in the game board.
        column_count (int): The number of columns in the game board.
        action_size (int): The total number of possible actions.
    """

    def __init__(self):
        """
        Initializes the Bohnenspiel object with default row and column counts.
        """
        self.row_count = 3
        self.column_count = 3
        self.action_size = self.row_count * self.column_count

    def __repr__(self):
        """
        Returns a string representation of the Bohnenspiel object.

        Returns:
            str: The string representation of the game.
        """
        return "Bohnenspiel"

    def get_initial_state(self):
        """
        Returns the initial state of the game board.

        Returns:
            np.ndarray: A 2D array representing the initial state of the game board.
        """
        return np.zeros((self.row_count, self.column_count))

    def get_next_state(self, state, action, player):
        """
        Returns the next state of the game board after a player takes an action.

        Args:
            state (np.ndarray): The current state of the game board.
            action (int): The action taken by the player.
            player (int): The player taking the action.

        Returns:
            np.ndarray: The next state of the game board.
        """
        row = action // self.column_count
        column = action % self.column_count
        state[row, column] = player
        return state

    def get_valid_moves(self, state):
        """
        Returns a binary array indicating valid moves for the current state.

        Args:
            state (np.ndarray): The current state of the game board.

        Returns:
            np.ndarray: A binary array indicating valid moves.
        """
        if len(state.shape) == 3:
            return (state.reshape(-1, 9) == 0).astype(np.uint8)
        return (state.reshape(9) == 0).astype(np.uint8)

    def check_win(self, state, action):
        """
        Checks if the given action results in a win for the current player.

        Args:
            state (np.ndarray): The current state of the game board.
            action (int): The action taken by the player.

        Returns:
            bool: True if the action results in a win, False otherwise.
        """
        if action is None:
            return False

        row = action // self.column_count
        column = action % self.column_count
        player = state[row, column]

        return (
                np.sum(state[row, :]) == player * self.column_count
                or np.sum(state[:, column]) == player * self.row_count
                or np.sum(np.diag(state)) == player * self.row_count
                or np.sum(np.diag(np.flip(state, axis=0))) == player * self.row_count
        )

    def get_value_and_terminated(self, state, action):
        """
        Returns the value and termination status of the game after a given action.

        Args:
            state (np.ndarray): The current state of the game board.
            action (int): The action taken by the player.

        Returns:
            tuple: A tuple containing the value (1 for win, 0 for draw, -1 for loss) and a boolean indicating if the
            game is terminated.
        """
        if self.check_win(state, action):
            return 1, True
        if np.sum(self.get_valid_moves(state)) == 0:
            return 0, True
        return 0, False

    def get_opponent(self, player):
        """
        Returns the opponent player.

        Args:
            player (int): The current player.

        Returns:
            int: The opponent player.
        """
        return -player

    def get_opponent_value(self, value):
        """
        Returns the value from the opponent's perspective.

        Args:
            value (int): The current value.

        Returns:
            int: The value from the opponent's perspective.
        """
        return -value

    def change_perspective(self, state, player):
        """
        Changes the perspective of the game state for the given player.

        Args:
            state (np.ndarray): The current state of the game board.
            player (int): The player whose perspective to change to.

        Returns:
            np.ndarray: The game state from the given player's perspective.
        """
        return state * player

    def get_encoded_state(self, state):
        """
        Returns the encoded state of the game board.

        Args:
            state (np.ndarray): The current state of the game board.

        Returns:
            np.ndarray: The encoded state of the game board.
        """
        encoded_state = np.stack(
            (state == -1, state == 0, state == 1)
        ).astype(np.float32)

        if len(state.shape) == 3:
            encoded_state = np.swapaxes(encoded_state, 0, 1)

        return encoded_state

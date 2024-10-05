import torch
from torch.optim import Adam
import random
import numpy as np
from bohnenspiel import Bohnenspiel
from architecture_model import ResNet
from train import Train
import time
import requests

# Set random seeds for reproducibility
torch.manual_seed(0)
random.seed(0)
np.random.seed(0)

# Determine the device to run the model on (GPU if available, otherwise CPU)
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(device)

# Flag to indicate whether to load existing model and optimizer states
LOAD = False

# Server URL and AI name
server = "http://bohnenspiel.informatik.uni-mannheim.de"
name = "random-AI"

# Player scores
p1 = 0
p2 = 0


def main():
    """
    Main function to create a new game.
    """
    create_game()


def create_game():
    """
    Creates a new game on the server and starts the game loop.
    """
    url = f"{server}/api/creategame/{name}"
    game_id = load(url)
    print(f"Spiel erstellt. ID: {game_id}")

    url = f"{server}/api/check/{game_id}/{name}"
    while True:
        time.sleep(3)
        state = load(url)
        print(f". ({state})", end="")
        if state in ["0", "-1"]:
            break
        elif state == "-2":
            print("time out")
            return
    play(game_id, 0)


def open_games():
    """
    Fetches and prints the list of open games from the server.
    """
    url = f"{server}/api/opengames"
    opengames = load(url).split(";")
    for opengame in opengames:
        print(opengame)


def join_game(game_id):
    """
    Joins an existing game on the server.

    Args:
        game_id (str): The ID of the game to join.
    """
    url = f"{server}/api/joingame/{game_id}/{name}"
    state = load(url)
    print(f"Join-Game-State: {state}")
    if state == "1":
        play(game_id, 6)
    elif state == "0":
        print("error (join game)")


def play(game_id, offset):
    """
    Main game loop to play the game.

    Args:
        game_id (str): The ID of the game.
        offset (int): The offset to determine the player's side.
    """
    global p1, p2
    check_url = f"{server}/api/check/{game_id}/{name}"
    states_msg_url = f"{server}/api/statemsg/{game_id}"
    state_id_url = f"{server}/api/state/{game_id}"
    board = [6] * 12
    start, end = (7, 12) if offset == 0 else (1, 6)

    while True:
        time.sleep(1)
        move_state = int(load(check_url))
        state_id = int(load(state_id_url))
        if state_id != 2 and (start <= move_state <= end or move_state == -1):
            if move_state != -1:
                selected_field = move_state - 1
                board = update_board(board, selected_field)
                print(f"Gegner wählte: {move_state} /\t{p1} - {p2}")
                print(print_board(board) + "\n")
            select_field = random.choice([i for i in range(offset, offset + 6) if board[i] != 0])
            board = update_board(board, select_field)
            print(f"Wähle Feld: {select_field + 1} /\t{p1} - {p2}")
            print(print_board(board) + "\n\n")
            move(game_id, select_field + 1)
        elif move_state == -2 or state_id == 2:
            print("GAME Finished")
            check_url = f"{server}/api/statemsg/{game_id}"
            print(load(check_url))
            return
        else:
            print(f"- {move_state}\t\t{load(states_msg_url)}")


def update_board(board, field):
    """
    Updates the game board after a move.

    Args:
        board (list): The current state of the game board.
        field (int): The field selected for the move.

    Returns:
        list: The updated game board.
    """
    global p1, p2
    start_field = field
    value = board[field]
    board[field] = 0
    while value > 0:
        field = (field + 1) % 12
        board[field] += 1
        value -= 1

    while board[field] in [2, 4, 6]:
        if start_field < 6:
            p1 += board[field]
        else:
            p2 += board[field]
        board[field] = 0
        field = 11 if field == 0 else field - 1
    return board


def print_board(board):
    """
    Prints the current state of the game board.

    Args:
        board (list): The current state of the game board.

    Returns:
        str: The string representation of the game board.
    """
    s = "; ".join(map(str, board[11:5:-1])) + "\n" + "; ".join(map(str, board[:6]))
    return s


def move(game_id, field_id):
    """
    Makes a move on the server.

    Args:
        game_id (str): The ID of the game.
        field_id (int): The field ID to make the move.
    """
    url = f"{server}/api/move/{game_id}/{name}/{field_id}"
    print(load(url))


def load(url):
    """
    Loads data from the given URL.

    Args:
        url (str): The URL to load data from.

    Returns:
        str: The response text from the URL.
    """
    response = requests.get(url)
    return response.text


if __name__ == "__main__":
    # Define the training arguments and hyperparameters
    args = {
        'num_iterations': 8,  # number of highest level iterations
        'num_selfPlay_iterations': 500,  # number of self-play games to play within each iteration
        'num_parallel_games': 100,  # number of games to play in parallel
        'num_mcts_searches': 60,  # number of mcts simulations when selecting a move within self-play
        'num_epochs': 4,  # number of epochs for training on self-play data for each iteration
        'batch_size': 64,  # batch size for training
        'temperature': 1.25,  # temperature for the softmax selection of moves
        'C': 2,  # the value of the constant policy
        'augment': False,  # whether to augment the training data with flipped states
        'dirichlet_alpha': 0.3,  # the value of the dirichlet noise
        'dirichlet_epsilon': 0.125,  # the value of the dirichlet noise
    }

    # Initialize the game, model, and optimizer
    game = Bohnenspiel()
    model = ResNet(game, 4, 128, device)
    optimizer = Adam(model.parameters(), lr=0.001, weight_decay=0.0001)

    # Load existing model and optimizer states if LOAD is True
    if LOAD:
        model.load_state_dict(torch.load(f'Models/model.pt', map_location=device))
        optimizer.load_state_dict(torch.load(f'Models/optimizer.pt', map_location=device))

    # Initialize the training process and start learning
    training = Train(model, optimizer, game, args)
    training.learn()

    # Start the game
    main()

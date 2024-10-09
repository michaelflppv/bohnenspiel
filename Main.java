import java.net.URI;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;


/**
 * Main class for the game.
 */
public class Main {
    // static String server = "http://127.0.0.1:5000";
    static String server = "http://ole.informatik.uni-mannheim.de";
    static String name = "star lord";

    static int p1 = 0;
    static int p2 = 0;
    static boolean isRedSide = true;

    static Node lastBestActionNode;

    /**
     * Main method of the application.
     * @param args command line arguments
     * @throws Exception if any error occurs during the execution of the method.
     */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("What do you want to do?");
        System.out.println("1: Create a new game");
        System.out.println("2: See the list of open games");
        System.out.println("3: Join a game");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                createGame();
                break;
            case 2:
                openGames();
                break;
            case 3:
                System.out.println("Please paste the gameID:");
                String gameID = scanner.next();
                joinGame(gameID);
                break;
            default:
                System.out.println("Invalid choice");
                break;
        }
    }


    /**
     * This method is used to create a new game.
     * It first constructs the URL to create a game and then sends a request to the server.
     * The server responds with a game ID which is then printed to the console.
     *
     * After the game is created, it enters a loop where it checks the state of the game every 3 seconds.
     * If the state is "0" or "-1", it breaks the loop and proceeds to the play method.
     * If the state is "-2", it prints "time out" to the console and returns from the method.
     *
     * @throws Exception if any error occurs during the execution of the method.
     */
    static void createGame() throws Exception {
        isRedSide = true;
        String url = server + "/api/creategame/" + name;
        String gameID = load(url);
        System.out.println("Spiel erstellt. ID: " + gameID);

        url = server + "/api/check/" + gameID + "/" + name;
        while (true) {
            Thread.sleep(50);
            String state = load(url);
            System.out.print("." + " (" + state + ")");
            if (state.equals("0") || state.equals("-1")) {
                break;
            } else if (state.equals("-2")) {
                System.out.println("time out");
                return;
            }
        }

        // We are the first player since we start the game. We are therefore the red player.
        play(gameID, 0);
    }

    /**
     * This method is used to fetch and print the list of open games.
     * It constructs the URL to fetch open games and sends a request to the server.
     * The server responds with a list of open games which are then split into an array.
     * Each game in the array is then printed to the console.
     *
     * @throws Exception if any error occurs during the execution of the method.
     */
    static void openGames() throws Exception {
        String url = server + "/api/opengames";
        String[] opengames = load(url).split(";");
        for (int i = 0; i < opengames.length; i++) {
            System.out.println(opengames[i]);
        }
    }

    /**
     * This method is used to join a game.
     * It first constructs the URL to join a game using the provided game ID and then sends a request to the server.
     * The server responds with a state which is then printed to the console.
     *
     * If the state is "1", it proceeds to the play method with an offset of 6.
     * If the state is "0", it prints "error (join game)" to the console.
     *
     * @param gameID the ID of the game to join
     * @throws Exception if any error occurs during the execution of the method.
     */
    static void joinGame(String gameID) throws Exception {
        isRedSide = false;
        String url = server + "/api/joingame/" + gameID + "/" + name;
        String state = load(url);
        System.out.println("Join-Game-State: " + state);
        if (state.equals("1")) {
            play(gameID, 6);
        } else if (state.equals("0")) {
            System.out.println("error (join game)");
        }
    }

    /**
     * This method is used to play the game.
     * It first constructs the URLs for checking the game state, getting the state message, and getting the state ID.
     * It then initializes the game board and sets the start and end positions based on the provided offset.

     * The method enters a loop where it checks the game state every second.
     * If the state ID is not "2" and the move state is within the start and end positions or is "-1", it proceeds to make a move.
     * If the move state is "-2" or the state ID is "2", it prints "GAME Finished" to the console, fetches the state message, and returns from the method.
     *
     * @param gameID the ID of the game to play
     * @param offset the offset to use when calculating the start and end positions
     * @throws Exception if any error occurs during the execution of the method.
     */
    static void play(String gameID, int offset) throws Exception {
        String checkURL = server + "/api/check/" + gameID + "/" + name;
        String statesMsgURL = server + "/api/statemsg/" + gameID;
        String stateIdURL = server + "/api/state/" + gameID;
        int[] board = { 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 }; // position 1-12
        int start, end;
        if(offset == 0) {
            start = 7;
            end = 12;
        } else {
            start = 1;
            end = 6;
        }

        while(true) {
            Thread.sleep(50);
            int moveState = Integer.parseInt(load(checkURL));
            int stateID = Integer.parseInt(load(stateIdURL));
            if(stateID != 2 && ((start <= moveState && moveState <= end) || moveState == -1)) {
                if(moveState != -1) {
                    int selectedField = moveState - 1;
                    board = updateBoard(board, selectedField);
                    System.out.println("Gegner wählte: " + moveState + " /\t" + p1 + " - " + p2);
                    System.out.println(printBoard(board) + "\n");
                }

                // Calculate the next move using MCTS
                // The parts of the tree that can be reused are stored in lastBestActionNode
                if (lastBestActionNode == null) {
                    lastBestActionNode = new Node(new State(board, p1, p2, isRedSide));
                } else {
                    int[] finalBoard = board;
                    lastBestActionNode.getChildNodes().stream().filter(node -> node.getAction() == moveState - 1).findFirst().ifPresentOrElse(node -> {
                        lastBestActionNode = node;
                    }, () -> {
                        lastBestActionNode = new Node(new State(finalBoard, p1, p2, isRedSide));
                    });
                }

                // Returns the root node of the finished mcts simulation
                Node nextMove = MCTS.runMCTS(lastBestActionNode);
                int selectField = MCTS.getBestActionFromFinishedSimulationRootNode(lastBestActionNode);

                board = updateBoard(board, selectField);
                System.out.println("Wähle Feld: " + (selectField + 1) + " /\t" + p1 + " - " + p2);
                System.out.println(printBoard(board) + "\n\n");

                move(gameID, selectField + 1);
            } else if(moveState == -2 || stateID == 2) {
                System.out.println("GAME Finished");
                checkURL = server + "/api/statemsg/" + gameID;
                System.out.println(load(checkURL));
                return;
            } else {
                System.out.println("- " + moveState + "\t\t" + load(statesMsgURL));
            }

        }
    }

    /**
     * This method is used to update the game board.
     * It first gets the value at the provided field and sets the field to 0.
     * It then distributes the value across the board in a clockwise direction.

     * If the final field has 2, 4, or 6 beans, it captures the beans and adds them to the player's score.
     * It continues capturing beans from the previous fields as long as they have 2, 4, or 6 beans.
     *
     * @param board the current game board
     * @param field the field to update
     * @return the updated game board
     */
    static int[] updateBoard(int[] board, int field) {
        int startField = field;

        int value = board[field];
        board[field] = 0;
        while (value > 0) {
            field = (++field) % 12;
            board[field]++;
            value--;
        }

        if (board[field] == 2 || board[field] == 4 || board[field] == 6) {
            do {
                if (startField < 6) {
                    p1 += board[field];
                } else {
                    p2 += board[field];
                }
                board[field] = 0;
                field = (field == 0) ? field = 11 : --field;
            } while (board[field] == 2 || board[field] == 4 || board[field] == 6);
        }
        return board;
    }

    /**
     * This method is used to print the current state of the game board.
     * It first constructs a string representation of the top half of the board (positions 7 to 12) in reverse order.
     * It then adds a newline character to the string and appends a string representation of the bottom half of the board (positions 1 to 6).
     *
     * @param board the current game board
     * @return a string representation of the game board
     */
    static String printBoard(int[] board) {
        String s = "";
        for (int i = 11; i >= 6; i--) {
            if (i != 6) {
                s += board[i] + "; ";
            } else {
                s += board[i];
            }
        }

        s += "\n";
        for (int i = 0; i <= 5; i++) {
            if (i != 5) {
                s += board[i] + "; ";
            } else {
                s += board[i];
            }
        }

        return s;
    }

    /**
     * This method is used to make a move in the game.
     * It first constructs the URL to make a move using the provided game ID and field ID and then sends a request to the server.
     * The server responds with a message which is then printed to the console.
     *
     * @param gameID the ID of the game to make a move in
     * @param fieldID the ID of the field to make a move from
     * @throws Exception if any error occurs during the execution of the method.
     */
    static void move(String gameID, int fieldID) throws Exception {
        String url = server + "/api/move/" + gameID + "/" + name + "/" + fieldID;
        System.out.println(load(url));
    }

    /**
     * Method to load the URL.
     * @param url the URL to load
     * @return the loaded URL
     * @throws Exception if any error occurs during the execution of the method.
     */
    static String load(String url) throws Exception {
        URI uri = new URI(url.replace(" ", ""));
        BufferedReader in = new BufferedReader(new InputStreamReader(uri.toURL().openStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /**
     * This method selects the best move based on the action probabilities from MCTS.
     * @param actionProbs the probabilities of each action
     * @param offset the offset to use when selecting the best move
     * @return the index of the best move
     */
    static int selectBestMove(float[] actionProbs, int offset) {
        int bestMove = -1;
        float maxProb = -1;
        for (int i = offset; i < offset + 6; i++) {
            if (actionProbs[i] > maxProb) {
                maxProb = actionProbs[i];
                bestMove = i;
            }
        }
        return bestMove;
    }
}

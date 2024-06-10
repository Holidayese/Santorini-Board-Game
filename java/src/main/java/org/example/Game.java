package org.example;

import org.example.Exceptions.IllegalBuildException;
import org.example.Exceptions.IllegalMoveException;
import org.example.godcards.GodCard;
import org.example.godcards.GodCardFactory;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


/**
 * Manages the state and logic of a Santorini game session.
 * Controls the flow of the game, including player turns, worker placement, movement, and building.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Game {
    private final List<Player> players = new ArrayList<>();
    private final Board board;
    private Player winner = null;
    private Player currentPlayer;
    private Worker currentWorker;
    private GamePhase gamePhase;
    private PlayerAction currentAction;
    private final Map<Player, GodCard> godCards = new HashMap<>(); // Maps to store god cards assigned to each player

    /**
     * Defines the possible states of the game.
     */
    public enum GamePhase {
        INITIALIZE,
        PLACE_WORKER,
        MOVE,
        BUILD,
        SECOND_BUILD, // New phase for handling the second build if applicable
        GAME_OVER
    }

    /**
     * Defines the possible phases of the current action.
     */
    public enum PlayerAction {
        MOVE,
        BUILD
    }

    /**
     * Initializes a new game with two players.
     *
     * @param player1 The first player.
     * @param player2 The second player.
     */
    public Game(Player player1, Player player2) {
        players.add(player1);
        players.add(player2);
        currentPlayer = player1; // Start game with player1
        currentWorker = player1.getWorkers().get(0);
        gamePhase = GamePhase.INITIALIZE;
        currentAction = null;
        board = new Board();
        // Initialize god cards map with no god cards assigned yet
        godCards.put(player1, null);
        godCards.put(player2, null);
    }

    /**
     * Method to select a god card before placing workers
     *
     * @param player      Player who is selecting god card
     * @param godCardName String god card name
     */
    public void selectGodCard(Player player, String godCardName) {
        if (gamePhase != GamePhase.INITIALIZE) {
            throw new IllegalStateException("God card selection is only allowed in the INITIALIZE phase.");
        }
        GodCard godCard = null;
        if (godCardName != null && !godCardName.isEmpty() && !godCardName.equalsIgnoreCase("None")) {
            godCard = GodCardFactory.createGodCard(godCardName); // Create god card if a valid name is provided
        }
        godCards.put(player, godCard); // Assign null or a specific god card
        logAction("Player: " + player.getPlayerID() + " - God Card: " + godCardName);
        // Check if all players have selected their god cards
        if (players.stream().allMatch(godCards::containsKey)) {
            gamePhase = GamePhase.PLACE_WORKER; // Transition to next phase
        }
    }

    /**
     * Attempts to initialize a worker at a specified position on the board.
     * This action is only allowed if the game is in progress.
     *
     * @param worker   The worker to be placed.
     * @param position The target position on the board.
     * @return true if the worker was successfully placed, false otherwise.
     */
    public boolean placeWorker(Worker worker, BoardPosition position) {
        if (gamePhase == GamePhase.INITIALIZE) {
            gamePhase = GamePhase.PLACE_WORKER;
        }
        // Check game state and current player
        if (gamePhase != GamePhase.PLACE_WORKER) {
            logAction("It is not PLACE_WORKER phase now. You should not place workers.");
            return false;
        }
        if (worker == null) {
            logAction("Worker not found.");
            return false;
        }
        try {
            boolean isSuccessful = board.placeWorkerAt(worker, position);
            if (isSuccessful) {
                determineNextWorker();
                return true;
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            logAction(e.getMessage());
        }
        return false;
    }

    /**
     * Method to select a worker before a Move
     *
     * @param workerId workerId of selected worker
     * @param playerId playerId of the select worker's owner
     * @return true is selected successfully
     */
    public boolean selectWorker(String workerId, String playerId) {
        // Check if it's the player's turn
        if (!currentPlayer.getPlayerID().equals(playerId)) {
            logAction("It's not player " + playerId + "'s turn. Current player: " + currentPlayer.getPlayerID());
            return false;
        }
        Worker selectedWorker = findWorkerById(workerId);
        // Verify the worker belongs to the current player
        if (selectedWorker == null || !selectedWorker.getOwnerID().equals(playerId)) {
            logAction("Worker " + workerId + " not found or does not belong to player " + playerId);
            return false;
        }
        // Set the current worker to the selected worker
        this.currentWorker = selectedWorker;
        logAction("Worker " + workerId + " selected by player " + playerId);
        return true;
    }

    /**
     * Moves a worker to a new position, if the move is legal, and it's the player's turn.
     *
     * @param newPosition The new position for the worker.
     * @return true if the move was successful, false otherwise.
     */
    public boolean moveWorker(BoardPosition newPosition) {
        if (!validateMovePreconditions()) {
            return false;
        }
        BoardPosition oldPosition = currentWorker.getPosition();
        try {
            // Perform the actual move for the current worker
            if (performMove(newPosition)) {
                postMoveActions(oldPosition, newPosition);
                return true;
            }
        } catch (Exception e) {
            logAction(e.getMessage());
        }
        return false;
    }

    /**
     * Validate if required move preconditions is satisfied
     *
     * @return true if move preconditions meet required
     */
    private boolean validateMovePreconditions() {
        // Ensure there is a current worker selected.
        if (currentWorker == null) {
            logAction("No worker has been selected for moving.");
            return false;
        }
        // Check game state
        if (gamePhase != GamePhase.MOVE) {
            logAction("It's not your turn or you should build now.");
            return false;
        }
        // Check current player, current action
        if (isNotPlayerTurn(currentWorker.getOwnerID()) || isNotCurrentAction(PlayerAction.MOVE)) {
            logAction("It's not your turn or you should build now.");
            return false;
        }
        return true;
    }

    /**
     * Perform move without or with god cards if applicable
     *
     * @param newPosition new position to move to
     * @return true if move is successful
     * @throws IllegalMoveException illegal move exception
     */
    private boolean performMove(BoardPosition newPosition) throws IllegalMoveException {
        // Get the god card, if any
        GodCard godCard = godCards.get(currentPlayer);
        BoardPosition oldPosition = currentWorker.getPosition();
        // Step 1: Check if the move is valid including special push logic
        if (godCard != null && !godCard.modifyMoveValidation(currentWorker, oldPosition, newPosition, board)) {
            logAction("Move rejected by god card.");
            return false;
        }
        // Step 2: Execute pre-move
        if (godCard != null && board.getSquare(newPosition).getIsOccupied()) {
            godCard.preMoveExecution(currentWorker, oldPosition, newPosition, board);
        }
        return board.moveWorkerTo(currentWorker, newPosition);
    }

    /**
     * Perform actions post move
     *
     * @param newPosition new position that moves to
     */
    private void postMoveActions(BoardPosition oldPosition, BoardPosition newPosition) {
        GodCard godCard = godCards.get(currentPlayer);
        // Execute any god card-specific post-move logic.
        if (godCard != null) {
            godCard.postMoveExecution(this, currentWorker, oldPosition, newPosition);
        }
        // Check god card specific win condition first
        if (godCard != null && godCard.checkWinCondition(currentWorker, oldPosition, newPosition, board)) {
            declareWinner(currentPlayer);
        }
        // Check standard win condition (reaching third level).
        if (board.hasWorkerClimbToThirdLevelByItsOwn(currentWorker, oldPosition, newPosition)) {
            declareWinner(currentPlayer);
        } else {
            // If no win conditions are met, proceed to the build phase.
            setGamePhase(GamePhase.BUILD);
            setPlayerAction(PlayerAction.BUILD);
        }
    }

    /**
     * Performs a building action at a specified position, if the action is legal, and it's the player's turn.
     *
     * @param buildPosition The position where the block or dome is to be built.
     * @return true if the build was successful, false otherwise.
     */
    public boolean buildBlock(BoardPosition buildPosition) {
        if (!validateBuildPreconditions()) {
            return false;
        }
        try {
            if (performBuild(buildPosition)) {
                postBuildActions(buildPosition);
                return true;
            }
            return false;
        } catch (Exception e) {
            logAction(e.getMessage());
        }
        return false;
    }

    /**
     * Validate if required move preconditions is satisfied
     *
     * @return true if move preconditions meet required
     */
    private boolean validateBuildPreconditions() {
        // First, ensure there is a current worker selected.
        if (currentWorker == null) {
            logAction("No worker has been selected for building.");
            return false;
        }
        // Second, check game state
        if (gamePhase != GamePhase.BUILD && gamePhase != GamePhase.SECOND_BUILD) {
            logAction("It's not the correct phase for building.");
            return false;
        }
        // Check current player, current action
        if (isNotPlayerTurn(currentWorker.getOwnerID()) || isNotCurrentAction(PlayerAction.BUILD)) {
            logAction("It's not your turn or you should move now.");
            return false;
        }
        return true;
    }

    /**
     * Perform build without or with god cards if applicable
     *
     * @param buildPosition The position where to build.
     * @return true if build is successful
     * @throws IllegalBuildException if the build is not allowed
     */
    private boolean performBuild(BoardPosition buildPosition) throws IllegalBuildException {
        GodCard godCard = godCards.get(currentPlayer);
        // Check god card rules if applicable
        if (godCard != null && !godCard.modifyBuildValidation(currentWorker, buildPosition, board)) {
            logAction("Build rejected by god card.");
            return false;
        }
        return board.buildAt(currentWorker, buildPosition);
    }

    /**
     * Perform actions post build
     *
     * @param buildPosition The position where the build occurred.
     */
    private void postBuildActions(BoardPosition buildPosition) {
        GodCard godCard = godCards.get(currentPlayer);
        if (godCard != null) {
            godCard.postBuildExecution(this, currentWorker, buildPosition);
            logAction("Game phase after postBuildAction: " + getGamePhase()); // This should log SECOND_BUILD or other appropriate phase.
        } else {
            // Standard behavior when no god card is involved
            switchTurn(); // Or end the build phase normally
        }
    }

    /**
     * Method to skip option second build or other actions of god cards' special abilities
     *
     * @return true if successfully skip
     */
    public boolean skipGodCardAction() {
        GodCard godCard = godCards.get(currentPlayer);
        if (godCard != null) {
            godCard.skipAction(this);
            return true;
        } else {
            logAction("No god card action to skip or no god card assigned.");
        }
        return false;
    }

    /**
     * Switches the turn to the next player.
     */
    public void switchTurn() {
        logAction("Current phase before switchTurn: " + gamePhase);
        currentPlayer = (currentPlayer == players.get(0)) ? players.get(1) : players.get(0);
        currentWorker = null;
        setGamePhase(GamePhase.MOVE);
        setPlayerAction(PlayerAction.MOVE); // Reset action to MOVE at the start of the new turn
        logAction("Current phase after switchTurn: " + gamePhase);
        activateGodCardForCurrentPlayer();
    }

    /**
     * Activate god cards if applicable
     */
    private void activateGodCardForCurrentPlayer() {
        GodCard godCard = godCards.get(currentPlayer);
        if (godCard != null) {
            godCard.activateEffect(this);
            logAction("Activated effect for " + currentPlayer.getPlayerID());
        }
    }

    /**
     * Declares a player as the winner and ends the game.
     *
     * @param winner The player who has won the game.
     */
    private void declareWinner(Player winner) {
        this.winner = winner;
        endGame();
    }

    /**
     * Ends the game, announcing the winner if there is one.
     */
    private void endGame() {
        gamePhase = GamePhase.GAME_OVER;
        if (winner != null) {
            logAction("Game is over. Winner is " + winner.getPlayerID() + ".");
        } else if (checkForTie()) {
            logAction("Game over due to a tie. No winner.");
        } else {
            logAction("Game is over. No more legal moves available.");
        }
    }

    /**
     * Checks if a tie condition has been reached by determining if any player can make a legal move or build.
     * A tie occurs when neither player can perform any legal actions.
     *
     * @return true if a tie is detected, false otherwise.
     */
    private boolean checkForTie() {
        boolean player1CanAct = canPlayerPerformAnyAction(players.get(0));
        boolean player2CanAct = canPlayerPerformAnyAction(players.get(1));
        return !player1CanAct && !player2CanAct;
    }

    /**
     * Logic to determine and set the next worker based on the current state
     * This could involve checking which workers have been placed and setting the currentWorker accordingly
     */
    private void determineNextWorker() {
        if (allWorkersPlaced()) {
            gamePhase = GamePhase.PLACE_WORKER;
            setGamePhase(GamePhase.MOVE);
            setPlayerAction(PlayerAction.MOVE);
            // Reset currentWorker for the moving phase
            currentPlayer = players.get(0);
            currentWorker = null;
            // Activate god card for the first player
            activateGodCardForCurrentPlayer();
        } else {
            // Switch to the next worker or next player's worker
            switch (currentWorker.getWorkerID()) {
                case "A1" -> currentWorker = findWorkerById("A2");
                case "A2" -> {
                    currentPlayer = findPlayerById("B"); // Assuming a method to find a player by ID
                    currentWorker = findWorkerById("B1");
                }
                case "B1" -> currentWorker = findWorkerById("B2");
                case "B2" -> {
                    // All workers placed, prepare for the next phase
                    currentPlayer = findPlayerById("A"); // Assuming the game starts with player A moving
                    currentWorker = null; // Reset for the moving phase
                    setPlayerAction(PlayerAction.MOVE);
                }
            }
        }
    }

    /**
     * Helper method to check if all workers have been placed for place worker action
     *
     * @return true if all workers are placed
     */
    private boolean allWorkersPlaced() {
        for (Player player : players) {
            for (Worker worker : player.getWorkers()) {
                if (worker.getPosition() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets a list of legal moves for a given worker.
     *
     * @param worker The worker for which to find legal moves.
     * @return A list of BoardPosition objects representing legal moves.
     */
    public List<BoardPosition> calculateLegalMovesForWorker(Worker worker) {
        List<BoardPosition> legalMoves = new ArrayList<>();
        BoardPosition currentPos = worker.getPosition();
        // Check all adjacent positions
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip checking the current position
                int newX = currentPos.getX() + dx;
                int newY = currentPos.getY() + dy;
                if (newX >= 0 && newX < 5 && newY >= 0 && newY < 5) {
                    BoardPosition newPos = new BoardPosition(newX, newY);
                    if (board.isMoveLegal(worker, currentPos, newPos)) {
                        legalMoves.add(newPos);
                    }
                }
            }
        }
        GodCard godCard = godCards.get(worker.getOwner());
        if (godCard != null) {
            legalMoves = godCard.modifyLegalMoves(worker, legalMoves, board);
        }
        logAction("LegalMoves are: ");
        for (BoardPosition pos : legalMoves) {
            System.out.print(pos + " ");
        }
        System.out.println();
        return legalMoves;
    }

    /**
     * Gets a list of legal moves for a given worker.
     *
     * @param worker The worker for which to find legal moves.
     * @return A list of BoardPosition objects representing legal moves.
     */
    public List<BoardPosition> calculateLegalBuildsForWorker(Worker worker) {
        List<BoardPosition> legalBuilds = new ArrayList<>();
        BoardPosition currentPos = worker.getPosition();
        // Check adjacent positions for legal build options
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the current position of the worker
                int newX = currentPos.getX() + dx;
                int newY = currentPos.getY() + dy;
                if (newX >= 0 && newX < 5 && newY >= 0 && newY < 5) {
                    BoardPosition newPos = new BoardPosition(newX, newY);
                    if (board.isBuildLegal(worker, currentPos, newPos)) {
                        legalBuilds.add(newPos);
                    }
                }
            }
        }
        GodCard godCard = godCards.get(worker.getOwner());
        if (godCard != null) {
            legalBuilds = godCard.modifyLegalBuilds(worker, legalBuilds, board);
        }
        logAction("LegalBuilds are: ");
        for (BoardPosition pos : legalBuilds) {
            System.out.print(pos + " ");
        }
        System.out.println();
        return legalBuilds;
    }

    /**
     * Checks if the specified player can make any legal move with any of their workers.
     *
     * @param player The player to check for possible moves.
     * @return true if at least one legal move exists for any of the player's workers, false otherwise.
     */
    private boolean canPlayerMove(Player player) {
        for (Worker worker : player.getWorkers()) {
            if (canWorkerMove(worker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified player can perform any legal build with any of their workers.
     *
     * @param player The player to check for possible builds.
     * @return true if at least one legal build exists for any of the player's workers, false otherwise.
     */
    private boolean canPlayerBuild(Player player) {
        for (Worker worker : player.getWorkers()) {
            if (canWorkerBuild(worker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified worker can make any legal move with any of their workers.
     *
     * @param worker specified worker
     * @return true if this worker can make legal moves
     */
    private boolean canWorkerMove(Worker worker) {
        return !calculateLegalMovesForWorker(worker).isEmpty();
    }

    /**
     * Checks if the specified worker can make any legal builds with any of their workers.
     *
     * @param worker specified worker
     * @return true if this worker can make legal builds
     */
    private boolean canWorkerBuild(Worker worker) {
        return !calculateLegalBuildsForWorker(worker).isEmpty();
    }

    /**
     * Checks if the specified player can perform any legal action (move or build).
     *
     * @param player The player to check.
     * @return true if the player can perform any legal action, false otherwise.
     */
    private boolean canPlayerPerformAnyAction(Player player) {
        return canPlayerMove(player) || canPlayerBuild(player);
    }

    /**
     * Getter methods to get player-god cards map
     *
     * @return map
     */
    public Map<Player, GodCard> getGodCards() {
        return godCards;
    }

    /**
     * Getter method to get god card for a player
     *
     * @param player player
     * @return god card of this player
     */
    public GodCard getGodCardForPlayer(Player player) {
        return godCards.getOrDefault(player, null); // Returns null if no god card is assigned
    }

    /**
     * Getter method to get players
     *
     * @return list of players
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Getter method to get current player
     *
     * @return current player's playerID
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Getter method to get current worker
     *
     * @return current worker's workerID
     */
    public Worker getCurrentWorker() {
        return currentWorker;
    }

    /**
     * Getter for Board
     *
     * @return board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Getter for game state
     *
     * @return gameState
     */
    public GamePhase getGamePhase() {
        return gamePhase;
    }

    /**
     * Getter for current action
     *
     * @return currentAction
     */
    public PlayerAction getCurrentAction() {
        return currentAction;
    }

    /**
     * Getter method to get winner
     *
     * @return winner's playerID
     */
    public String getWinnerID() {
        if (this.winner != null) {
            return this.winner.getPlayerID();
        } else {
            return null;
        }
    }

    /**
     * Finds a player by its unique ID.
     *
     * @param playerId The ID of the player to find.
     * @return The Player object if found, null otherwise.
     */
    public Player findPlayerById(String playerId) {
        for (Player player : players) {
            if (player.getPlayerID().equals(playerId)) {
                return player;
            }
        }
        return null; // Worker not found
    }

    /**
     * Finds a worker by its unique ID.
     *
     * @param workerId The ID of the worker to find.
     * @return The Worker object if found, null otherwise.
     */
    public Worker findWorkerById(String workerId) {
        for (Player player : players) {
            for (Worker worker : player.getWorkers()) {
                if (worker.getWorkerID().equals(workerId)) {
                    return worker;
                }
            }
        }
        return null; // Worker not found
    }

    /**
     * Setters for current worker
     *
     * @param w current worker
     */
    public void setCurrentWorker(Worker w) {
        this.currentWorker = w;
    }

    /**
     * Setters for game phase
     *
     * @param gamePhase current game phase
     */
    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    /**
     * Setters for player action
     *
     * @param playerAction current player action
     */
    public void setPlayerAction(PlayerAction playerAction) {
        this.currentAction = playerAction;
    }

    /**
     * Checks if it is currently not the turn of the specified player.
     *
     * @param playerID The player to check
     * @return true if it is not the player's turn, false otherwise.
     */
    private boolean isNotPlayerTurn(String playerID) {
        return !currentPlayer.getPlayerID().equals(playerID);
    }

    /**
     * Check if it is currently not right phase for action move or build
     *
     * @param action action to check
     * @return true if not consistent with correct current action
     */
    private boolean isNotCurrentAction(PlayerAction action) {
        return action != currentAction;
    }

    /**
     * Print action result message for debugging
     *
     * @param message message to log in print
     */
    private void logAction(String message) {
        System.out.println(message);
    }
}

package org.example;

import org.example.Exceptions.IllegalBuildException;
import org.example.Exceptions.IllegalMoveException;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the game board for Santorini, consisting of a 5x5 grid of squares.
 * Each square can hold a tower and may be occupied by a worker.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Board {
    private static final int BOARD_SIZE = 5;
    private static final int MAX_LEVELS = 3;
    private final Square[][] squares = new Square[BOARD_SIZE][BOARD_SIZE];
    private final Map<Worker, BoardPosition> workerPositions = new HashMap<>();

    /**
     * Constructs a new Board, initializing each square within the 5x5 grid.
     */
    public Board() {
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                squares[i][j] = new Square();
            }
        }
    }

    /**
     * Places a worker at the specified position on the board, if the position is not out of bounds or already occupied.
     *
     * @param worker   The worker to be placed.
     * @param position The position where the worker is to be placed.
     * @return true if the worker was successfully placed, false otherwise.
     */
    public boolean placeWorkerAt(Worker worker, BoardPosition position) {
        if (getSquare(position).getIsOccupied()) {
            logAction("Cannot place worker at: " + position + " . It is occupied.");
            return false;
        }
        if (isOutOfBounds(position)) {
            logAction("Cannot place worker at: " + position + " . It is out of bounds");
            return false;
        }
        updateWorkerPosition(worker, position);
        logAction(worker.getWorkerID() + " is placed at " + position);
        return true;
    }

    /**
     * Moves a worker to a new position on the board if the move is legal.
     *
     * @param worker      The worker to move.
     * @param newPosition The new position for the worker.
     * @return true if the move was made successfully.
     * @throws Exception if the move is not legal.
     */
    public boolean moveWorkerTo(Worker worker, BoardPosition newPosition) throws IllegalMoveException {
        if (!isMoveLegal(worker, worker.getPosition(), newPosition)) {
            throw new IllegalMoveException("Move from " + worker.getPosition() + " to " + newPosition + " is not legal.");
        }
        executeMoveAction(worker, newPosition);
        return true;
    }

    /**
     * Helper method to execute actual move action
     *
     * @param worker      worker who performs move
     * @param newPosition new position that worker is going to
     */
    private void executeMoveAction(Worker worker, BoardPosition newPosition) {
        updateWorkerPosition(worker, newPosition);
        logAction("Successful move. " + worker.getWorkerID() + " moved to " + newPosition + ".");
    }

    /**
     * Performs a build action at the specified position, if legal.
     *
     * @param worker        The worker performing the build action.
     * @param buildPosition The position where the build action is to be performed.
     * @return true if the build action was successful.
     * @throws Exception if the build action is not legal.
     */
    public boolean buildAt(Worker worker, BoardPosition buildPosition) throws IllegalBuildException {
        if (!isBuildLegal(worker, worker.getPosition(), buildPosition)) {
            throw new IllegalBuildException("Build at " + buildPosition + " is not legal.");
        }
        return executeBuildAction(worker, buildPosition);
    }

    /**
     * Helper method to execute actual build action
     *
     * @param worker        worker who performs build
     * @param buildPosition position that performs buildb
     * @return
     */
    private boolean executeBuildAction(Worker worker, BoardPosition buildPosition) {
        Square targetSquare = getSquare(buildPosition);
        int levels = targetSquare.getBuildingLevel();
        boolean hasDome = targetSquare.hasDome();
        if (levels < MAX_LEVELS) {
            targetSquare.buildBlock();
            logAction("Successful build. A block has been built at " + buildPosition + " by " + worker.getWorkerID() + ".");
            return true;
        } else if (levels == MAX_LEVELS && !hasDome) {
            targetSquare.placeDome();
            logAction("A dome has been placed at " + buildPosition);
            return true;
        }
        logAction("No build action executed: either the tower is complete or not eligible for a dome.");
        return false;
    }

    /**
     * Push the opponent worker to its behind position
     *
     * @param opponentWorker worker that is going to be pushed
     * @param pushTo         target position for push
     * @return true if push succeeds
     */
    public boolean pushOpponentWorker(Worker opponentWorker, BoardPosition pushTo) {
        // Validate the push position
        if (isOutOfBounds(pushTo) || getSquare(pushTo).hasDome() || getSquare(pushTo).getIsOccupied()) {
            return false; // Push position is invalid
        }
        // Push the opponent worker
        updateWorkerPosition(opponentWorker, pushTo);
        return true; // Push was successful
    }

    /**
     * Updates the board and worker position mappings when a worker moves or is placed.
     *
     * @param worker      The worker whose position is being updated.
     * @param newPosition The worker's new position.
     */
    public void updateWorkerPosition(Worker worker, BoardPosition newPosition) {
        // Remove the worker from their old position
        BoardPosition oldPosition = workerPositions.get(worker);
        if (oldPosition != null) {
            getSquare(oldPosition).setOccupiedWorker(null);
        }
        // Update the map with the new position
        workerPositions.put(worker, newPosition);
        getSquare(newPosition).setOccupiedWorker(worker);
        // Update the worker's position (assuming Worker class has setPosition method)
        worker.setPosition(newPosition);
        logAction("Worker " + worker.getWorkerID() + "'s position is updated from " + oldPosition + " to " + newPosition);
    }

    /**
     * Checks if the current player has any worker on the third level of a tower, which is a win condition.
     *
     * @param currentWorker The player to check for a win condition.
     * @return true if the currentPlayer has a worker on the third level, false otherwise.
     */
    public boolean hasWorkerClimbToThirdLevelByItsOwn(Worker currentWorker, BoardPosition from, BoardPosition to) {
        // First, get the building levels at the from and to positions
        int fromLevel = getSquare(from).getBuildingLevel();
        int toLevel = getSquare(to).getBuildingLevel();
        // Check if the worker moved up directly from the second to the third level
        if (fromLevel == (MAX_LEVELS - 1) && toLevel == MAX_LEVELS) {
            return true;
        }
        return false;
    }

    /**
     * Checks if moving a worker from one position to another is legal according to game rules.
     * This method checks the god card rules as well as the standard move legality.
     *
     * @param worker The worker moving.
     * @param from   The starting position of the move.
     * @param to     The target position of the move.
     * @return true if the move is legal, false otherwise.
     */
    public boolean isMoveLegal(Worker worker, BoardPosition from, BoardPosition to) {
        logAction("isMoveLegal check:");
        logAction("  isOutOfBounds: " + isOutOfBounds(to));
        logAction("  isTargetOccupied: " + isTargetOccupied(to));
        logAction("  isMoveTargetAdjacentAndLegal: " + isMoveTargetAdjacentAndLegal(from, to));
        return !isOutOfBounds(to) && !isTargetOccupied(to) && isMoveTargetAdjacentAndLegal(from, to);
    }

    /**
     * Checks if building at a specified position is legal according to game rules and potential god card effects.
     *
     * @param worker The worker attempting to build.
     * @param from   The position of the worker attempting the build.
     * @param to     The target position for the build.
     * @return true if the build action is legal, false otherwise.
     */
    public boolean isBuildLegal(Worker worker, BoardPosition from, BoardPosition to) {
        boolean outOfBounds = isOutOfBounds(to);
        boolean targetOccupied = isTargetOccupied(to);
        boolean buildTargetLegal = isBuildTargetAdjacentAndLegal(from, to);

        return !outOfBounds && !targetOccupied && buildTargetLegal;
    }

    /**
     * Determines if a move from one position to another is adjacent and legal according to game rules.
     * A move is considered legal if the target square is adjacent, not occupied by a dome, and not more than one level higher than the current square.
     *
     * @param from The current position of the worker.
     * @param to   The target position to move the worker to.
     * @return true if the move is legal, false otherwise.
     */
    private boolean isMoveTargetAdjacentAndLegal(BoardPosition from, BoardPosition to) {
        Square currentSquare = getSquare(from);
        Square targetSquare = getSquare(to);
        return from.isAdjacentTo(to) && // Target position is not adjacent
                !targetSquare.hasDome() && // Target square has a dome
                targetSquare.getBuildingLevel() <= currentSquare.getBuildingLevel() + 1; // Target square is more than one level higher
    }

    /**
     * Checks if a build action at a specified position is adjacent and legal from the worker's current position.
     * Building is considered legal if the target square is adjacent, does not have a dome, and is not already at the maximum height.
     *
     * @param from The current position of the worker attempting to build.
     * @param to   The target position for the build action.
     * @return true if the build action is legal, false otherwise.
     */
    private boolean isBuildTargetAdjacentAndLegal(BoardPosition from, BoardPosition to) {
        Square targetSquare = getSquare(to);
        boolean isAdjacent = from.isAdjacentTo(to);
        boolean hasNoDome = !targetSquare.hasDome();
        boolean canBuildBlock = targetSquare.getBuildingLevel() < MAX_LEVELS;
        boolean canPlaceDome = targetSquare.getBuildingLevel() == MAX_LEVELS;

        return isAdjacent && hasNoDome && (canBuildBlock || canPlaceDome);
    }

    /**
     * Checks if a given position is outside the bounds of the game board.
     *
     * @param position The position to check.
     * @return true if the position is out of bounds, false otherwise.
     */
    public boolean isOutOfBounds(BoardPosition position) {
        return position.getX() < 0 || position.getX() > 4 || position.getY() < 0 || position.getY() > 4;
    }

    /**
     * Retrieves the Square at the specified BoardPosition.
     *
     * @param boardPosition The position on the board.
     * @return The Square at the given position.
     */
    public Square getSquare(BoardPosition boardPosition) {
        return squares[boardPosition.getX()][boardPosition.getY()];
    }

    /**
     * Determines if a given square on the board is currently occupied by a worker.
     *
     * @param position The position of the square to check.
     * @return true if the square is occupied, false otherwise.
     */
    private boolean isTargetOccupied(BoardPosition position) {
        return getSquare(position).getIsOccupied();
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

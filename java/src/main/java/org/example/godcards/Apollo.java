package org.example.godcards;

import org.example.*;

import java.util.List;

/**
 * Apollo god card allows a worker to move into an opponent worker's space,
 * swapping places with the opponent's worker.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Apollo implements GodCard {
    private Worker displacedWorker; // the opponent worker is going to be swapped

    // No activation effect is needed for Apollo since the ability is passive and activated during move validation.
    @Override
    public void activateEffect(Game game) {
    }

    // No deactivation effect is needed since the ability does not persist or modify state outside individual moves.
    @Override
    public void deactivateEffect(Game game) {
    }

    /**
     * Apollo can use its special ability to swap position with an opponent worker
     *
     * @param worker Apollo worker
     * @param from old position
     * @param to new position
     * @param board game board
     * @return true if move is valid
     */
    @Override
    public boolean modifyMoveValidation(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // First, check standard move legality.
        if (board.isMoveLegal(worker, from, to)) {
            return true;
        }
        return canSwapWithOpponentWorker(board, worker, from, to);
    }

    /**
     * Helper method to check if Apollo worker can swap with the opponent worker as its special ability
     *
     * @param board  game board
     * @param worker Apollo worker
     * @param from   old position
     * @param to     new position
     * @return true if Apollo worker can use its special ability
     */
    private boolean canSwapWithOpponentWorker(Board board, Worker worker, BoardPosition from, BoardPosition to) {
        Square toSquare = board.getSquare(to);
        // Validate the basic conditions for a push
        if (!toSquare.getIsOccupied() || toSquare.getOccupiedWorker().getOwnerID().equals(worker.getOwnerID()) ||
                !from.isAdjacentTo(to) || board.isOutOfBounds(to) ||
                toSquare.getBuildingLevel() > board.getSquare(from).getBuildingLevel() + 1) {
            return false;
        }
        // Temporarily store the displaced worker to handle in postMoveExecution.
        displacedWorker = toSquare.getOccupiedWorker();
        return true;
    }

    // Apollo does not modify build validation; use the standard rules.
    @Override
    public boolean modifyBuildValidation(Worker worker, BoardPosition buildPosition, Board board) {
        return board.isBuildLegal(worker, worker.getPosition(), buildPosition);
    }

    /**
     * Modifies the list of legal moves for Apollo's worker based on the god card's unique ability.
     * Apollo allows a worker to move into an adjacent square occupied by an opponent's worker under specific conditions.
     * This method extends the standard legal moves to include such possibilities.
     *
     * @param worker     The worker whose legal moves are being calculated.
     * @param legalMoves The initial list of legal moves based on standard game rules, usually unoccupied adjacent squares.
     * @param board      The game board, providing context and access to other squares and their occupancy.
     * @return A list of {@link BoardPosition} objects representing all legal moves for Apollo's worker, including the
     * potential to move into squares occupied by opponent workers if the height difference is appropriate.
     * Specifically, Apollo can move into an occupied square if:
     * 1. The square is directly adjacent to the worker's current position.
     * 2. The square is occupied by an opponent's worker.
     * 3. The height difference between Apollo's current square and the target square is one level or less,
     * meaning the target square is not more than one level higher than the current square.
     * <p>
     * The method first checks if the potential move positions are within board boundaries and then verifies if
     * they meet the height criteria and are either unoccupied or valid for Apollo's special move capability.
     * Each valid move, whether a standard or special Apollo move, is added to the returned list of legal moves.
     */
    @Override
    public List<BoardPosition> modifyLegalMoves(Worker worker, List<BoardPosition> legalMoves, Board board) {
        // Get the current position of the worker.
        BoardPosition currentPosition = worker.getPosition();

        // Iterate over all possible move directions.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;  // Skip the current position.

                int newX = currentPosition.getX() + dx;
                int newY = currentPosition.getY() + dy;
                BoardPosition newPosition = new BoardPosition(newX, newY);

                // Check if the new position is within board limits.
                if (board.isOutOfBounds(newPosition)) {
                    continue;
                }

                Square targetSquare = board.getSquare(newPosition);
                int heightDifference = targetSquare.getBuildingLevel() - board.getSquare(currentPosition).getBuildingLevel();

                // Check for Apollo's special move condition: the square is occupied by an opponent's worker.
                if (targetSquare.getIsOccupied() && !targetSquare.getOccupiedWorker().getOwner().equals(worker.getOwner())) {
                    // Ensure the height difference allows for a move.
                    if (heightDifference <= 1) {
                        legalMoves.add(newPosition);
                    }
                }
            }
        }
        return legalMoves;
    }

    // Apollo's power does not influence building rules.
    @Override
    public List<BoardPosition> modifyLegalBuilds(Worker worker, List<BoardPosition> legalBuilds, Board board) {
        return legalBuilds;
    }

    // No post-build action needed for Minotaur
    @Override
    public void postBuildExecution(Game game, Worker worker, BoardPosition buildPosition) {
        game.switchTurn();
    }

    /**
     * If Apollo worker decides to use its special ability, the game should make target square unoccupied
     * before Apollo worker perform move.
     *
     * @param worker Apollo worker
     * @param from   old position
     * @param to     new position
     * @param board  game board
     * @return true if pre move execution is successful
     */
    @Override
    public boolean preMoveExecution(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Clear the space for Apollo's worker to move in.
        if (displacedWorker != null) {
            board.getSquare(to).setOccupiedWorker(null);
        }
        return true;
    }

    /**
     * After Apollo worker uses it special ability and itself moves to new position,
     * the game should swap the opponent worker to the position that the Apollo worker just vacated.
     *
     * @param game   game
     * @param worker Apollo worker
     * @param from   old position
     * @param to     new position
     */
    @Override
    public void postMoveExecution(Game game, Worker worker, BoardPosition from, BoardPosition to) {
        if (displacedWorker != null) {
            // Place the displaced worker in the square that Apollo's worker just vacated.
            game.getBoard().updateWorkerPosition(displacedWorker, from);
            displacedWorker.setPosition(from);
            displacedWorker = null; // Reset the state after handling.
        }
        game.getBoard().updateWorkerPosition(worker, to);
    }

    // No skip action necessary for Apollo.
    @Override
    public void skipAction(Game game) {
    }

    /**
     * Checks win condition that if Apollo worker moves from 2 to 3 itself
     *
     * @param worker Apollo worker
     * @param from   old position
     * @param to     new position
     * @param board  game board
     * @return true if Apollo worker wins
     */
    @Override
    public boolean checkWinCondition(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Checks if the worker has moved up onto the third level, not by force
        return board.getSquare(to).getBuildingLevel() == 3 && board.getSquare(from).getBuildingLevel() < board.getSquare(to).getBuildingLevel();
    }

    @Override
    public String getName() {
        return "Apollo";
    }

}

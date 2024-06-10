package org.example.godcards;

import org.example.*;

import java.util.List;

/**
 * Implements the Minotaur god card for the Santorini game, allowing a worker to push an opponent's worker.
 * This god card modifies the standard movement rules to allow a worker to move into an opponent's space and
 * push the opponent's worker to an adjacent space behind them, provided that space is unoccupied and at any level.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Minotaur implements GodCard {

    // The Minotaur's effect is directly related to move validation, no activation needed.
    @Override
    public void activateEffect(Game game) {
    }

    // The Minotaur's effect is directly related to move validation, no deactivation needed.
    @Override
    public void deactivateEffect(Game game) {
    }

    /**
     * Modifies the movement validation logic to include the ability to push an opponent's worker according to Minotaur's rules.
     * A worker can move into an occupied space if it is possible to push the opponent's worker directly backwards
     * to an unoccupied space, irrespective of its level.
     *
     * @param worker The worker attempting to move.
     * @param from   The original position of the worker before the move.
     * @param to     The target position of the move, potentially occupied by an opponent's worker.
     * @param board  The game board, providing context needed for the decision.
     * @return true if the move is valid either according to standard rules or Minotaur's special ability, false otherwise.
     */
    @Override
    public boolean modifyMoveValidation(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        if (board.isMoveLegal(worker, from, to)) {
            return true;  // Basic movement rules apply first.
        }
        return canPushOpponent(worker, from, to, board);
    }

    /**
     * Checks if the Minotaur's worker can push an opponent's worker from a specified position.
     * The push is allowed if:
     * - The target position is occupied by an opponent's worker.
     * - The positions of Minotaur's worker and the opponent's worker are adjacent.
     * - The move does not push the opponent's worker out of bounds.
     * - The level of the target position is not more than one level higher than the level of the Minotaur's starting position.
     * - The position directly behind the opponent's worker is unoccupied, within bounds, and does not have a dome.
     *
     * @param worker The Minotaur's worker attempting to push.
     * @param from   The current position of the Minotaur's worker.
     * @param to     The target position occupied by the opponent's worker.
     * @param board  The game board, providing context for the positions.
     * @return true if the Minotaur can push the opponent's worker according to the conditions, false otherwise.
     */
    private boolean canPushOpponent(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        Square toSquare = board.getSquare(to);

        // Validate the basic conditions for a push
        if (!toSquare.getIsOccupied() || toSquare.getOccupiedWorker().getOwner().equals(worker.getOwner()) ||
                !from.isAdjacentTo(to) || board.isOutOfBounds(to) ||
                toSquare.getBuildingLevel() > board.getSquare(from).getBuildingLevel() + 1) {
            return false;
        }

        // Calculate the position behind the opponent's worker
        BoardPosition behind = calculatePositionBehind(to, from);
        // Check if the behind position is valid for pushing
        return isValidPushPosition(behind, board);
    }

    /**
     * Calculates the board position directly behind a given target position from a specific starting position,
     * effectively determining where an opponent's worker would be pushed if the move is executed by Minotaur.
     *
     * @param to   The position to which the worker is moving.
     * @param from The original position from which the worker is moving.
     * @return The position directly behind the target position relative to the move direction.
     */
    private BoardPosition calculatePositionBehind(BoardPosition to, BoardPosition from) {
        // Calculate the new position based on the direction of movement.
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        return new BoardPosition(to.getX() + dx, to.getY() + dy);
    }

    @Override
    public boolean modifyBuildValidation(Worker worker, BoardPosition buildPosition, Board board) {
        // Minotaur's powers do not affect building, thus it uses the standard build validation rules.
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

    // No modification needed for generating legal builds generally
    @Override
    public List<BoardPosition> modifyLegalBuilds(Worker worker, List<BoardPosition> legalBuilds, Board board) {
        return legalBuilds;
    }

    @Override
    public void postBuildExecution(Game game, Worker worker, BoardPosition buildPosition) {
        // No post-build action needed for Minotaur
        game.switchTurn();
    }

    /**
     * Executes the special move action of the Minotaur, which includes pushing an opponent's worker.
     * This method is called before the actual move of the Minotaur's worker if the move validation passes.
     *
     * @param minotaurWorker The Minotaur's worker attempting the push.
     * @param from           The current position of the Minotaur's worker.
     * @param to             The target position occupied by the opponent's worker to be pushed.
     * @param board          The game board on which the action is performed.
     * @return true if the opponent's worker was successfully pushed, false if the push could not be executed.
     */
    @Override
    public boolean preMoveExecution(Worker minotaurWorker, BoardPosition from, BoardPosition to, Board board) {
        Worker opponentWorker = board.getSquare(to).getOccupiedWorker();
        // Execute the push here
        if (opponentWorker != null) {
            // Calculate the position from push the opponent worker
            BoardPosition pushToPosition = calculatePositionBehind(to, from);

            // Directly update the opponent worker's position
            if (isValidPushPosition(pushToPosition, board)) {
                return board.pushOpponentWorker(opponentWorker, pushToPosition);
            }
        }
        return false;
    }

    @Override
    public void postMoveExecution(Game game, Worker worker, BoardPosition from, BoardPosition to) {
    }

    /**
     * Validates whether a position is suitable for pushing an opponent's worker to.
     * The position must not be out of bounds, occupied, or have a dome on it.
     *
     * @param position The position to validate for pushing.
     * @param board    The game board used for checking the position details.
     * @return true if the position is valid for pushing the worker to, false otherwise.
     */
    private boolean isValidPushPosition(BoardPosition position, Board board) {
        if (board.isOutOfBounds(position)) {
            return false;
        }
        if (board.getSquare(position).getIsOccupied()) {
            return false;
        }
        if (board.getSquare(position).hasDome()) {
            return false;
        }
        return true;
    }

    // No skip action needed for Minotaur
    @Override
    public void skipAction(Game game) {
        // No skip action needed for Minotaur
    }

    // The Minotaur's ability does not directly affect win conditions.
    @Override
    public boolean checkWinCondition(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Checks if the worker has moved up onto the third level, not by force
        return board.getSquare(to).getBuildingLevel() == 3 && board.getSquare(from).getBuildingLevel() < board.getSquare(to).getBuildingLevel();
    }

    /**
     * Returns the name of the god card, which can be useful for debugging or displaying in the user interface.
     *
     * @return A string representing the name of the god card.
     */
    @Override
    public String getName() {
        return null;
    }
}

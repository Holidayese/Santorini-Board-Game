package org.example.godcards;

import org.example.*;

import java.util.List;

/**
 * Pan: You also win if your Worker moves down two or more levels.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Pan implements GodCard {

    /**
     * Activates or initializes any necessary state at the start of the player's turn.
     * For Pan, no initial state setup is needed specific to the beginning of a turn.
     */
    @Override
    public void activateEffect(Game game) {
    }

    /**
     * Deactivates or clears any states at the end of the player's turn.
     * For Pan, no cleanup is required since the effect is checked per move.
     */
    @Override
    public void deactivateEffect(Game game) {
    }

    /**
     * Modifies the move validation logic to implement Pan's special win condition ability.
     * Allows the player to win if the worker descends two or more levels.
     *
     * @param worker The worker attempting to move.
     * @param from   The starting position of the move.
     * @param to     The target position of the move.
     * @param board  The game board.
     * @return true if the move is valid according to standard game rules, and checks if Pan's win condition is met.
     */
    @Override
    public boolean modifyMoveValidation(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Since Pan's power does not affect movement validation, use the standard game rules
        return board.isMoveLegal(worker, from, to);
    }

    /**
     * Pan does not modify the standard building validation rules, so this method delegates to the standard rules.
     *
     * @param worker        The worker performing the build.
     * @param buildPosition The position where the worker attempts to build.
     * @param board         The game board, providing context needed for the decision.
     * @return true if the build is valid according to standard game rules, false otherwise.
     */
    @Override
    public boolean modifyBuildValidation(Worker worker, BoardPosition buildPosition, Board board) {
        // Default to the standard build validation
        return board.isBuildLegal(worker, worker.getPosition(), buildPosition);
    }

    // No modification needed for legal moves; return the input list.
    @Override
    public List<BoardPosition> modifyLegalMoves(Worker worker, List<BoardPosition> legalMoves, Board board) {
        return legalMoves; // No change to legal moves for Pan.
    }

    // No modification needed for legal builds; return the input list.
    @Override
    public List<BoardPosition> modifyLegalBuilds(Worker worker, List<BoardPosition> legalBuilds, Board board) {
        return legalBuilds; // No change to legal builds for Pan.
    }

    // Pan does not use a post-build action.
    @Override
    public void postBuildExecution(Game game, Worker worker, BoardPosition buildPosition) {
        // No post-build action for Pan.
        game.switchTurn();
    }

    // Pan does not modify move preconditions.
    @Override
    public boolean preMoveExecution(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        return true;
    }

    // No special post move execution for Pan
    @Override
    public void postMoveExecution(Game game, Worker worker, BoardPosition from, BoardPosition to) {
    }

    // No special skip action needed for Pan.
    @Override
    public void skipAction(Game game) {
    }

    /**
     * Checks if Pan's special win condition is met, in addition to the standard game win conditions.
     * Pan wins if the worker moves down two or more levels in a single move. This method checks for that condition,
     * as well as the standard condition of climbing to the third level of a tower.
     *
     * @param worker The worker who is moving.
     * @param from   The position from which the worker is moving.
     * @param to     The position to which the worker is moving.
     * @param board  The game board, providing context for the move.
     * @return true if Pan's win condition or the standard win condition is met, false otherwise.
     */
    @Override
    public boolean checkWinCondition(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        int fromLevel = board.getSquare(from).getBuildingLevel();
        int toLevel = board.getSquare(to).getBuildingLevel();
        // Pan's special win condition: moving down two or more levels
        if (fromLevel - toLevel >= 2) {
            return true;
        }
        // Also check the standard win condition.
        return board.hasWorkerClimbToThirdLevelByItsOwn(worker, from, to);
    }

    /**
     * Returns the name of the god card, useful for debugging or displaying in the user interface.
     *
     * @return A string representing the name of the god card.
     */
    @Override
    public String getName() {
        return "Pan";
    }
}

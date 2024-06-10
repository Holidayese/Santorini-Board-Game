package org.example.godcards;

import org.example.*;

import java.util.List;

/**
 * Demeter: Your Worker may build one additional time, but not on the same space.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Demeter implements GodCard {
    private BoardPosition lastBuildPosition = null;
    private boolean hasBuiltOnce = false;

    /**
     * This method activates or resets the effect of the Demeter god card at the start of the turn.
     * It ensures that the god card's state is correctly initialized when the player's turn starts.
     */
    @Override
    public void activateEffect(Game game) {
        // Reset the last build position at the start of the turn
        lastBuildPosition = null;
        hasBuiltOnce = false;
    }

    /**
     * This method deactivates or clears the effect of the Demeter god card.
     * It is typically called at the end of the turn to clean up any state before the next player's turn.
     */
    @Override
    public void deactivateEffect(Game game) {
        // Clear the last build position at the end of the turn
        lastBuildPosition = null;
        hasBuiltOnce = false;
    }

    /**
     * This method modifies the moving validation logic.
     *
     * @param worker The worker performing the move.
     * @param from   The position where the worker initially at.
     * @param to     The position where the worker attempts to move.
     * @param board  The game board, providing context needed for the decision.
     * @return true if the move is valid according to Demeter's rules, false otherwise.
     */
    @Override
    public boolean modifyMoveValidation(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Since Demeter's power does not affect movement, just use the standard game rules for movement validation.
        return board.isMoveLegal(worker, from, to);
    }

    /**
     * This method modifies the building validation logic to implement Demeter's special building ability.
     * It allows the worker to build one additional time, but not on the same space as the initial build.
     *
     * @param worker        The worker performing the build.
     * @param buildPosition The position where the worker attempts to build.
     * @param board         The game board, providing context needed for the decision.
     * @return true if the build is valid according to Demeter's rules, false otherwise.
     */
    @Override
    public boolean modifyBuildValidation(Worker worker, BoardPosition buildPosition, Board board) {
        // Check if this is the first build or a subsequent build on a different square
        if (hasBuiltOnce && lastBuildPosition != null && lastBuildPosition.equals(buildPosition)) {
            // Prevent building on the same square in the same turn
            return false;
        }
        // Fallback to standard legality check if conditions above don't apply
        return board.isBuildLegal(worker, worker.getPosition(), buildPosition);
    }

    // Demeter does not affect movement
    @Override
    public List<BoardPosition> modifyLegalMoves(Worker worker, List<BoardPosition> legalMoves, Board board) {
        return legalMoves;
    }

    // For second build, Demeter cannot build on the same square
    @Override
    public List<BoardPosition> modifyLegalBuilds(Worker worker, List<BoardPosition> legalBuilds, Board board) {
        if (hasBuiltOnce) {
            legalBuilds.removeIf(position -> position.equals(lastBuildPosition)); // Remove the last build position if built once
        }
        return legalBuilds;
    }

    /**
     * Demeter may perform a second build
     * @param game game
     * @param worker current worker
     * @param buildPosition position performs build
     */
    @Override
    public void postBuildExecution(Game game, Worker worker, BoardPosition buildPosition) {
        // If this is the first build, allow for a possible second build.
        if (!hasBuiltOnce) {
            firstBuildAction(buildPosition, game, worker);
        } else {
            endBuildPhase(game, worker);
        }
    }

    // This method should execute after the first successful build.
    private void firstBuildAction(BoardPosition buildPosition, Game game, Worker worker){
        hasBuiltOnce = true;
        lastBuildPosition = buildPosition;
        game.setGamePhase(Game.GamePhase.SECOND_BUILD); // Set to second build phase
    }

    /**
     * This method should execute after the second build or if the player chooses to skip the second build.
     * @param game
     * @param worker
     */
    private void endBuildPhase(Game game, Worker worker){
        // Reset for next turn
        hasBuiltOnce = false;
        lastBuildPosition = null;
        game.switchTurn(); // End the current player's turn
    }

    @Override
    public boolean preMoveExecution(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Demeter's power does not affect pre-move conditions.
        return true;
    }

    @Override
    public void postMoveExecution(Game game, Worker worker, BoardPosition from, BoardPosition to) {
        // Demeter's power does not affect post-move conditions.
    }

    @Override
    public void skipAction(Game game) {
        if (hasBuiltOnce) {
            hasBuiltOnce = false;
            lastBuildPosition = null;
            game.switchTurn();
            game.setGamePhase(Game.GamePhase.MOVE);
        } else {
        }
    }

    @Override
    public boolean checkWinCondition(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Demeter's ability does not affect winning conditions directly.
        return false;
    }

    /**
     * Returns the name of the god card, which can be useful for debugging or displaying in the user interface.
     *
     * @return A string representing the name of the god card.
     */
    @Override
    public String getName() {
        return "Demeter";
    }

    /**
     * Getter method for lastBuildPosition
     *
     * @return BoardPosition lastBuildPosition
     */
    public BoardPosition getLastBuildPosition() {
        return lastBuildPosition;
    }

    /**
     * Getter method for hasBuiltOnce
     *
     * @return boolean hasBuiltOnce
     */
    public boolean getHasBuiltOnce() {
        return hasBuiltOnce;
    }
}

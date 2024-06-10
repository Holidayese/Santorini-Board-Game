package org.example.godcards;

import org.example.*;

import java.util.List;

/**
 * Hephaestus God Card allows a worker to build one additional block on the same space after the first block,
 * provided the tower is not at maximum height already and no dome is being built.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Hephaestus implements GodCard {
    private BoardPosition lastBuildPosition = null;
    private boolean hasBuiltOnce = false;

    /**
     * Activates or resets the effect of the Hephaestus god card at the start of the player's turn.
     * It ensures that the god card's state is correctly initialized when the player's turn starts.
     */
    @Override
    public void activateEffect(Game game) {
        // Reset the additional build flag and last build position at the start of the turn
        hasBuiltOnce = false;
        lastBuildPosition = null;
    }

    /**
     * Deactivates or clears the effect of the Hephaestus god card.
     * It is called at the end of the turn to clean up any state before the next player's turn.
     */
    @Override
    public void deactivateEffect(Game game) {
        // Clear the additional build flag and last build position at the end of the turn
        hasBuiltOnce = false;
        lastBuildPosition = null;
    }

    /**
     * Hephaestus does not modify move validation, so this method just uses the standard game rules for movement validation.
     *
     * @param worker The worker attempting to move.
     * @param from   The starting position of the move.
     * @param to     The target position of the move.
     * @param board  The game board.
     * @return true if the move is valid according to the standard game rules, false otherwise.
     */
    @Override
    public boolean modifyMoveValidation(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Since Hephaestus's power does not affect movement, use the standard game rules
        return board.isMoveLegal(worker, from, to);
    }

    /**
     * Modifies the building validation logic to implement Hephaestus's special building ability.
     * Allows an additional build on the same space if the first build was a block and not a dome.
     * <p>
     * This method determines if the build is valid by evaluating two scenarios:
     * 1. The first build, which follows the standard game rules.
     * 2. A potential second build, which must be on the same space as the first and cannot be a dome.
     *
     * @param worker        The worker performing the build.
     * @param buildPosition The position where the worker attempts to build.
     * @param board         The game board, providing context needed for the decision.
     * @return true if the build is valid according to Hephaestus's rules, false otherwise.
     */
    @Override
    public boolean modifyBuildValidation(Worker worker, BoardPosition buildPosition, Board board) {
        return isFirstBuildAllowed(worker, buildPosition, board) || isSecondBuildAllowed(buildPosition, board);
    }

    /**
     * Determines if the first build attempt by a worker is allowed.
     * The first build follows standard game rules without the special conditions imposed by Hephaestus.
     *
     * @param worker        The worker attempting the build.
     * @param buildPosition The board position where the build is attempted.
     * @param board         The game board, which provides necessary context for the build legality check.
     * @return true if the first build is allowed by standard game rules, false if it is the second build attempt or not allowed.
     */
    private boolean isFirstBuildAllowed(Worker worker, BoardPosition buildPosition, Board board) {
        // If no build has been executed, standard build rules apply
        return !hasBuiltOnce && board.isBuildLegal(worker, worker.getPosition(), buildPosition);
    }

    /**
     * Determines if a second build attempt by a worker is allowed under Hephaestus's god card rules.
     * The second build can only be performed on the same space as the first build and cannot involve placing a dome.
     * <p>
     * This method checks if:
     * - There has already been one build during this turn (hasBuiltOnce is true).
     * - The second build attempt is on the same position as the first build.
     * - The build does not attempt to place a dome (controlled by canPlaceAdditionalBlock).
     *
     * @param buildPosition The position where the second build is attempted.
     * @param board         The game board providing context for the build legality check.
     * @return true if all conditions for a second build are met, false otherwise.
     */
    private boolean isSecondBuildAllowed(BoardPosition buildPosition, Board board) {
        // Second build allowed only on the same position and not for placing a dome
        return hasBuiltOnce && lastBuildPosition.equals(buildPosition) && canPlaceAdditionalBlock(buildPosition, board);
    }

    /**
     * Checks if an additional block (not a dome) can be placed on the specified position.
     * This method is used to validate the conditions under which Hephaestus allows a second build on the same space.
     * The second build is only valid if the current level of the tower is less than 3, indicating that a block, not a dome, is being added.
     *
     * @param buildPosition The board position where the additional block is to be placed.
     * @param board         The game board, which provides the current building level at the specified position.
     * @return true if the building level is less than 3 (hence a block can be added), false if a dome would be placed.
     */
    private boolean canPlaceAdditionalBlock(BoardPosition buildPosition, Board board) {
        int currentLevel = board.getSquare(buildPosition).getBuildingLevel();
        return currentLevel < 3; // Ensure the block is not a dome
    }

    @Override
    public List<BoardPosition> modifyLegalMoves(Worker worker, List<BoardPosition> legalMoves, Board board) {
        return legalMoves;
    }

    @Override
    public List<BoardPosition> modifyLegalBuilds(Worker worker, List<BoardPosition> legalBuilds, Board board) {
        if (hasBuiltOnce) {
            // If the worker has already built once this turn,
            // restrict the legal builds to only the position of the last build
            // and only if adding another block is legal (i.e., not placing a dome).
            legalBuilds.removeIf(position -> !position.equals(lastBuildPosition) || board.getSquare(position).getBuildingLevel() >= 3);
        }
        return legalBuilds;
    }

    /**
     * Handles post-build actions specific to the Hephaestus god card. Depending on whether a build has already been
     * performed this turn, it either allows a second build on the same space or ends the player's turn.
     *
     * @param game          The game context in which this action is being executed.
     * @param worker        The worker that performed the build.
     * @param buildPosition The position on the board where the build occurred.
     */
    @Override
    public void postBuildExecution(Game game, Worker worker, BoardPosition buildPosition) {
        if (!hasBuiltOnce) {
            handleFirstBuild(game, buildPosition);
        } else {
            endBuildPhase(game);
        }
    }

    /**
     * Handles the first build action under Hephaestus's rules. If the build is on a level less than 3, it allows
     * for a second build on the same position by setting the game phase to SECOND_BUILD.
     *
     * @param game          The game instance.
     * @param buildPosition The position where the build occurred.
     */
    private void handleFirstBuild(Game game,BoardPosition buildPosition) {
        int currentLevel = game.getBoard().getSquare(buildPosition).getBuildingLevel();
        if (currentLevel < 3) {
            hasBuiltOnce = true;
            lastBuildPosition = buildPosition;
            game.setGamePhase(Game.GamePhase.SECOND_BUILD);
        } else {
            endBuildPhase(game);
        }
    }

    /**
     * Ends the building phase for the current player's turn, resetting the state and switching to the next player.
     * This method is called either when a second build is not possible or after a second build is completed.
     *
     * @param game The game instance.
     */
    private void endBuildPhase(Game game) {
        hasBuiltOnce = false;
        lastBuildPosition = null;
        game.switchTurn();
    }

    @Override
    public boolean preMoveExecution(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Hephaestus's power does not affect pre-move conditions.
        return true;
    }

    @Override
    public void postMoveExecution(Game game, Worker worker, BoardPosition from, BoardPosition to) {

    }

    @Override
    public void skipAction(Game game) {
        if (hasBuiltOnce) {
            hasBuiltOnce = false;
            lastBuildPosition = null;
            game.switchTurn();
            game.setGamePhase(Game.GamePhase.MOVE);
        }
    }

    @Override
    public boolean checkWinCondition(Worker worker, BoardPosition from, BoardPosition to, Board board) {
        // Hephaestus's ability does not affect winning conditions directly.
        return false;
    }

    /**
     * Returns the name of the god card, useful for debugging or displaying in the user interface.
     *
     * @return A string representing the name of the god card.
     */
    @Override
    public String getName() {
        return "Hephaestus";
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
     * Getter method for hasBuildOnce
     *
     * @return boolean hasBuiltOnce
     */
    public boolean getHasBuiltOnce() {
        return hasBuiltOnce;
    }

}

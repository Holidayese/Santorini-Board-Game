package org.example.godcards;

import org.example.*;

import java.util.List;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public interface GodCard {
    // Activates the effect of the god card for a particular game
    void activateEffect(Game game);
    // Optionally, reverses the effect when no longer needed or when game conditions change
    void deactivateEffect(Game game);

    // Method to adjust validation of moves based on the god card's rules
    boolean modifyMoveValidation(Worker worker, BoardPosition from, BoardPosition to, Board board);
    // Method to adjust validation of builds based on the god card's rules
    boolean modifyBuildValidation(Worker worker, BoardPosition buildPosition, Board board);

    // Method to handle the retrieval of legal moves.
    List<BoardPosition> modifyLegalMoves(Worker worker, List<BoardPosition> legalMoves, Board board);
    // Method to handle the retrieval of legal builds.
    List<BoardPosition> modifyLegalBuilds(Worker worker, List<BoardPosition> legalBuilds, Board board);

    // Method to deal with post build action
    void postBuildExecution(Game game, Worker worker, BoardPosition buildPosition);

    // Method to deal with pre move action
    boolean preMoveExecution(Worker worker, BoardPosition from, BoardPosition to, Board board);
    // Method to handle actions after a move is made.
    void postMoveExecution(Game game, Worker worker, BoardPosition from, BoardPosition to);

    // Method for handling skips
    void skipAction(Game game);

    // Method to check if meets win condition
    boolean checkWinCondition(Worker worker, BoardPosition from, BoardPosition to, Board board);

    // Returns the name of the god card, useful for debugging or UI purposes
    String getName();
}

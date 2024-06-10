package org.example;

import org.example.Exceptions.IllegalBuildException;
import org.example.godcards.Minotaur;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class MinotaurTest {
    private Game game;
    private Board board;
    private Player player1, player2;
    private Worker worker1, worker2;
    private Minotaur minotaur;

    @Before
    public void setUp() {
        player1 = new Player("A");
        player2 = new Player("B");
        worker1 = new Worker("A1", player1);
        worker2 = new Worker("B1", player2);
        game = new Game(player1, player2);
        board = game.getBoard();
        minotaur = new Minotaur();
        game.getGodCards().put(player1, minotaur);
    }

    @Test
    public void testMinotaurPushOpponentValid() {
        BoardPosition from = new BoardPosition(1, 1);
        BoardPosition to = new BoardPosition(1, 2);
        BoardPosition pushTo = new BoardPosition(1, 3);

        board.placeWorkerAt(worker1, from);
        board.placeWorkerAt(worker2, to);

        assertTrue("Minotaur should be able to push opponent", minotaur.modifyMoveValidation(worker1, from, to, board));
        assertTrue("Minotaur should execute push successfully", minotaur.preMoveExecution(worker1, from, to, board));
        assertNull("Original position should be empty", board.getSquare(to).getOccupiedWorker());
        assertEquals("Opponent should be pushed to the correct position", worker2, board.getSquare(pushTo).getOccupiedWorker());
    }

    @Test
    public void testMinotaurCannotPushOccupiedPosition() {
        board.getSquare(new BoardPosition(1, 3)).setOccupiedWorker(new Worker("Worker3", player2)); // Place another worker behind the target

        BoardPosition from = new BoardPosition(1, 1);
        BoardPosition to = new BoardPosition(1, 2);

        board.placeWorkerAt(worker1, from);
        board.placeWorkerAt(worker2, to);

        assertFalse("Minotaur should not be able to push opponent if space behind is occupied", minotaur.modifyMoveValidation(worker1, from, to, board));
    }

    @Test
    public void testMinotaurCannotPushOutOfBound() {
        BoardPosition from = new BoardPosition(4, 3);
        BoardPosition to = new BoardPosition(4, 4);

        board.placeWorkerAt(worker1, from);
        board.placeWorkerAt(worker2, to); // Place opponent worker at the edge of the board

        assertFalse("Minotaur should not be able to push opponent out of bounds", minotaur.modifyMoveValidation(worker1, from, to, board));
    }

    @Test
    public void testMinotaurCannotPushIntoDome() {
        BoardPosition from = new BoardPosition(1, 1);
        BoardPosition to = new BoardPosition(1, 2);
        BoardPosition behind = new BoardPosition(1, 2);

        board.placeWorkerAt(worker1, from);
        board.placeWorkerAt(worker2, to);
        board.getSquare(behind).buildBlock();
        board.getSquare(behind).buildBlock();
        board.getSquare(behind).buildBlock();
        board.getSquare(behind).buildBlock(); // Place a dome behind the target worker

        assertFalse("Minotaur should not be able to push opponent into a dome", minotaur.modifyMoveValidation(worker1, from, to, board));
    }

    @Test
    public void testMinotaurCannotPushOwnWorker() {
        Worker worker3 = new Worker("A2", player1);

        BoardPosition from = new BoardPosition(1, 1);
        BoardPosition to = new BoardPosition(1, 2);

        board.placeWorkerAt(worker1, from);
        board.placeWorkerAt(worker3, to); // Place same player's other worker ahead

        assertFalse("Minotaur should not be able to push own worker", minotaur.modifyMoveValidation(worker1, from, to, board));
    }

    @Test
    public void testMinotaurPushToThirdLevelDoesNotCountAsWin() {
        // Set up for pushing to third level
        BoardPosition behind = new BoardPosition(1, 3);
        board.getSquare(behind).buildBlock(); // First level
        board.getSquare(behind).buildBlock(); // Second level
        board.getSquare(behind).buildBlock(); // Third level

        BoardPosition from = new BoardPosition(1, 1);
        BoardPosition to = new BoardPosition(1, 2);

        board.placeWorkerAt(worker1, from);
        board.placeWorkerAt(worker2, to);

        assertTrue("Minotaur move should be valid for pushing", minotaur.modifyMoveValidation(worker1, from, to, board));
        assertTrue("Minotaur should execute push", minotaur.preMoveExecution(worker1, from, to, board));

        assertFalse("Pushing an opponent to the third level should not count as a win for Minotaur", minotaur.checkWinCondition(worker1, from, to, board));
    }
}

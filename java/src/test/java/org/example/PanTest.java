package org.example;

import org.example.Exceptions.IllegalBuildException;
import org.example.godcards.Pan;

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
public class PanTest {
    private Game game;
    private Board board;
    private Player player1, player2;
    private Worker worker1;
    private Pan pan;

    @Before
    public void setUp() {
        player1 = new Player("A");
        player2 = new Player("B");
        worker1 = new Worker("A1", player1);
        game = new Game(player1, player2);
        board = game.getBoard();
        pan = new Pan();
        game.getGodCards().put(player1, pan);
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));
    }

    @Test
    public void testPanWinConditionByDescending() {
        // Set up initial and target positions to simulate descending move
        BoardPosition initialPosition = new BoardPosition(2, 2);
        BoardPosition targetPosition = new BoardPosition(2, 3);

        // Simulate board levels
        board.getSquare(initialPosition).buildBlock();
        board.getSquare(initialPosition).buildBlock();
        board.getSquare(initialPosition).buildBlock(); // level 3
        board.getSquare(targetPosition).buildBlock(); // level 1

        board.placeWorkerAt(worker1, initialPosition);

        // Move the worker down two levels
        assertTrue("Move should be legal", pan.modifyMoveValidation(worker1, initialPosition, targetPosition, board));
        assertTrue("Pan should win by moving down two levels", pan.checkWinCondition(worker1, initialPosition, targetPosition, board));
    }

    @Test
    public void testPanNormalWin() {
        BoardPosition initialPosition = new BoardPosition(2, 2);
        BoardPosition targetPosition = new BoardPosition(2, 3);

        // Simulate normal level ground
        board.getSquare(initialPosition).buildBlock();
        board.getSquare(initialPosition).buildBlock(); // level 2
        board.getSquare(targetPosition).buildBlock();
        board.getSquare(targetPosition).buildBlock();
        board.getSquare(targetPosition).buildBlock(); // level 3

        board.placeWorkerAt(worker1, initialPosition);

        // Move the worker on the same level
        assertTrue("Move should be legal", pan.modifyMoveValidation(worker1, initialPosition, targetPosition, board));
        assertTrue("Pan should win by standard win condition", pan.checkWinCondition(worker1, initialPosition, targetPosition, board));
    }

}

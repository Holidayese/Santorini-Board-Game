package org.example;

import org.example.Exceptions.IllegalMoveException;
import org.example.godcards.Apollo;
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
public class ApolloTest {
    private Game game;
    private Board board;
    private Player player1, player2;
    private Worker worker1, worker2;
    private Apollo apollo;

    @Before
    public void setUp() {
        player1 = new Player("A");
        player2 = new Player("B");
        worker1 = new Worker("A1", player1);
        worker2 = new Worker("B1", player2);
        game = new Game(player1, player2);
        board = game.getBoard();
        apollo = new Apollo();
        game.getGodCards().put(player1, apollo);
    }

    @Test
    public void testApolloValidSwap() {
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));
        board.placeWorkerAt(worker2, new BoardPosition(1, 2));

        // Check if Apollo can swap places with an opponent worker
        assertTrue("Apollo should be able to swap places with an opponent worker", apollo.modifyMoveValidation(worker1, new BoardPosition(1, 1), new BoardPosition(1, 2), board));
        apollo.preMoveExecution(worker1, new BoardPosition(1, 1), new BoardPosition(1, 2), board);
        apollo.postMoveExecution(game, worker1, new BoardPosition(1, 1), new BoardPosition(1, 2));

        assertEquals("Worker1 should be at new position", new BoardPosition(1, 2), worker1.getPosition());
        assertEquals("Worker2 should be swapped to original position", new BoardPosition(1, 1), worker2.getPosition());
    }

    @Test
    public void testApolloInvalidSwapSameTeam() {
        Worker worker3 = new Worker("A2", player1);
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));
        board.placeWorkerAt(worker3, new BoardPosition(1, 2));

        // Apollo tries to swap with another worker of the same player
        assertFalse("Apollo should not be able to swap with own worker", apollo.modifyMoveValidation(worker1, new BoardPosition(1, 1), new BoardPosition(1, 2), board));
    }

    @Test
    public void testApolloInvalidSwapNonAdjacent() {
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));
        board.placeWorkerAt(worker2, new BoardPosition(2, 3));

        // Apollo tries to swap with a non-adjacent worker
        assertFalse("Apollo should not be able to swap with non-adjacent worker", apollo.modifyMoveValidation(worker1, new BoardPosition(1, 1), new BoardPosition(2, 3), board));
    }

    @Test
    public void testApolloInvalidSwapHigherLevel() {
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));
        board.placeWorkerAt(worker2, new BoardPosition(1, 2));
        board.getSquare(new BoardPosition(1, 2)).buildBlock();
        board.getSquare(new BoardPosition(1, 2)).buildBlock();  // Worker2 is now two levels higher than Worker1

        // Apollo tries to swap but the target is too high
        assertFalse("Apollo should not be able to swap into a higher level more than one above its own", apollo.modifyMoveValidation(worker1, new BoardPosition(1, 1), new BoardPosition(1, 2), board));
    }

    @Test
    public void testApolloSwapMaintainsNormalMoveAbility() {
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));

        // Apollo makes a normal move without swapping
        assertTrue("Apollo should be able to make normal moves", apollo.modifyMoveValidation(worker1, new BoardPosition(1, 1), new BoardPosition(1, 0), board));
    }
}

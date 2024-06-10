package org.example;

import org.example.Exceptions.IllegalBuildException;
import org.example.Exceptions.IllegalMoveException;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class BoardTest {
    private Board board;
    private Player player;
    private Player opponentPlayer;
    private Worker worker1;
    private Worker worker2;
    private Worker opponentWorker;

    @Before
    public void setUp() {
        board = new Board();
        player = new Player("A");
        opponentPlayer = new Player("B");
        worker1 = new Worker("A1", player);
        worker2 = new Worker("A2", player);
        opponentWorker = new Worker("B1", opponentPlayer);
    }

    // Initialize
    @Test
    public void testBoardInitialization() {
        assertNotNull("Board should be initialized", board);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                assertNotNull("Square should not be null", board.getSquare(new BoardPosition(i, j)));
                assertFalse("Square should initially be unoccupied", board.getSquare(new BoardPosition(i, j)).getIsOccupied());
            }
        }
    }

    // Place worker
    @Test
    public void testPlaceWorkerAtEmptySquare() {
        BoardPosition position = new BoardPosition(0, 0);
        assertTrue("Should be able to place worker on an empty square", board.placeWorkerAt(worker1, position));
        assertTrue("Square should be occupied after placing worker", board.getSquare(position).getIsOccupied());
    }

    @Test
    public void testPlaceWorkerOnOccupiedSquare() {
        board.placeWorkerAt(worker1, new BoardPosition(2, 2));
        assertFalse("Should not place worker on occupied square", board.placeWorkerAt(new Worker("Worker3", player), new BoardPosition(2, 2)));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testPlaceWorkerOutOfBounds() {
        board.placeWorkerAt(new Worker("Worker3", player), new BoardPosition(5, 5));
    }

    // Move worker
    @Test(expected = IllegalMoveException.class)
    public void testMoveWorkerToOccupiedSquare() throws IllegalMoveException {
        BoardPosition position1 = new BoardPosition(0, 0);
        BoardPosition position2 = new BoardPosition(0, 1);
        board.placeWorkerAt(worker1, position1);
        Worker worker2 = new Worker("A2", player);
        board.placeWorkerAt(worker2, position2);
        board.moveWorkerTo(worker1, position2); // Should throw exception
    }

    @Test(expected = IllegalMoveException.class)
    public void testMoveWorkerNonAdjacent() throws IllegalMoveException {
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));
        board.moveWorkerTo(worker1, new BoardPosition(4, 4)); // Non-adjacent move
    }

    @Test
    public void testLegalMove() throws IllegalMoveException {
        BoardPosition start = new BoardPosition(2, 2);
        BoardPosition end = new BoardPosition(3, 2);
        board.placeWorkerAt(worker1, start);
        assertTrue("Move should be legal", board.moveWorkerTo(worker1, end));
    }

    // Build block
    @Test
    public void testBuildAtMaxLevel() throws IllegalBuildException {
        BoardPosition position = new BoardPosition(3, 3);
        board.placeWorkerAt(worker1, new BoardPosition(3, 2));
        // Simulate building to max level
        for (int i = 0; i < 3; i++) {
            board.buildAt(worker1, position);
        }
        assertTrue("Move should succeed.", board.buildAt(worker1, position));
    }

    // Build dome
    @Test
    public void testBuildDomeAtThirdLevel() throws IllegalBuildException {
        BoardPosition position = new BoardPosition(4, 4);
        board.placeWorkerAt(worker1, new BoardPosition(4, 3));
        // Build to third level
        for (int i = 0; i < 3; i++) {
            board.buildAt(worker1, position);
        }
        // Now try to place a dome
        assertTrue("Should place a dome", board.buildAt(worker1, position));
    }

    @Test(expected = IllegalBuildException.class)
    public void testBuildOnDome() throws IllegalBuildException {
        BoardPosition pos = new BoardPosition(0, 0);
        board.getSquare(pos).buildBlock(); // Level 1
        board.getSquare(pos).buildBlock(); // Level 2
        board.getSquare(pos).buildBlock(); // Level 3
        board.getSquare(pos).placeDome();
        board.placeWorkerAt(worker1, new BoardPosition(0, 1));
        board.buildAt(worker1, pos);
    }

    // Push opponent worker
    @Test
    public void testPushOpponentWorker_Valid() {
        board.placeWorkerAt(worker1, new BoardPosition(2, 2));
        board.placeWorkerAt(opponentWorker, new BoardPosition(2, 3));
        assertTrue("Should successfully push the opponent worker", board.pushOpponentWorker(opponentWorker, new BoardPosition(2, 4)));
        assertNull("Old position should be empty", board.getSquare(new BoardPosition(2, 3)).getOccupiedWorker());
        assertEquals("New position should be occupied by opponent worker", opponentWorker, board.getSquare(new BoardPosition(2, 4)).getOccupiedWorker());
    }

    @Test
    public void testPushOpponentWorker_Invalid_OutOfBounds() {
        board.placeWorkerAt(worker1, new BoardPosition(2, 2));
        board.placeWorkerAt(opponentWorker, new BoardPosition(2, 3));
        assertFalse("Should fail to push out of bounds", board.pushOpponentWorker(opponentWorker, new BoardPosition(5, 5)));
    }

    @Test
    public void testPushOpponentWorker_Invalid_Occupied() {
        board.placeWorkerAt(worker1, new BoardPosition(2, 2));
        board.placeWorkerAt(opponentWorker, new BoardPosition(2, 3));
        board.placeWorkerAt(worker2, new BoardPosition(2, 4)); // Place another worker where we want to push
        assertFalse("Should fail to push into occupied square", board.pushOpponentWorker(opponentWorker, new BoardPosition(2, 4)));
    }

    @Test
    public void testPushOpponentWorker_Invalid_HasDome() throws IllegalBuildException {
        board.placeWorkerAt(worker1, new BoardPosition(2, 2));
        board.placeWorkerAt(opponentWorker, new BoardPosition(2, 3));
        BoardPosition pushToPosition = new BoardPosition(2, 4);
        // Place a dome at the target position
        board.buildAt(opponentWorker, pushToPosition); // Level 1
        board.buildAt(opponentWorker, pushToPosition); // Level 2
        board.buildAt(opponentWorker, pushToPosition); // Level 3
        board.buildAt(opponentWorker, pushToPosition); // Place a dome
        assertFalse("Should fail to push into a square with a dome", board.pushOpponentWorker(opponentWorker, new BoardPosition(2, 4)));
    }

    // Update worker position
    @Test
    public void testUpdateWorkerPosition() {
        BoardPosition oldPosition = new BoardPosition(1, 0);
        board.placeWorkerAt(opponentWorker, oldPosition);
        BoardPosition newPosition = new BoardPosition(1, 1);
        board.updateWorkerPosition(opponentWorker, newPosition);
        assertFalse("Old position should be empty", board.getSquare(oldPosition).getIsOccupied());
        assertTrue("New position should be occupied", board.getSquare(newPosition).getIsOccupied());
        assertEquals("Worker should be in new position", opponentWorker, board.getSquare(newPosition).getOccupiedWorker());
    }

    // Win by climb from level 2 to level 3 by its own
    @Test
    public void testHasWorkerClimbedToThirdLevelByItsOwn_Success() {
        // Simulate climbing from second to third level
        BoardPosition from = new BoardPosition(1, 1);
        BoardPosition to = new BoardPosition(1, 2);
        board.getSquare(from).buildBlock(); // from: level 1
        board.getSquare(from).buildBlock(); // from: level 2
        board.getSquare(to).buildBlock(); // to: level 1
        board.getSquare(to).buildBlock(); // to: level 2
        board.getSquare(to).buildBlock(); // to: level 3
        board.placeWorkerAt(worker1, from);
        assertTrue("Should detect climb from second to third level", board.hasWorkerClimbToThirdLevelByItsOwn(worker1, from, to));
    }

    @Test
    public void testHasWorkerClimbedToThirdLevelByItsOwn_Failure() {
        // Simulate no climbing just moving on same level
        BoardPosition from = new BoardPosition(1, 1);
        BoardPosition to = new BoardPosition(1, 2);
        board.getSquare(from).buildBlock(); // First level
        board.getSquare(to).buildBlock(); // First level
        board.placeWorkerAt(worker1, from);
        assertFalse("Should not detect climb to third level if not climbing", board.hasWorkerClimbToThirdLevelByItsOwn(worker1, from, to));
    }

    @Test
    public void testPushToThirdLevelDoesNotCountAsClimb() {
        BoardPosition oldPosition = new BoardPosition(1, 0);
        board.placeWorkerAt(opponentWorker, oldPosition);
        // Build up to the third level at a nearby position
        BoardPosition targetPosition = new BoardPosition(1, 1);
        board.getSquare(targetPosition).buildBlock(); // First level
        board.getSquare(targetPosition).buildBlock(); // Second level
        board.getSquare(targetPosition).buildBlock(); // Third level

        // Push opponent worker onto the third level
        board.pushOpponentWorker(opponentWorker, targetPosition);

        // Check if the push is considered as climbing by the worker's own move
        assertFalse("Pushing to third level should not count as self-climbing",
                board.hasWorkerClimbToThirdLevelByItsOwn(opponentWorker, oldPosition, targetPosition));
    }
}


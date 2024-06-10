package org.example;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class GameTest {
    private Game game;
    private Player player1, player2;
    private Worker worker1, worker2;

    @Before
    public void setUp() {
        player1 = new Player("A");
        player2 = new Player("B");

        game = new Game(player1, player2);

        worker1 = new Worker("A1", player1);
        worker2 = new Worker("A2", player1);
    }

    // Initialize
    @Test
    public void testInitialization() {
        assertEquals("Game should start in INITIALIZE phase", Game.GamePhase.INITIALIZE, game.getGamePhase());
        assertNotNull("Game board should be initialized", game.getBoard());
        assertEquals("First player should be Player1", "A", game.getCurrentPlayer().getPlayerID());
    }

    // Select god card
    @Test
    public void testGodCardAssignmentAndEffect() {
        game.selectGodCard(player1, "Demeter");
        assertNotNull("Player should have a god card assigned", game.getGodCardForPlayer(player1));
    }

    // Place worker
    @Test
    public void testPlaceWorkerSuccess() {
        BoardPosition position = new BoardPosition(0, 0);
        assertTrue("Should successfully place worker", game.placeWorker(worker1, position));
        assertEquals("Game should transition to PLACE_WORKER phase", Game.GamePhase.PLACE_WORKER, game.getGamePhase());
        assertEquals("Worker should be at position (0,0)", position, worker1.getPosition());
    }

    @Test
    public void testPlaceWorkerFailureOutOfPhase() {
        game.setGamePhase(Game.GamePhase.MOVE); // Incorrect phase for placing workers
        BoardPosition position = new BoardPosition(1, 1);
        assertFalse("Should not place worker out of phase", game.placeWorker(worker1, position));
    }

    // Move
    @Test
    public void testMoveWorkerLegal() {
        game.setGamePhase(Game.GamePhase.MOVE);
        game.setPlayerAction(Game.PlayerAction.MOVE);
        game.setCurrentWorker(worker1);
        worker1.setPosition(new BoardPosition(0, 0)); // Set initial position
        BoardPosition newPosition = new BoardPosition(0, 1);
        assertTrue("Should allow legal move", game.moveWorker(newPosition));
        assertEquals("Worker should move to the new position", newPosition, worker1.getPosition());
    }

    // Build
    @Test
    public void testBuildBlockLegal() {
        game.setGamePhase(Game.GamePhase.BUILD);
        game.setPlayerAction(Game.PlayerAction.BUILD);
        game.setCurrentWorker(worker1);
        worker1.setPosition(new BoardPosition(0, 0)); // Set position for building
        BoardPosition buildPosition = new BoardPosition(0, 1);
        assertTrue("Should allow legal build", game.buildBlock(buildPosition));
        assertTrue("Build position should have increased level", game.getBoard().getSquare(buildPosition).getBuildingLevel() > 0);
    }

    // win condition
    @Test
    public void testWinCondition() {
        game.setGamePhase(Game.GamePhase.MOVE);
        game.setPlayerAction(Game.PlayerAction.MOVE);
        game.setCurrentWorker(worker1);
        BoardPosition from = new BoardPosition(0, 0);
        game.getBoard().getSquare(from).buildBlock();
        game.getBoard().getSquare(from).buildBlock();
        worker1.setPosition(from); // Assume position 0,0 is at level 2

        BoardPosition to = new BoardPosition(0, 1);
        game.getBoard().getSquare(to).buildBlock();
        game.getBoard().getSquare(to).buildBlock();
        game.getBoard().getSquare(to).buildBlock(); // Build third level
        game.moveWorker(to); // Move to third level
        assertEquals("Player should win if moving to third level", player1.getPlayerID(), game.getWinnerID());
    }


}


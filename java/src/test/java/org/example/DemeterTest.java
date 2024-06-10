package org.example;

import org.example.godcards.Demeter;
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
public class DemeterTest {
    private Game game;
    private Board board;
    private Player player1, player2;
    private Worker worker1;
    private Demeter demeter;

    @Before
    public void setUp() {
        player1 = new Player("A");
        player2 = new Player("B");
        worker1 = new Worker("A1", player1);
        game = new Game(player1, player2);
        board = game.getBoard();
        demeter = new Demeter();
        game.getGodCards().put(player1, demeter);
    }

    @Test
    public void testDemeterFirstBuildAllowsSecondBuild() {
        BoardPosition firstBuildPosition = new BoardPosition(0, 0);
        BoardPosition secondBuildPosition = new BoardPosition(0, 1);

        // Assume worker is at position (1, 0)
        worker1.setPosition(new BoardPosition(1, 0));
        board.placeWorkerAt(worker1, new BoardPosition(1, 0));

        // Activate Demeter effect
        demeter.activateEffect(game);
        assertTrue("First build should be allowed", demeter.modifyBuildValidation(worker1, firstBuildPosition, board));
        demeter.postBuildExecution(game, worker1, firstBuildPosition);

        // Verify entering second build phase
        assertEquals("Should be in SECOND_BUILD phase", Game.GamePhase.SECOND_BUILD, game.getGamePhase());

        // Second build should be allowed on a different position
        assertTrue("Second build should be allowed on a different space", demeter.modifyBuildValidation(worker1, secondBuildPosition, board));
    }

    @Test
    public void testDemeterCannotBuildTwiceOnSameSpace() {
        BoardPosition buildPosition = new BoardPosition(0, 0);

        // Assume worker is at position (1, 0)
        worker1.setPosition(new BoardPosition(1, 0));
        board.placeWorkerAt(worker1, new BoardPosition(1, 0));

        // Activate Demeter effect
        demeter.activateEffect(game);
        assertTrue("First build should be allowed", demeter.modifyBuildValidation(worker1, buildPosition, board));
        demeter.postBuildExecution(game, worker1, buildPosition);

        // Second build should not be allowed on the same space
        assertFalse("Second build should not be allowed on the same space", demeter.modifyBuildValidation(worker1, buildPosition, board));
    }

    @Test
    public void testDemeterResetsAfterTurn() {
        BoardPosition buildPosition = new BoardPosition(0, 0);

        // First build and turn completion
        board.placeWorkerAt(worker1, new BoardPosition(1, 0));

        demeter.activateEffect(game);
        assertTrue("First build should be allowed", demeter.modifyBuildValidation(worker1, buildPosition, board));
        demeter.postBuildExecution(game, worker1, buildPosition);
        demeter.deactivateEffect(game);

        // Ensure effects are reset
        assertNull("Last build position should be reset", demeter.getLastBuildPosition());
        assertFalse("Should not have built once flag set", demeter.getHasBuiltOnce());
    }

    @Test
    public void testSkipSecondBuild() {
        BoardPosition firstBuildPosition = new BoardPosition(0, 0);
        board.placeWorkerAt(worker1, new BoardPosition(1, 0));

        // Activate Demeter effect
        demeter.activateEffect(game);
        assertTrue("First build should be allowed", demeter.modifyBuildValidation(worker1, firstBuildPosition, board));
        demeter.postBuildExecution(game, worker1, firstBuildPosition);

        // Ensure the game phase is correctly set for a second build
        assertEquals("Game should be in SECOND_BUILD phase", Game.GamePhase.SECOND_BUILD, game.getGamePhase());

        // Skip the second build
        demeter.skipAction(game);

        // Check that the game phase is reset to MOVE for the next turn
        assertEquals("Game should return to MOVE phase", Game.GamePhase.MOVE, game.getGamePhase());
        // Ensure the turn is switched to the next player
        assertEquals("Turn should switch to the next player", player2, game.getCurrentPlayer());
        // Verify Demeter's internal state is reset
        assertNull("Demeter's last build position should be reset", demeter.getLastBuildPosition());
        assertFalse("Demeter's hasBuiltOnce should be reset", demeter.getHasBuiltOnce());
    }

}

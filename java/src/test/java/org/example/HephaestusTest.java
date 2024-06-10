package org.example;

import org.example.Exceptions.IllegalBuildException;
import org.example.godcards.Hephaestus;

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
public class HephaestusTest {
    private Game game;
    private Board board;
    private Player player1, player2;
    private Worker worker1;
    private Hephaestus hephaestus;

    @Before
    public void setUp() {
        player1 = new Player("A");
        player2 = new Player("B");
        worker1 = new Worker("A1", player1);
        game = new Game(player1, player2);
        board = game.getBoard();
        hephaestus = new Hephaestus();
        game.getGodCards().put(player1, hephaestus);
    }

    @Test
    public void testAllowSecondBuildOnSameBlock() throws IllegalBuildException {
        BoardPosition buildPosition = new BoardPosition(1, 0);

        // Activate Hephaestus effect
        hephaestus.activateEffect(game);
        board.placeWorkerAt(worker1, new BoardPosition(0, 0));
        board.buildAt(worker1, buildPosition); // First build
        hephaestus.postBuildExecution(game, worker1, buildPosition);

        // Attempt second build on the same position
        assertTrue("Second build should be allowed", hephaestus.modifyBuildValidation(worker1, buildPosition, board));
        assertEquals("Game should remain in SECOND_BUILD phase for a second block", Game.GamePhase.SECOND_BUILD, game.getGamePhase());
    }

    @Test
    public void testPreventSecondBuildAfterDome() throws IllegalBuildException {
        BoardPosition buildPosition = new BoardPosition(0, 0);
        board.placeWorkerAt(worker1, new BoardPosition(1, 0));

        // Build up to level 3 and place dome
        board.getSquare(buildPosition).buildBlock(); // Level 1
        board.getSquare(buildPosition).buildBlock(); // Level 2
        board.getSquare(buildPosition).buildBlock(); // Level 3
        assertTrue("First build (dome) should succeed", board.buildAt(worker1, buildPosition));
        hephaestus.postBuildExecution(game, worker1, buildPosition);

        // Attempt second build (placing another dome should be blocked)
        assertFalse("Second build should not be allowed after a dome", hephaestus.modifyBuildValidation(worker1, buildPosition, board));
    }

    @Test
    public void testPreventAnotherDomeOnSecondBuild() throws IllegalBuildException {
        BoardPosition buildPosition = new BoardPosition(0, 0);

        // Simulate building to third level
        board.getSquare(buildPosition).buildBlock(); // Level 1
        board.getSquare(buildPosition).buildBlock(); // Level 2
        board.getSquare(buildPosition).buildBlock(); // Level 3

        // Activate Hephaestus and perform first build (attempt to place dome)
        hephaestus.activateEffect(game);
        board.placeWorkerAt(worker1, new BoardPosition(1, 0));
        board.buildAt(worker1, buildPosition); // first dome
        hephaestus.postBuildExecution(game, worker1, buildPosition);

        // Attempt second build (placing dome)
        assertFalse("Should not allow a dome on second build", hephaestus.modifyBuildValidation(worker1, buildPosition, board));
        assertEquals("Should switch game phase away from SECOND_BUILD after dome attempt", Game.GamePhase.MOVE, game.getGamePhase());
    }

    @Test
    public void testHephaestusSecondBuildSkipped() throws IllegalBuildException {
        BoardPosition buildPosition = new BoardPosition(0, 0);

        // Activate Hephaestus effect
        hephaestus.activateEffect(game);
        board.placeWorkerAt(worker1, new BoardPosition(1, 1));
        board.buildAt(worker1, buildPosition); // First build
        hephaestus.postBuildExecution(game, worker1, buildPosition);

        // Skip the second build
        hephaestus.skipAction(game);

        // Ensure game phase is correctly reset
        assertEquals("Game phase should be reset to MOVE after skipping", Game.GamePhase.MOVE, game.getGamePhase());
        assertFalse("Should have reset the Hephaestus built-once flag", hephaestus.getHasBuiltOnce());
        assertNull("Last build position should be reset", hephaestus.getLastBuildPosition());
    }
}

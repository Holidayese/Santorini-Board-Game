package org.example;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class PlayerTest {
    private Player player;
    private final String name = "Alice";
    private Worker worker1;
    private Worker worker2;

    @Before
    public void setUp() {
        player = new Player(name);
        worker1 = new Worker("worker1", player);
        worker2 = new Worker("worker2", player);
    }

    @Test
    public void testPlayerInitializationWithName() {
        assertEquals("Player name should match the initialization value.", name, player.getPlayerID());
    }

    @Test
    public void testPlayerInitializationWithWorkers() {
        List<Worker> workers = Arrays.asList(worker1, worker2);
        player.setWorkers(workers);

        assertNotNull(workers);
        assertEquals("Player number should be two.", 2, workers.size());

        // Check that workers are correctly associated with the player
        assertTrue("All workers should belong to the player.",
                workers.stream().allMatch(worker -> worker.getOwnerID() == player.getPlayerID()));
        // Check worker IDs for uniqueness and correct format
        assertTrue("Should have a worker named Worker1.",
                workers.stream().anyMatch(worker -> worker.getWorkerID().equals("worker1")));
        assertTrue("Should have a worker named Worker2.",
                workers.stream().anyMatch(worker -> worker.getWorkerID().equals("worker2")));
    }

    @Test
    public void testSetWorkers() {
        List<Worker> workers = Arrays.asList(worker1, worker2);
        player.setWorkers(workers);
        List<Worker> currentWorkers = player.getWorkers();

        assertEquals("Player's workers should be updated to the workers list.", workers, currentWorkers);
    }
}


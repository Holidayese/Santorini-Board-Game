package org.example;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class WorkerTest {
    private Player player;
    private Worker worker;
    private final String workerID = "Worker1";
    private final BoardPosition initialPosition = new BoardPosition(0, 0);

    @Before
    public void setUp() {
        player = new Player("Alice");
        worker = new Worker(workerID, player);
    }

    @Test
    public void testWorkerInitialization() {
        assertEquals("Worker ID should match the initialization value.", workerID, worker.getWorkerID());
        assertNull(worker.getPosition());
    }

    @Test
    public void testSetPosition() {
        worker.setPosition(initialPosition);
        assertTrue("Worker's position should be updated to the new value.", initialPosition.equals(worker.getPosition()));
    }

    @Test
    public void testWorkerOwnership() {
        assertEquals("Worker's owner should have the correct name.", "Alice", worker.getOwnerID());
    }

}

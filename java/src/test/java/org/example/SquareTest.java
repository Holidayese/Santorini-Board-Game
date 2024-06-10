package org.example;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Andrew ID: luw2
 *
 * @author Lu Wang
 */
public class SquareTest {
    private Square square;
    private Worker worker;

    @Before
    public void setUp() {
        square = new Square();
        worker = new Worker("Worker1", new Player("Alice"));
    }

    @Test
    public void testOccupancyWhenOccupied() {
        square.setOccupiedWorker(worker);
        assertTrue("Square should be marked as occupied.", square.getIsOccupied());
        assertEquals("Occupied worker should match the one set.", worker, square.getOccupiedWorker());
    }

    @Test
    public void testOccupancyWhenVacated() {
        square.setOccupiedWorker(worker);
        square.setOccupiedWorker(null);
        assertFalse("Square should be marked as not occupied after being vacated.", square.getIsOccupied());
        assertNull("Occupied worker should be null after being vacated.", square.getOccupiedWorker());
    }

    @Test
    public void testBuildBlockIncreasesLevel() {
        assertTrue("Building first level should succeed.", square.buildBlock()); // level 1
        assertEquals("Square should have 1 level after building once.", 1, square.getBuildingLevel());

        assertTrue("Building second level should succeed.", square.buildBlock()); // level 2
        assertEquals("Square should have 2 levels after building twice.", 2, square.getBuildingLevel());

        assertTrue("Building third level should succeed.", square.buildBlock()); // level 3
        assertEquals("Square should have 3 levels after building thrice.", 3, square.getBuildingLevel());
    }

    @Test
    public void testBuildBlockWhenAlreadyHaThreeLevels() {
        square.buildBlock();
        square.buildBlock();
        square.buildBlock(); // Square now has 3 levels.

        // Attempt to build a fourth level should fail
        assertFalse("Building a block on a square with three levels should fail.", square.buildBlock());
        assertEquals("Square should still have 3 levels after failed build attempt.", 3, square.getBuildingLevel());
    }

    @Test
    public void testBuildBlockWhenAlreadyHasDome() {
        square.buildBlock();
        square.buildBlock();
        square.buildBlock(); // Square now has 3 levels.
        square.placeDome(); // Square now has a dome

        // Attempt to build after dome is placed should fail
        assertFalse("Building a block on a square with a dome should fail.", square.buildBlock());
        assertEquals("Square should have 3 levels after dome is placed.", 3, square.getBuildingLevel());
        assertTrue("Square should still have a dome after failed build attempt.", square.hasDome());
    }

    @Test
    public void testPlaceDomeSucceedsAfterThreeLevelsBuilt() {
        square.buildBlock();
        square.buildBlock();
        square.buildBlock(); // Square now has 3 levels.

        assertTrue("Placing dome should succeed after building 3 levels.", square.placeDome());
        assertTrue("Square should have a dome after placing it.", square.hasDome());
    }

    @Test
    public void testPlaceDomeFailsWhenNotEnoughLevels() {
        assertFalse("Placing dome should fail without 3 levels built.", square.placeDome());
    }

    @Test
    public void testPlaceDomeFailsWhenAlreadyHasDome() {
        square.buildBlock();
        square.buildBlock();
        square.buildBlock(); // Square now has 3 levels.
        square.placeDome(); // Square now has a dome

        assertFalse("Cannot place a dome when already has a dome.", square.placeDome());
    }
}


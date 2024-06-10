package org.example;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class TowerTest {
    private Tower tower;

    @Before
    public void setUp() {
        tower = new Tower();
    }

    @Test
    public void testBuildLevelWhenLevelBelowThree() {
        assertTrue("Building first level should succeed.", tower.buildLevel()); // level 1
        assertEquals(1, tower.getLevels());

        assertTrue("Building second level should succeed.", tower.buildLevel()); // level 2
        assertEquals(2, tower.getLevels());

        assertTrue("Building third level should succeed.", tower.buildLevel()); // level 3
        assertEquals(3, tower.getLevels());
    }

    @Test
    public void testBuildLevelFailsWhenThreeLevelsBuilt() {
        tower.buildLevel();
        tower.buildLevel();
        tower.buildLevel(); // Tower now has 3 levels.

        assertFalse("Building fourth level should fail.", tower.buildLevel());
    }

    @Test
    public void placeDomeSucceedsAfterThreeLevelsBuilt() {
        tower.buildLevel();
        tower.buildLevel();
        tower.buildLevel(); // Tower now has 3 levels.

        assertTrue("Placing dome should succeed after building 3 levels.", tower.placeDome());
        assertTrue("Tower should have a dome after placing it.", tower.getHasDome());
    }

    @Test
    public void placeDomeFailsWhenNotEnoughLevels() {
        assertFalse("Placing dome should fail without 3 levels built.", tower.placeDome());
    }

    @Test
    public void placeDomeFailsWhenDomeAlreadyPlaced() {
        tower.buildLevel();
        tower.buildLevel();
        tower.buildLevel(); // Tower now has 3 levels.
        tower.placeDome(); // Place dome successfully.

        assertFalse("Placing another dome should fail.", tower.placeDome());
    }
}


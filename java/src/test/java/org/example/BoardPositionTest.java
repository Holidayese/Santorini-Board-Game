package org.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class BoardPositionTest {
    private BoardPosition centerPosition;

    @Before
    public void setUp() {
        centerPosition = new BoardPosition(1, 1);
    }

    @Test
    public void testIsAdjacentTrueForDirectlyAdjacentPosition() {
        BoardPosition abovePosition = new BoardPosition(0, 1);
        BoardPosition belowPosition = new BoardPosition(2, 1);
        BoardPosition leftPosition = new BoardPosition(1, 0);
        BoardPosition rightPosition = new BoardPosition(1, 2);
        assertTrue("Position directly above should be considered adjacent.", abovePosition.isAdjacentTo(centerPosition));
        assertTrue("Position directly below should be considered adjacent.", belowPosition.isAdjacentTo(centerPosition));
        assertTrue("Position directly left should be considered adjacent.", leftPosition.isAdjacentTo(centerPosition));
        assertTrue("Position directly right should be considered adjacent.", rightPosition.isAdjacentTo(centerPosition));
    }

    @Test
    public void testIsAdjacentTrueForDiagonallyPosition() {
        BoardPosition diagonalPosition1 = new BoardPosition(0, 0); // upper left
        BoardPosition diagonalPosition2 = new BoardPosition(2, 2); // lower right
        BoardPosition diagonalPosition3 = new BoardPosition(0, 2); // upper right
        BoardPosition diagonalPosition4 = new BoardPosition(2, 0); // lower left
        assertTrue("Position upper left above should be considered adjacent.", diagonalPosition1.isAdjacentTo(centerPosition));
        assertTrue("Position lower right above should be considered adjacent.", diagonalPosition2.isAdjacentTo(centerPosition));
        assertTrue("Position upper right above should be considered adjacent.", diagonalPosition3.isAdjacentTo(centerPosition));
        assertTrue("Position lower left above should be considered adjacent.", diagonalPosition4.isAdjacentTo(centerPosition));
    }

    @Test
    public void testIsAdjacentFalseForSamePosition() {
        BoardPosition samePosition = new BoardPosition(1, 1);
        assertFalse("A position should not be considered adjacent to itself.", samePosition.isAdjacentTo(centerPosition));
    }

    @Test
    public void testIsAdjacentToFalseForNonAdjacentPositions() {
        BoardPosition farPosition = new BoardPosition(3, 3);
        assertFalse("Position that too far should not be considered adjacent.", farPosition.isAdjacentTo(centerPosition));
    }

    @Test
    public void testSamePosition() {
        BoardPosition position1 = new BoardPosition(1, 1);
        BoardPosition position2 = new BoardPosition(2, 2);

        assertTrue("Positions with the same coordinates should be equal.", position1.equals(centerPosition));
        assertFalse("Positions with different coordinates should not be equal.", position1.equals(position2));
    }

}


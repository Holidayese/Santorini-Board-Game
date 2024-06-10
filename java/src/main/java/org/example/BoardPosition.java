package org.example;

import java.util.Objects;

/**
 * Represents a position on the Santorini game board.
 * Each position is defined by its x (column) and y (row) coordinates.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class BoardPosition {
    // The x-coordinate (column) of the board position.
    private final int x;
    // The y-coordinate (row) of the board position.
    private final int y;

    /**
     * Constructs a new BoardPosition with specified x and y coordinates.
     *
     * @param x The x coordinate (column) of the position.
     * @param y The y coordinate (row) of the position.
     */
    public BoardPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x coordinate (column) of this position.
     *
     * @return The x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y coordinate (row) of this position.
     *
     * @return The y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Checks if this position is adjacent to another position.
     * Two positions are considered adjacent if they are in each other's immediate nine-square box
     * (including diagonally adjacent squares) but not the same square.
     *
     * @param other The other BoardPosition to compare against.
     * @return true if the two positions are adjacent; false otherwise.
     */
    public boolean isAdjacentTo(BoardPosition other) {
        if (other == null) {
            return false;
        }
        return Math.abs(this.x - other.x) <= 1 && Math.abs(this.y - other.y) <= 1 && !this.equals(other);
    }

    /**
     * Determines whether this BoardPosition is equal to another object.
     * Equality is defined by matching x and y coordinates.
     * Overrides the Object class's equals method to ensure correct behavior in collections, etc.
     *
     * @param o The object to compare this BoardPosition against.
     * @return true if the object is a BoardPosition with the same x and y coordinates; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardPosition that = (BoardPosition) o;
        return x == that.x && y == that.y;
    }

    /**
     * Generates a hash code for this BoardPosition.
     * This is essential for the correct functioning of hash-based collections, such as HashMap.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Returns a string representation of this BoardPosition.
     * This is helpful for debugging and logging, providing a human-readable format of the position.
     *
     * @return A string in the format "[x, y]" representing this position's coordinates.
     */
    @Override
    public String toString() {
        return "[" + x + ", " + y + ']';
    }
}


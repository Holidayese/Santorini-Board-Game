package org.example;

/**
 * Represents a single square on the Santorini game board.
 * Each square can hold a tower and may be occupied by a worker.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Square {
    private Worker occupiedWorker;
    private Tower tower;

    /**
     * Initializes a new Square instance without a worker and with a new tower.
     */
    public Square() {
        this.tower = new Tower();
    }

    /**
     * Sets or clears the worker occupying this square.
     *
     * @param occupiedWorker The worker to occupy the square. Setting this to null marks the square as unoccupied.
     */
    public void setOccupiedWorker(Worker occupiedWorker) {
        this.occupiedWorker = occupiedWorker;
    }

    /**
     * Retrieves the worker currently occupying the square, if any.
     *
     * @return The Worker occupying the square, or null if the square is unoccupied.
     */
    public Worker getOccupiedWorker() {
        return occupiedWorker;
    }


    /**
     * Checks whether the square is currently occupied by a worker.
     *
     * @return true if the square is occupied, false otherwise.
     */
    public boolean getIsOccupied() {
        return getOccupiedWorker() != null;
    }

    /**
     * Retrieves the current building level of the tower on this square.
     *
     * @return An int representing the number of levels built on the tower.
     */
    public int getBuildingLevel() {
        return tower.getLevels();
    }

    /**
     * Attempts to add a level to the tower on this square.
     *
     * @return true if a level was successfully added, indicating the tower had less than 3 levels and no dome; false otherwise.
     */
    public boolean buildBlock() {
        return tower.buildLevel();
    }

    /**
     * Attempts to place a dome on the tower on this square.
     *
     * @return true if the dome was successfully placed, requiring the tower to have exactly 3 levels and no existing dome; false otherwise.
     */
    public boolean placeDome() {
        return tower.placeDome();
    }

    /**
     * Checks if the tower on this square is topped with a dome.
     *
     * @return true if the tower has a dome, false otherwise.
     */
    public boolean hasDome() {
        return tower != null && tower.getHasDome();
    }
}


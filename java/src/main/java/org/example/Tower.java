package org.example;

/**
 * Represents a tower in the board game Santorini.
 * A tower can have up to three levels and may be topped with a dome.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Tower {
    private int levels;
    private boolean hasDome;
    private static final int MAX_LEVELS = 3;

    /**
     * Initializes a new Tower instance with no levels and no dome.
     */
    public Tower() {
        this.levels = 0;
        this.hasDome = false;
    }

    /**
     * Attempts to add a level to the tower.
     *
     * @return true if a level was successfully added, false otherwise
     */
    public boolean buildLevel() {
        if (!hasDome && levels < MAX_LEVELS) {
            levels++;
            return true;
        } else if (!hasDome && levels == MAX_LEVELS) {
            System.out.println("Already has three levels. Can only place a dome.");
        } else {
            System.out.println("Already has three levels and a dome. The tower is complete.");
        }
        return false;
    }

    /**
     * Attempts to place a dome on the tower.
     *
     * @return true if the dome was successfully placed, false otherwise.
     */
    public boolean placeDome() {
        if (!hasDome) {
            if (levels == MAX_LEVELS) {
                hasDome = true;
                System.out.println("Dome placed successfully.");
                return true;
            } else if (levels < MAX_LEVELS) {
                System.out.println("Not enough levels to place a dome.");
            }
        } else {
            System.out.println("Already has a dome. The tower is complete.");
        }
        return false;
    }

    /**
     * Retrieves the current number of levels in the tower.
     *
     * @return The number of levels (0 to 3).
     */
    public int getLevels() {
        return levels;
    }


    /**
     * Checks if the tower is topped with a dome.
     *
     * @return true if the tower has a dome, false otherwise.
     */
    public boolean getHasDome() {
        return hasDome;
    }

}

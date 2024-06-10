package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a player in the Santorini game.
 * Each player has a unique identifier and controls two workers on the game board.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Player {
    private final String playerID;
    private List<Worker> workers = new ArrayList<>();
    private static final int NUM_WORKERS = 2;

    /**
     * Constructs a Player with a specified player ID and initializes two workers for this player.
     *
     * @param playerID A unique identifier for the player.
     */
    public Player(String playerID) {
        this.playerID = playerID;
        for (int i = 0; i < NUM_WORKERS; ++i) {
            workers.add(new Worker(playerID + (i + 1), this));
        }
        this.workers = Collections.unmodifiableList(workers);
    }

    /**
     * Sets the list of workers controlled by this player. This method can be used to update the player's workers,
     * though typically the initial set of workers is sufficient for gameplay.
     *
     * @param workers The list of Worker objects to be associated with this player.
     */
    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

    /**
     * Retrieves the player's unique ID.
     *
     * @return The unique identifier of the player.
     */
    public String getPlayerID() {
        return playerID;
    }

    /**
     * Retrieves the list of workers controlled by this player. Each player starts with two workers, which are used to
     * move around the board and build structures.
     *
     * @return A list of Worker objects controlled by this player.
     */
    public List<Worker> getWorkers() {
        return workers;
    }
}

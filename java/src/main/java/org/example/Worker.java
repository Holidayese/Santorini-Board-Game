package org.example;

/**
 * Represents a worker in the Santorini game.
 * Each worker is associated with a player and can occupy a position on the game board.
 *
 * @author Lu Wang
 * @AndrewID luw2
 */
public class Worker {
    private final String workerID;
    private final Player owner;
    private BoardPosition position;

    /**
     * Constructs a Worker with a specified ID and owner.
     *
     * @param workerID A unique identifier for the worker. This ID helps distinguish between different workers owned by the same player.
     * @param owner    The player who owns this worker. This association determines who can control the worker's movements and actions.
     */
    public Worker(String workerID, Player owner) {
        this.workerID = workerID;
        this.owner = owner;
    }

    /**
     * Retrieves the worker's ID.
     *
     * @return The unique identifier of the worker.
     */
    public String getWorkerID() {
        return workerID;
    }

    /**
     * Retrieves the owner (Player) of the worker.
     *
     * @return The player who owns this worker.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Retrieves the owner (Player) of the worker.
     *
     * @return The player who owns this worker.
     */
    public String getOwnerID() {
        return owner.getPlayerID();
    }

    /**
     * Retrieves the current position of the worker on the game board.
     *
     * @return The BoardPosition representing the worker's current location. May be null if the worker has not been placed on the board.
     */
    public BoardPosition getPosition() {
        return position;
    }

    /**
     * Sets the worker's position on the game board.
     * This method is typically used to move the worker to a new location.
     *
     * @param position The new position of the worker on the game board. This can be used to move the worker to a different square.
     */
    public void setPosition(BoardPosition position) {
        this.position = position;
    }


}

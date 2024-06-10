package org.example.godcards;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class GodCardFactory {
    /**
     * Creates a GodCard based on the specified name.
     *
     * @param name The name of the god card to create.
     * @return The corresponding GodCard object or null if the name does not match any known god cards.
     */
    public static GodCard createGodCard(String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
            case "Demeter":
                return new Demeter();
            case "Hephaestus":
                return new Hephaestus();
            case "Minotaur":
                return new Minotaur();
            case "Pan":
                return new Pan();
            case "Apollo":
                return new Apollo();
            default:
                return null;  // No god card found for the given name, return null or throw an exception as appropriate.
        }
    }
}

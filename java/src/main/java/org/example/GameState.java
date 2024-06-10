package org.example;

import org.example.godcards.GodCard;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class GameState {
    private Game game;

    public GameState(Game game) {
        this.game = game;
    }

    // Method to generate a JSON representation of the game state
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Game State and Current Action
        json.append("\"gamePhase\": \"").append(game.getGamePhase()).append("\",");
        json.append("\"currentAction\": \"").append(game.getCurrentAction()).append("\",");

        // Current Player
        json.append("\"currentPlayer\": \"").append(game.getCurrentPlayer().getPlayerID()).append("\",");

        // Current Worker
        Worker currentWorker = game.getCurrentWorker(); // Assuming this method exists and returns the current worker
        if (currentWorker != null) {
            json.append("\"currentWorker\": \"").append(currentWorker.getWorkerID()).append("\",");
        } else {
            json.append("\"currentWorker\": null,");
        }

        // Winner (if any)
        json.append("\"winner\": \"").append(this.game.getWinnerID()).append("\",");

        // Players
        json.append(playersJson()); // Including god card details in player's JSON

        // Board (Squares)
        json.append("\"board\": [");
        for (int y = 0; y < 5; y++) {
            json.append("[");
            for (int x = 0; x < 5; x++) {
                Square square = game.getBoard().getSquare(new BoardPosition(x, y));
                json.append("{");
                json.append("\"x\": ").append(x).append(",");
                json.append("\"y\": ").append(y).append(",");
                json.append("\"level\": ").append(square.getBuildingLevel()).append(",");
                json.append("\"dome\": ").append(square.hasDome()).append(",");
                json.append("\"occupied\": ").append(square.getIsOccupied());
                if (square.getIsOccupied() && square.getOccupiedWorker() != null) {
                    json.append(",\"workerID\": \"").append(square.getOccupiedWorker() != null ? square.getOccupiedWorker().getWorkerID() : null).append("\"");
                    json.append(",\"ownerID\": \"").append(square.getOccupiedWorker() != null ? square.getOccupiedWorker().getOwnerID() : null).append("\"");
                }
                json.append("}");
                if (x < 4) {
                    json.append(",");
                }
            }
            json.append("]");
            if (y < 4) {
                json.append(",");
            }
        }
        json.append("]");

        // Add the current player's possible moves and builds to the JSON
        if (game.getGamePhase() == Game.GamePhase.MOVE || game.getGamePhase() == Game.GamePhase.BUILD || game.getGamePhase() == Game.GamePhase.SECOND_BUILD) {
            json.append(",\"possibleMoves\": ").append(getPossibleMovesJson(currentWorker));
            json.append(",\"possibleBuilds\": ").append(getPossibleBuildsJson(currentWorker));
        }
        json.append("}");

        return json.toString();
    }

    private String playersJson() {
        StringBuilder playersJson = new StringBuilder();
        playersJson.append("\"players\": [");
        List<String> playerStates = game.getPlayers().stream().map(player -> {
            StringBuilder playerJson = new StringBuilder();
            playerJson.append("{");
            playerJson.append("\"playerID\": \"").append(player.getPlayerID()).append("\",");
            // append god card
            GodCard godCard = game.getGodCardForPlayer(player);
            String godCardName = godCard != null ? godCard.getName() : "None";
            playerJson.append("\"godCard\": \"").append(godCardName).append("\",");

            playerJson.append("\"workers\": [");

            List<String> workersJson = player.getWorkers().stream().map(worker -> {
                StringBuilder workerJson = new StringBuilder();
                workerJson.append("{");
                workerJson.append("\"workerID\": \"").append(worker.getWorkerID()).append("\",");
                BoardPosition pos = worker.getPosition();
                String positionJson = "null";
                if (pos != null) {
                    // If position is not null, create JSON representation
                    positionJson = String.format("{\"x\": %d, \"y\": %d}", pos.getX(), pos.getY());
                }
                workerJson.append("\"position\": ").append(positionJson);
                workerJson.append("}");
                return workerJson.toString();
            }).collect(Collectors.toList());

            playerJson.append(String.join(", ", workersJson));
            playerJson.append("]}");
            return playerJson.toString();
        }).collect(Collectors.toList());

        playersJson.append(String.join(", ", playerStates));
        playersJson.append("],");
        return playersJson.toString();
    }

    // Helper method to get possible moves in JSON format
    private String getPossibleMovesJson(Worker worker) {
        if (worker == null) {
            return "[]"; // Return an empty JSON array or handle appropriately.
        }
        List<BoardPosition> legalMoves = game.calculateLegalMovesForWorker(worker);
        // Convert the list of legal moves to JSON array format
        String movesJson = legalMoves.stream()
                .map(move -> String.format("{\"x\": %d, \"y\": %d}", move.getX(), move.getY()))
                .collect(Collectors.joining(",", "[", "]"));
        return movesJson;
    }

    // Helper method to get possible builds in JSON format
    private String getPossibleBuildsJson(Worker worker) {
        if (worker == null) {
            return "[]"; // Return an empty JSON array or handle appropriately.
        }
        List<BoardPosition> legalBuilds = game.calculateLegalBuildsForWorker(worker);
        // Convert the list of legal builds to JSON array format
        String buildsJson = legalBuilds.stream()
                .map(build -> String.format("{\"x\": %d, \"y\": %d}", build.getX(), build.getY()))
                .collect(Collectors.joining(",", "[", "]"));

        return buildsJson;
    }
}

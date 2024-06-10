package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lu Wang
 * @AndrewID luw2
 */
public class App extends NanoHTTPD {
    private Game game;

    public App() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning!\n");
    }

    public static void main(String[] args) {
        try {
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        Map<String, String> params = session.getParms();

        if (method == Method.GET && uri.equals("/newgame")) {
            return handleNewGame(params);
        } else if (method == Method.POST && uri.equals("/selectgodcard")) {
            Map<String, String> files = new HashMap<>();
            try {
                session.parseBody(files); // Parse the request body
                String json = files.get("postData");  // Assuming postData contains the raw body of your POST request
                if (json == null) {
                    json = files.get("content");
                }
                return handleSelectGodCards(json);
            } catch (IOException | NanoHTTPD.ResponseException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal Server Error: " + e.getMessage());
            }
        } else if (uri.equals("/placeworker")) {
            System.out.println("Params received: " + params);
            String xParam = params.get("x");
            String yParam = params.get("y");
            if (xParam == null || xParam.isEmpty() || yParam == null || yParam.isEmpty()) {
                return createErrorResponse("Missing or empty x or y parameter.");
            }
            int x = Integer.parseInt(xParam);
            int y = Integer.parseInt(yParam);
            Worker worker = this.game.getCurrentWorker();
            BoardPosition position = new BoardPosition(x, y);
            if (worker != null && this.game.placeWorker(worker, position)) {
                GameState gameState = new GameState(this.game);
                return createOkResponse(gameState.toJson());
            } else {
                return createErrorResponse("Failed to place worker.");
            }
        } else if (uri.equals("/selectworker")) {
            String workerId = params.get("workerId");
            String playerId = params.get("playerId");
            boolean selectionSuccess = this.game.selectWorker(workerId, playerId);
            if (selectionSuccess) {
                // Respond with the updated game state including the selected worker
                GameState gameState = new GameState(this.game);
                return createOkResponse(gameState.toJson());
            } else {
                return createErrorResponse("Failed to select worker.");
            }
        } else if (uri.equals("/move")) {
            Worker worker = this.game.getCurrentWorker();
            BoardPosition newPosition = new BoardPosition(Integer.parseInt(params.get("x")), Integer.parseInt(params.get("y")));
            if (worker != null && this.game.moveWorker(newPosition)) {
                GameState gameState = new GameState(this.game);
                return createOkResponse(gameState.toJson());
            }
        } else if (uri.equals("/build")) {
            Worker worker = this.game.getCurrentWorker();
            BoardPosition buildPosition = new BoardPosition(Integer.parseInt(params.get("x")), Integer.parseInt(params.get("y")));
            if (worker != null && this.game.buildBlock(buildPosition)) {
                GameState gameState = new GameState(this.game);
                return createOkResponse(gameState.toJson());
            }
        } else if (uri.equals("/skipSecondBuild")) {
            Worker worker = game.getCurrentWorker();
            if (worker != null && game.skipGodCardAction()) {
                GameState gameState = new GameState(this.game);
                return createOkResponse(gameState.toJson());
            } else {
                return createErrorResponse("Failed to skip second build.");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
    }

    private Response handleNewGame(Map<String, String> params) {
        Player playerA = new Player("A");
        Player playerB = new Player("B");
        this.game = new Game(playerA, playerB);

        // Log game start
        System.out.println("New game started with players: A and B.");
        GameState gameState = new GameState(this.game);
        return createOkResponse(gameState.toJson());
    }

    private Response handleSelectGodCards(String jsonData) {
        try {
            Gson gson = new Gson(); // Using Gson to parse JSON
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> params = gson.fromJson(jsonData, type);

            String playerId = params.get("playerId");
            String godCardName = params.get("godCard");

            System.out.println("Selecting god card with playerId: " + playerId + " and godCard: " + godCardName);
            Player player = game.findPlayerById(playerId);
            if (player == null) {
                return createErrorResponse("Player not found");
            }

            // Select the god card for the player
            game.selectGodCard(player, godCardName);
            GameState gameState = new GameState(this.game);
            return createOkResponse(gameState.toJson());
        } catch (Exception e) {
            return createErrorResponse("Error selecting god card: " + e.getMessage());
        }
    }

    // Helper method to create a successful JSON response
    private Response createOkResponse(String json) {
        Response response = newFixedLengthResponse(Response.Status.OK, "application/json", json);
        addCORSHeaders(response);
        return response;
    }

    // Helper method to create an error JSON response
    private Response createErrorResponse(String errorMessage) {
        String errorJson = String.format("{\"error\": \"%s\"}", errorMessage);
        Response response = newFixedLengthResponse(Response.Status.OK, "application/json", errorJson);
        addCORSHeaders(response);
        return response;
    }

    private void addCORSHeaders(Response response) {
        response.addHeader("Access-Control-Allow-Origin", "*"); // Allow any origin to access the resource
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE"); // Allowed methods
        response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization"); // Allowed headers
        response.addHeader("Access-Control-Allow-Credentials", "true"); // If you need to handle cookies
    }
}

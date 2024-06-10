# Starting Game #
## Set Up Backend Server ##
Either run the Java backend by using your IDE or by typing 

```
mvn install
mvn exec:exec
```
in the `java` folder. This will start the Java server at http://localhost:8080.

## Set Up Frontend Server ##
In the `frontend folder`, run

```
npm install
npm start
```

This will start the frontend server at http://localhost:3000.
## How to play game ##
In `http://localhost:3000`, you can see welcoming message and a `Start New Game` button, once click it, players can start to play game. <br><br>
Firstly, two players should select god cards, and hit `confirm God Cards` button. <br><br>
Secondly, players should place their workers one by one. <br><br>
Thirdly, Player A first starts to move and build then switches turn to Player B. When selecting workers to move, player can hit `undo selection` to undo worker selection and select again. If any player has a second build, the player can skip the optional second build by clicking on the worker's current location. Then moves and builds back and forth until someone wins or gets a tie. <br><br>
The message above the Board will show the current action and indicate which player to perform move or build. Before move, the game will highlight available workers to select. After selecting one worker, the game will highlight available cells to move or build. <br>
<br>
P.S. When testing, sometimes the frontend would not be able to get the server response from the backend due to server delay. When restarting the server, it might not be immediately ready to accept connections. The frontend might be attempting to connect before the backend server is fully operational. So if the frontend cannot load data, just 	`rerun` the backend and frontend again, and the game will process normally.
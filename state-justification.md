## Player
Stored In: "Game" class as "List<Player> players"

Justification: The Game class serves as the orchestrator of the game, making it a natural fit for tracking the players and which player's turn it is. This centralizes control logic, facilitating easier updates to game flow and turn management. Storing players in the Game class also simplifies the management of the game's lifecycle and state transitions, and leverages encapsulation by controlling access to player management and turn sequencing through the Game class's public methods.

Alternatives Considered: Storing players within the "Board" class. However, this conflates the responsibilities of the Board (which should focus on spatial aspects of the game) with player management.

## Current Player
Stored In: "Game" class as "Player currentPlayer"

Justification: It centralizes turn management within the Game class, which controls the progression and state transitions. This supports high cohesion within Game and simplifies the logic for determining whose turn it is. Storing the current player within the Game class leverages encapsulation by controlling access to player management and turn sequencing through the Game class's public methods.This setup ensures that changes to the current player are made in a controlled manner, preventing direct manipulation from outside the Game class and adhering to encapsulation principles.

Alternatives Considered: A separate TurnManager class could be introduced for turn management. The trade-off here is increased complexity and additional inter-class dependencies versus centralized game logic.

## Worker Locations
Stored In: Each Worker has a position (of type BoardPosition). The Board class contains a map or 2D array tracking all Squares.

Justification: Encapsulation is applied by associating each Worker with a BoardPosition object, allowing the Worker to control its location information. This design encapsulates the Worker's state within itself and provides a clear interface for moving the worker, ensuring that location updates are managed through defined behaviors rather than direct field manipulation.

Alternatives Considered: Having the Game class manage all positions directly. This would overly centralize state management, making the Game class too complex and less cohesive.

## Tower
Stored In: Each Square contains a Tower.

Justification: By encapsulating the Tower within a Square, the design adheres to encapsulation by ensuring that any modifications to a tower's state, such as adding levels or a dome, are managed through the Square's methods. This restricts direct access to the Tower's fields, aligning with encapsulation principles by offering a controlled interface for interactions with the tower.

Alternatives Considered: A global map in Board mapping positions to towers. This approach would separate spatial management from the structural attributes of each location, reducing cohesion.

## Winner
Stored In: "Game" class as "Player winner".

Justification: The Game class oversees the rules and flow, making it suitable for determining and storing the game's outcome. The decision to track the game winner within the Game class follows encapsulation by centralizing the game outcome's determination and access. This approach ensures that identifying and declaring a winner is managed through controlled operations within the Game class, safeguarding the integrity of game state management. This choice also supports the Single Responsibility Principle by keeping game state management centralized.

Alternatives Considered: Storing win status in each Player. This would distribute the responsibility for tracking game outcomes but could lead to inconsistencies and redundant checks across players.

## Design goals/principles/heuristics considered
Encapsulation: Keeping related data and behaviors together.
Cohesion: Ensuring classes are focused on a single purpose.
Single Responsibility Principle: Each class has one reason to change.
Low Coupling: Reducing dependencies between components to increase modularity.

## Alternatives considered and analysis of trade-offs
TurnManager Class: Separates turn logic from Game, potentially improving modularity but adding complexity.
Player Positions in Game: Centralizes state but reduces cohesion by mixing player management with spatial tracking.
Global Tower Map in Board: Separates structural elements from spatial ones, potentially simplifying Square, but reduces the natural encapsulation of a square managing its tower.
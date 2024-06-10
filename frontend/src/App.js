import React, { useState, useEffect } from 'react';
import './App.css';

const BOARD_SIZE = 5;
const initialBoard = () => Array.from({ length: BOARD_SIZE }, () => Array(BOARD_SIZE).fill(null));

const App = () => {
  const [board, setBoard] = useState(initialBoard());
  const [message, setMessage] = useState(`Welcome to Santorini! Click 'Start New Game' to begin.`);
  const [currentPlayer, setCurrentPlayer] = useState(null);
  const [currentWorker, setCurrentWorker] = useState(null);
  const [currentPhase, setCurrentPhase] = useState('null');
  const [possibleMoves, setPossibleMoves] = useState([]);
  const [possibleBuilds, setPossibleBuilds] = useState([]);
  const [godCards, setGodCards] = useState({ A: 'None', B: 'None' });
  const [isWorkerAvailable, setIsWorkerAvailable] = useState(true);

  const startNewGame = async () => {
    console.log("StartNewGame is called. Waiting for god card selection.");
    // Reset the state to initial values
    resetGameState();
    // Fetch the new game data
    const response = await fetch('/newgame', { method: 'GET' });
    if (response.ok) {
      const data = await response.json();
      console.log("New game data received:", data);
      setCurrentPhase('INITIALIZE');
      setMessage("Please select god cards for both players.");
      updateGameState(data);
    } else {
      console.error('Failed to start a new game:', response.status);
      setMessage("Failed to start a new game. Please try again.");
    }
  };

  const resetGameState = () => {
    setBoard(initialBoard()); // This assumes initialBoard() always returns a new, correct initial state
    setCurrentPlayer(null);
    setCurrentWorker(null);
    setCurrentPhase(null);
    setPossibleMoves([]);
    setPossibleBuilds([]);
    setGodCards({ A: 'None', B: 'None' });
    setMessage("Welcome to Santorini! Click 'Start New Game' to begin.");
  };

  const confirmGodCards = async () => {
    if (currentPhase !== 'INITIALIZE') {
      setMessage('God card selection is not allowed right now.');
      return;
    }
    console.log("God cards selected: Player A -", godCards.A, ";Player B -", godCards.B);
    try {
      // Make both requests simultaneously
      const responses = await Promise.all([
        fetch('/selectgodcard', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ playerId: "A", godCard: godCards.A })
        }),
        fetch('/selectgodcard', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ playerId: "B", godCard: godCards.B })
        })
      ]);
      // Convert both responses to JSON
      const results = await Promise.all(responses.map(res => res.json()));
      // Check both responses were OK before proceeding
      if (responses[0].ok && responses[1].ok) {
        setCurrentPhase('PLACE_WORKER'); // Change phase only after both selections are confirmed
        setMessage(`God cards selected. Please place your workers.`);
        updateGameState(results[1]); // Assuming the second call returns the initial state
      } else {
        setMessage(`Error during god card selection: ${results[0].error || results[1].error}`);
      }
    } catch (error) {
      console.error('Network or other error during god card selection:', error);
      setMessage('Network or processing error during god card selection.');
    }
  };

  const handleGodCardSelection = (player, card) => {
    setGodCards(prev => ({ ...prev, [player]: card }));
  };

  useEffect(() => {
    console.log("Current game phase after update: ", currentPhase);
  }, [currentPhase]);

  const updateGameState = (data) => {
    if (data.board) {
      setBoard(parseBoard(data.board));
      setCurrentPlayer(data.currentPlayer);
      setCurrentWorker(data.currentWorker);
      setCurrentPhase(data.gamePhase);
      // Reset possible moves and builds by default
      setPossibleMoves([]);
      setPossibleBuilds([]);
      // If it's the move or build phase, set possible moves and builds
      if (data.gamePhase === 'MOVE' || data.gamePhase === 'BUILD' || data.gamePhase === 'SECOND_BUILD') {
        setPossibleMoves(data.possibleMoves);
        setPossibleBuilds(data.possibleBuilds);
        // Check if the current worker can move
        if ((data.possibleMoves && data.possibleMoves.length === 0)) {
          setIsWorkerAvailable(false);
          setMessage(`This worker is not available, click undo and select another one.`);
        } else {
          setIsWorkerAvailable(true);
        }
      } else {
        // If we're not in MOVE or BUILD phase, reset possible moves and builds
        setPossibleMoves([]);
        setPossibleBuilds([]);
      }
      // Set the message for the current game phase
      setMessage(`Current Phase: ${data.gamePhase}. Player ${data.currentPlayer}'s turn.`);
    } else {
      // If 'board' does not exist, you can choose to set a default board state or handle the error.
      console.error("Board data is missing from the server response.");
      setMessage("There was an error loading. Please rerun the game following README file's instructions.");
      setBoard(initialBoard()); 
    }
  };

  const parseBoard = (boardArray) => {
    console.log("Board array received in parseBoard:", boardArray);
    if (!boardArray) {
      console.error("parseBoard was called with undefined or null boardArray");
      // return []; // Return an empty array or some default state
      return initialBoard();
    }
    return boardArray.map(row =>
      row.map(cell => ({
        ...cell,
        workerId: cell ? (cell.occupied ? cell.workerID : null) : null, // Set workerId to null if not occupied
      }))
    );
  };

  const handleCellClick = async (x, y) => {
    console.log("handleCellClick is called with workerId:", currentWorker, "x:", x, "y:", y);
    console.log("Current game phase: ", currentPhase);
    switch (currentPhase) {
      case 'PLACE_WORKER':
        await placeWorker(x, y);
        break;
      case 'MOVE':
        if (!currentWorker) {
          setMessage("Please select a worker before moving.");
          const workerId = getWorkerIdFromBoard(x, y);
          if (workerId && workerId.startsWith(currentPlayer)) {
            await selectWorker(getWorkerIdFromBoard(x, y));
          } else {
            setMessage(`Invalid selection. Please select one of your workers. Current Player: ${currentPlayer}.`);
          }
        } else {
          await moveWorker(x, y);
        }
        break;
      case 'SECOND_MOVE':
      case 'BUILD':
        if (possibleBuilds.some(build => build.x === x && build.y === y)) {
          await buildBlock(x, y);
        }
        break;
      case 'SECOND_BUILD':
        const currentWorkerPosition = getCurrentWorkerPosition();
        console.log(`Position of current worker is: ${currentWorkerPosition.x}, ${currentWorkerPosition.y}.`);
        if (currentWorkerPosition && x === currentWorkerPosition.x && y === currentWorkerPosition.y) {
          console.log("skipSecondBuild is called at position:", currentWorkerPosition);
          await skipSecondBuild();
        } else if (possibleBuilds.some(build => build.x === x && build.y === y)) {
          await buildBlock(x, y);
        }
        break;
      default:
        console.log('Action not allowed in current phase.');
    }
  };

  // Helper method to get current worker's position
  const getCurrentWorkerPosition = () => {
    for (let y = 0; y < board.length; y++) {
      for (let x = 0; x < board[y].length; x++) {
        if (board[y][x] && board[y][x].workerId === currentWorker) {
          return { x, y };
        }
      }
    }
    return null; // Return null if the worker isn't found
  };

  // Helper method to get occupied worker's ID of a position
  const getWorkerIdFromBoard = (x, y) => {
    const cell = board[y][x];
    return cell && cell.occupied ? cell.workerId : null;
  };

  // Function to place workers when game is initialized
  const placeWorker = async (x, y) => {
    console.log("placeWorker is called with workerId:", currentWorker, "x:", x, "y:", y);
    if (!currentWorker || currentPhase !== 'PLACE_WORKER') {
      console.error('No worker selected or not in place worker phase.');
      return;
    }
    try {
      const response = await fetch(`/placeworker?workerId=${currentWorker}&x=${x}&y=${y}`, { method: 'GET' });
      if (response.ok) {
        const data = await response.json();
        console.log("Place worker response data: ", data);
        if (!data.error) {
          updateGameState(data);
        } else {
          console.error('Error response from place worker:', data.error);
        }
      } else {
        const errorData = await response.json();
        console.error('Failed to place worker:', errorData.error || 'An error occurred');
      }
    } catch (error) {
      console.error('Error placing worker:', error);
    }
  };

  // Function to select a worker
  const selectWorker = async (workerId) => {
    console.log(`Selecting worker: ${workerId}`);
    try {
      const response = await fetch(`/selectworker?workerId=${workerId}&playerId=${currentPlayer}`, { method: 'POST' });
      if (response.ok) {
        const data = await response.json();
        console.log("Select worker response data: ", data);
        // If the worker is successfully selected, show a message, update "currentWorker" state
        if (data.possibleMoves && data.possibleMoves.length === 0) {
          // If not, display a message and try to automatically select the other worker
          setMessage(`Worker ${workerId} cannot move. Please select another worker.`);
        } else {
          // If the worker can move, update the state as normal
          setCurrentWorker(workerId);
          setMessage(`Worker ${workerId} selected. Now move.`);
          setBoard(parseBoard(data.board));
          setPossibleMoves(data.possibleMoves);
        }
      } else {
        // Handle unsuccessful selection attempt
        console.error('Worker selection was unsuccessful.');
        const errorData = await response.json();
        setMessage(errorData.error || 'Failed to select worker.');
      }
    } catch (error) {
      console.error('Error selecting worker:', error);
      setMessage('Error selecting worker.');
    }
  };

  // Function to undo worker selection
  const undoWorkerSelection = () => {
    setCurrentWorker(null);
    setIsWorkerAvailable(true);
    setMessage(`Worker deselected. Player ${currentPlayer}, please select a worker.`);
  };

  // Function to move worker
  const moveWorker = async (x, y) => {
    console.log(`moveWorker is called with workerId: ${currentWorker}, x: ${x}, y: ${y}`);
    // Ensure we are in the correct phase and a worker is selected
    if (currentPhase !== 'MOVE' || !currentWorker) {
      console.error('Not in move phase or no worker selected.');
      return;
    }
    try {
      const response = await fetch(`/move?workerId=${currentWorker}&x=${x}&y=${y}`, { method: 'POST' });
      if (response.ok) {
        const data = await response.json();
        // Update the game state with the response
        updateGameState(data);
        console.log(data);
        if (data.board) {
          setBoard(parseBoard(data.board));
        }
        if (data.winner !== "null") {
          setMessage(`Game over. Winner is Player ${data.winner}. Click 'Start New Game' to play again.`);
          setCurrentPhase('END'); // End the game or manage the state accordingly
        } else {
          // Transition to build phase if the move was successful and no winner was declared
          setCurrentPhase('BUILD');
          setMessage(`Build with your worker. It is ${currentPlayer}'s turn.`);
        }
      } else {
        // Handle errors
        const errorData = await response.json();
        console.error('Move was unsuccessful:', errorData.error || 'An error occurred');
        setMessage(errorData.error || 'Failed to move worker.');
      }
    } catch (error) {
      console.error('Error moving worker:', error);
      setMessage('Error moving worker.');
    }
  };

  // Function to build a block
  const buildBlock = async (x, y) => {
    console.log("buildBlock is called with workerId:", currentWorker, "x:", x, "y:", y);
    if (!(currentPhase === 'BUILD' || currentPhase === 'SECOND_BUILD') || !currentWorker) {
      console.error('Not in build phase or no worker selected.');
      return;
    }
    try {
      const response = await fetch(`/build?workerId=${currentWorker}&x=${x}&y=${y}`, { method: 'POST' });
      if (response.ok) {
        const data = await response.json();
        console.log(data);
        updateGameState(data);
      } else {
        // Handle unsuccessful build attempt
        const errorData = await response.json();
        console.error('Build was unsuccessful:', errorData.error || 'An error occurred');
        setMessage(errorData.error || 'Failed to build.');
      }
    } catch (error) {
      console.error('Error during build:', error);
      setMessage('Error during build.');
    }
  };

  // Function to skip an optional second build
  const skipSecondBuild = async () => {
    console.log("skipSecondBuild is called.");
    try {
      const response = await fetch('/skipSecondBuild', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ workerId: currentWorker })
      });
      if (response.ok) {
        const data = await response.json();
        updateGameState(data);
        setMessage('Second build skipped. It is now the next player\'s turn.');
      } else {
        const errorData = await response.json();
        console.error('Failed to skip second build:', errorData.error);
        setMessage(errorData.error || 'Failed to skip second build.');
      }
    } catch (error) {
      console.error('Error during skipping second build:', error);
      setMessage('Error during skipping second build.');
    }
  };

  const renderCell = (cell, x, y) => {
    // console.log(`Rendering cell at position x: ${x}, y: ${y}`);
    if (!cell) {
      return <div className="cell" key={`${x}-${y}`}></div>; // Fallback for debugging
    }

    let cellContent = '';
    let classNames = ['cell'];
    // Determine if the cell is a possible move or build
    const isPossibleMove = possibleMoves.some(move => move.x === x && move.y === y);
    const isPossibleBuild = possibleBuilds.some(build => build.x === x && build.y === y);

    const isCurrentPlayerWorker = cell.occupied && cell.ownerID === currentPlayer && !currentWorker && currentPhase === 'MOVE';
    if (isCurrentPlayerWorker) {
      classNames.push('cell-available-workers'); // workers available for selection
    }

    if (cell.occupied && currentWorker === cell.workerId) { // selected worker on a cell
      classNames.push('cell-selected-worker'); // selected worker for move
    } else if (isPossibleMove && currentWorker && currentPhase === 'MOVE') { // possible move hints
      classNames.push('cell-possible-moves');
    } else if (isPossibleBuild && currentWorker && (currentPhase === 'BUILD' || currentPhase === 'SECOND_BUILD')) { // possible build hints
      classNames.push('cell-possible-builds');
    }

    // Adjusting cell content based on the presence of a worker and the level of the tower
    if (cell.occupied && cell.workerId) {
      cellContent = `${'[ '.repeat(cell.level)} ${cell.workerId} ${' ]'.repeat(cell.level)}`;
    } else if (cell.level > 0) {
      // Display the tower levels
      cellContent = `${'[ '.repeat(cell.level)} ${' ]'.repeat(cell.level)}`;
    }

    // Incorporate dome representation
    if (cell.dome) {
      cellContent = '[ [ [ o ] ] ]';
    }

    return (
      <button
        key={`${x}-${y}`}
        className={classNames.join(' ')}
        onClick={() => handleCellClick(x, y)}
      >
        {cellContent}
      </button>
    );
  };

  return (
    <div className="App">
      <h2>{message}</h2>
      {currentPhase === 'INITIALIZE' && (
        <div className="god-card-selection">
          <h3>Select God Cards:</h3>
          <div className="selection-group">
            <label className="label">Player A's God Card:</label>
            <select className="select" value={godCards.A} onChange={e => handleGodCardSelection('A', e.target.value)}>
              <option value="None">None</option>
              <option value="Demeter">Demeter</option>
              <option value="Hephaestus">Hephaestus</option>
              <option value="Minotaur">Minotaur</option>
              <option value="Pan">Pan</option>
              <option value="Apollo">Apollo</option>
            </select>
          </div>
          <div className="selection-group">
            <label className="label">Player B's God Card:</label>
            <select className="select" value={godCards.B} onChange={e => handleGodCardSelection('B', e.target.value)}>
              <option value="None">None</option>
              <option value="Demeter">Demeter</option>
              <option value="Hephaestus">Hephaestus</option>
              <option value="Minotaur">Minotaur</option>
              <option value="Pan">Pan</option>
              <option value="Apollo">Apollo</option>
            </select>
          </div>
          <button className="btn" onClick={confirmGodCards}>Confirm God Cards</button>
        </div>
      )}
      {/* {currentPhase !== 'INITIALIZE' && (
        <button className="btn" onClick={startNewGame}>Start New Game</button>
      )} */}
      {currentPhase !== 'INITIALIZE' && (
        <>
          <button className="btn" onClick={startNewGame}>Start New Game</button>
          {currentPhase !== 'INITIALIZE' && (
            <div className="god-card-display">
              <p>Player A's God Card: {godCards.A}</p>
              <p>Player B's God Card: {godCards.B}</p>
            </div>
          )}
          <div className="board">
            {board.map((row, y) => (
              <div key={y} className="row">
                {row.map((cell, x) => renderCell(cell, x, y))}
              </div>
            ))}
          </div>
        </>
      )}
      {currentPhase === 'SECOND_BUILD' && (
        <p className="second-build-message">* You can click selected worker's cell to skip the second build *</p>
      )}
      {currentWorker && currentPhase === 'MOVE' && (
        <button className="btn" onClick={undoWorkerSelection}>Undo Selection</button>
      )}
    </div>
  );
};

export default App;

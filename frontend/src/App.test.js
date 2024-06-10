import React from 'react';
import { render, screen, waitFor, fireEvent, cleanup } from '@testing-library/react';
import App from './App';

const BOARD_SIZE = 5;
const initialBoard = () => Array.from({ length: BOARD_SIZE }, () => Array(BOARD_SIZE).fill(null));

describe('App Component', () => {
  test('renders the welcome message and start button initially', () => {
    render(<App />);
    expect(screen.getByText(/welcome to santorini!/i)).toBeInTheDocument();
    expect(screen.getByText(/click 'Start New Game' to begin./i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /start new game/i })).toBeInTheDocument();
  });
  afterEach(cleanup);
  
  test('handles errors during the start of a new game gracefully', async () => {
    const mockFetchPromise = Promise.resolve({
      json: () => Promise.reject(new Error('Network error')),
      ok: false
    });
    global.fetch = jest.fn().mockImplementation(() => mockFetchPromise);
  
    render(<App />);
    fireEvent.click(screen.getByRole('button', { name: /start new game/i }));
  
    await waitFor(() => expect(screen.getByText(/failed to start a new game. please try again./i)).toBeInTheDocument());
  });
  afterEach(cleanup);
});

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import java.util.ArrayList;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.GomokuGrid;
import mygame.boardgames.gomoku.WinningRow;
import mygame.boardgames.gomoku.player.GomokuPlayer;
import mygame.boardgames.network.NewGameMessage;

/**
 *
 * @author Jimmy
 */
public class GomokuGame {
    
    public interface Listener {
        public void onMove(GomokuGame game, CellColor color, GridPoint p);
        public void onWin(GomokuGame game, WinningRow wr);
        public void onReset(GomokuGame game);
    }
    
    private static int NEW_GAME_ID = 0;
    
    private final int gameID;
    private GomokuGrid grid;
    private GomokuPlayer startingPlayer = null, currentPlayer = null;
    private boolean locked = true;
    private ArrayList<Listener> listeners = null;
    
    public GomokuGame(GridSize size) {
        gameID = NEW_GAME_ID++;
        grid = new GomokuGrid(size);
    }
    
    public GomokuGame() {
        this(new GridSize(11, 11));
    }
    
    public GomokuGame(NewGameMessage msg) {
        gameID = msg.getGameID();
        grid = new GomokuGrid(msg.getGridSize());
    }
    
    public void setPlayers(GomokuPlayer p1, GomokuPlayer p2) {
        
        startingPlayer = currentPlayer = p1;
        
        p1.setOpponent(p2);
        p2.setOpponent(p1);
        
        p1.setGame(this);
        p2.setGame(this);
    }
    
    public void start() {
        
        locked = false;
        
        GomokuPlayer opponent = startingPlayer.getOpponent();
        opponent.onStartGame(false);
        startingPlayer.onStartGame(true);
        
        // Switch starting player for next game
        startingPlayer = opponent;
    }
    
    public void reset() {
        grid.reset();
        start();
        
        notifyListenersOnReset();
    }
    
    public boolean tryMove(GomokuPlayer player, GridPoint p) {
        
        if (locked)
            return false;
        
        if (player != currentPlayer)
            return false;
        
        if (!grid.tryMove(p, player.getColor()))
            return false;
        
        GomokuPlayer opponent = player.getOpponent();
        
        currentPlayer = opponent;
        
        WinningRow wr = new WinningRow(grid);
        CellColor winningColor = wr.getWinningColor();
        
        // Notify listeners
        if (winningColor == CellColor.NONE) {
            notifyListenersOnMove(player.getColor(), p);
            opponent.onOpponentMove(p);
        } else {
            locked = true;
            notifyListenersOnWin(wr);
            player.onGameWon(winningColor);
            opponent.onGameWon(winningColor);
        }
        
        return true;
    }
    
    public GomokuGrid getGrid() {
        return grid;
    }
    public GomokuPlayer getCurrentPlayer() {
        return currentPlayer;
    }
    public int getID() {
        return gameID;
    }
    
    
     // ***** Listener methods *****
    
    public void addListener(Listener listener) {
        if (listeners == null) {
            listeners = new ArrayList<Listener>();
        }
        listeners.add(listener);
    }
    
    private void notifyListenersOnWin(WinningRow wr) {
        if (listeners != null) {
            for (Listener listener : listeners) {
                listener.onWin(this, wr);
            }
        }
    }
    
    private void notifyListenersOnMove(CellColor color, GridPoint p) {
        if (listeners != null) {
            for (Listener listener : listeners) {
                listener.onMove(this, color, p);
            }
        }
    }
    
    private void notifyListenersOnReset() {
        if (listeners != null) {
            for (Listener listener : listeners) {
                listener.onReset(this);
            }
        }
    }
}

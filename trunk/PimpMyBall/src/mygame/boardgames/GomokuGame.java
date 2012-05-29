/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import mygame.util.GridSize;
import mygame.util.GridPoint;
import java.util.ArrayList;
import mygame.boardgames.network.GomokuStartMessage;

/**
 * 
 * @author Jimmy
 */
public class GomokuGame {

    public interface Listener {
        public void onMove(GomokuGame game, CellColor color, GridPoint p);
        public void onWin(GomokuGame game, WinningRow wr);
        public void onDraw(GomokuGame game);
        public void onReset(GomokuGame game);
    }
    
    private static int NEW_GAME_ID = 0;
    
    private final int gameID;
    private GomokuGrid grid;
    private GomokuPlayer firstPlayer, secondPlayer, currentTurn;
    private boolean locked = false;
    private ArrayList<Listener> listeners = null;
    
    public GomokuGame(GridSize size) {
        gameID = NEW_GAME_ID++;
        grid = new GomokuGrid(size);
    }
    
    public GomokuGame() {
        this(new GridSize(11, 11));
    }
    
    public GomokuGame(long firstPlayerID, long secondPlayerID) {
        this();
        firstPlayer = new GomokuPlayer(firstPlayerID, CellColor.BLUE);
        secondPlayer = new GomokuPlayer(secondPlayerID, CellColor.RED);
        currentTurn = firstPlayer;
    }
    
    public GomokuGame(GomokuStartMessage msg) {
        
        gameID = msg.getGameID();
        grid = new GomokuGrid(msg.boardSize);
        
        firstPlayer = new GomokuPlayer(msg.firstPlayerID, CellColor.BLUE);
        secondPlayer = new GomokuPlayer(msg.secondPlayerID, CellColor.RED);
        currentTurn = firstPlayer;
    }

    public void reset() {
        grid.reset();
        locked = false;
        notifyListenersOnReset();
    }
    
    public long getOpponentID(long id) {
        return firstPlayer.id == id ? secondPlayer.id : firstPlayer.id;
    }
    private GomokuPlayer getOpponent(GomokuPlayer p) {
        return (firstPlayer == p) ? secondPlayer : firstPlayer;
    }
    
    public boolean tryMove(long playerID, GridPoint p) {
        
        if (locked)
            return false;
        
        if (playerID != currentTurn.id)
            return false;
        
        CellColor playedColor = currentTurn.color;
        
        if (!grid.tryMove(p, playedColor))
            return false;

        WinningRow wr = new WinningRow(grid);
        CellColor winningColor = wr.getWinningColor();
        currentTurn = getOpponent(currentTurn);
        
        // Notify listeners
        if (winningColor == CellColor.NONE) {
            if (grid.isFull()) {
                notifyListenersOnDraw();
            } else {
                notifyListenersOnMove(playedColor, p);
            }
        } else {
            locked = true;
            notifyListenersOnMove(playedColor, p);
            notifyListenersOnWin(wr);
        }
        return true;
    }
    
    public GomokuGrid getGrid() {
        return grid;
    }
    
    public GomokuPlayer getCurrentPlayer() {
        return currentTurn;
    }
    public GomokuPlayer getFirstPlayer() {
        return firstPlayer;
    }
    public GomokuPlayer getSecondPlayer() {
        return secondPlayer;
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
    public void removeListener(Listener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
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
    
    private void notifyListenersOnDraw() {
        if (listeners != null) {
            for (Listener listener : listeners) {
                listener.onDraw(this);
            }
        }
    }
}

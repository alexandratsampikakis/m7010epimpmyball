/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.GomokuGrid;
import mygame.boardgames.gomoku.player.GomokuPlayer;

/**
 *
 * @author Jimmy
 */
public class GomokuGame {
    
    private static int NEW_GAME_ID = 0;
    
    private final int gameID;
    private GomokuGrid grid;
    
    private GomokuPlayer p1, p2;
    private GomokuPlayer currentPlayer;
    
    private boolean p1Start;
    
    public GomokuGame(GomokuPlayer p1, GomokuPlayer p2, boolean start) {
        
        gameID = NEW_GAME_ID++;
        
        currentPlayer = (start) ? p1 : p2;
        p1Start = start;
        
        grid = new GomokuGrid(15, 15);
        
        this.p1 = p1;
        this.p2 = p2;
        
        p1.setOpponent(p2);
        p2.setOpponent(p1);
        
        p1.setGame(this);
        p2.setGame(this);
    }
    
    public boolean tryMove(GomokuPlayer player, GridPoint p) {
        
        if (player != currentPlayer)
            return false;
        
        if (!grid.tryMove(p, player.getColor()))
            return false;
        
        GomokuPlayer opponent = currentPlayer.getOpponent();
        currentPlayer = opponent;
        
        opponent.onOpponentMove(p);
        
        return true;
    }
    
    public void start() {
        // Haxx to make the AI-player work properly... :D
        if (p1Start) {
            p2.onStartGame(false);
            p1.onStartGame(true);
        } else {
            p1.onStartGame(false);
            p2.onStartGame(true);
        }
        p1Start = !p1Start;
    }
    
    public GomokuGrid getGrid() {
        return grid;
    }
    public GomokuPlayer getCurrentPlayer() {
        return currentPlayer;
    }
    public GomokuPlayer getP1() {
        return p1;
    }
    public GomokuPlayer getP2() {
        return p2;
    }
    public int getID() {
        return gameID;
    }
    
}

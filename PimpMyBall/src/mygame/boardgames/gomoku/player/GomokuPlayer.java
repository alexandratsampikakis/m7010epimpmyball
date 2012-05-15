/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku.player;

import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;
import mygame.boardgames.gomoku.CellColor;

/**
 * TODO:
 * Exchange abstract methods for GomokuGame.Listener?
 * 
 * @author Jimmy
 */
public abstract class GomokuPlayer {
    
    protected CellColor color = CellColor.NONE;
    protected GomokuPlayer opponent;
    protected GomokuGame game = null;
    
    public void setGame(GomokuGame game) {
        this.game = game;
    }
    
    public void setOpponent(GomokuPlayer opponent) {
        if (opponent != null && color == CellColor.NONE) {
            color = CellColor.randomColor();
            opponent.color = color.opponent();
        }
        this.opponent = opponent;
    }
    
    public GomokuPlayer getOpponent() {
        return opponent;
    }
    
    public CellColor getColor() {
        return color;
    }
    
    public abstract void onOpponentMove(GridPoint p);
    public abstract void onStartGame(boolean myTurn);
    public abstract void onGameWon(CellColor winningColor);
    public abstract void onOpponentSurrender();
}

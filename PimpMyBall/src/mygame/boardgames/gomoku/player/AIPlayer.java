/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku.player;

import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;
import mygame.boardgames.gomoku.GomokuAI;
import mygame.boardgames.gomoku.CellColor;

/**
 *
 * @author Jimmy
 */
public class AIPlayer extends GomokuPlayer {
    
    private GomokuAI ai;
    
    public AIPlayer() {
    }
    
    @Override
    public void setGame(GomokuGame game) {
        super.setGame(game);
        ai = new GomokuAI(game.getGrid());
    }
    
    @Override
    public void onOpponentMove(GridPoint p) {
        game.tryMove(this, ai.nextMove(color, p));
    }

    @Override
    public void onStartGame(boolean myTurn) {
        if (myTurn)
            game.tryMove(this, ai.nextMove(color, null));
    }
    
    @Override
    public void onGameWon(CellColor winningColor) {        
    }
    
    @Override
    public void onOpponentSurrender() {
        
    }
}

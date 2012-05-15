/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boardgames.gomoku.player;

import boardgames.GomokuGame;
import boardgames.GridPoint;
import boardgames.gomoku.GomokuAI;
import boardgames.gomoku.CellColor;

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
    public void onGameWon(boolean didWin) {
        
    }
    @Override
    public void onOpponentSurrender() {
        
    }
}

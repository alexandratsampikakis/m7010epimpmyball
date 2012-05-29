/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GomokuPlayer;
import mygame.boardgames.WinningRow;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuEndMessage extends AbstractGomokuMessage {
    
    public long winnerID = -1;
    public long loserID = -1;
    public int scoreChange;
    
    public GomokuEndMessage() {
    }

    public GomokuEndMessage(GomokuGame game, WinningRow wr, int scoreChange) {
        super(game);
        
        GomokuPlayer winner = game.getFirstPlayer().getColor() == wr.getWinningColor() ?
                game.getFirstPlayer() : game.getSecondPlayer();
        
        this.winnerID = winner.getID();
        this.loserID = game.getOpponentID(winnerID);
        this.scoreChange = scoreChange;
    }
    
    public GomokuEndMessage(GomokuGame game, long winnerID, int scoreChange) {
        super(game);
        
        this.winnerID = winnerID;
        this.loserID = game.getOpponentID(winnerID);
        this.scoreChange = scoreChange;
    }
}

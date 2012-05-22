/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network.broadcast;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.WinningRow;
import mygame.boardgames.gomoku.player.RemotePlayerServer;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuEndMessage extends AbstractMessage {
    
    public int gameID;
    public long winnerID;
    public long loserID;
    public int scoreChange;
    
    public GomokuEndMessage() {
    }
    
    public GomokuEndMessage(GomokuGame game, WinningRow wr, int scoreChange) {
          
        RemotePlayerServer p1 = (RemotePlayerServer) game.getStartingPlayer();
        RemotePlayerServer p2 = (RemotePlayerServer) p1.getOpponent();
        RemotePlayerServer winner = (p1.getColor() == wr.getWinningColor()) ? p1 : p2;
        RemotePlayerServer loser = (RemotePlayerServer) winner.getOpponent();
        
        this.winnerID = winner.getUser().getId();
        this.loserID = loser.getUser().getId();
        this.gameID = game.getID();
        this.scoreChange = scoreChange;
    }
}

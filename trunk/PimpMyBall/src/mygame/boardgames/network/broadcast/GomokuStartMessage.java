/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network.broadcast;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridSize;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.player.RemotePlayerServer;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuStartMessage extends AbstractMessage {
    
    public Vector3f firstPlayerPos, secondPlayerPos;
    
    public int gameID;
    public long firstPlayerID;
    public long secondPlayerID;
    public CellColor startingColor;
    public GridSize boardSize;
    
    public GomokuStartMessage() {
    }
    
    public GomokuStartMessage(GomokuGame game) {
        
        RemotePlayerServer p1 = (RemotePlayerServer) game.getStartingPlayer();
        RemotePlayerServer p2 = (RemotePlayerServer) p1.getOpponent();
        
        gameID = game.getID();
        firstPlayerID = p1.getUser().getId();
        secondPlayerID = p2.getUser().getId();
        startingColor = p1.getColor();
        boardSize = game.getGrid().getSize();
        
        firstPlayerPos = p1.getUser().getBall().getPosition();
        secondPlayerPos = p1.getUser().getBall().getPosition();
    }
}

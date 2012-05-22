/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network.broadcast;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;
import mygame.boardgames.gomoku.CellColor;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuUpdateMessage extends AbstractMessage {
    
    public int gameID = -1;
    public GridPoint p;
    public CellColor color;
    
    public GomokuUpdateMessage() {
    }
    public GomokuUpdateMessage(GomokuGame game, CellColor color, GridPoint p) {
        this.p = p;
        this.color = color;
        this.gameID = game.getID();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuMessage extends AbstractMessage {
    
    public int gameID = -1;
    public GridPoint p;
    
    public GomokuMessage() {
        p = new GridPoint();
        setReliable(true);
    }
    public GomokuMessage(GomokuGame game, GridPoint p) {
        this.p = p;
        this.gameID = game.getID();
        setReliable(true);
    }
}

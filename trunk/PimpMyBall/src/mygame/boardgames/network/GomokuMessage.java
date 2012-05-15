/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GridPoint;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuMessage extends AbstractMessage {
    
    public int gameID;
    public GridPoint p;
    
    public GomokuMessage() {
        p = new GridPoint();
        setReliable(true);
    }
    public GomokuMessage(GridPoint p) {
        this.p = p;
        setReliable(true);
    }
}

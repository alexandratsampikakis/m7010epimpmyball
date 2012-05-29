/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;

/**
 *
 * @author Jimmy
 */
@Serializable
public class AbstractGomokuMessage extends AbstractMessage {
    
    private int gameID = -1;
    
    public AbstractGomokuMessage() {
    }
    public AbstractGomokuMessage(GomokuGame game) {
        this.gameID = game.getID();
    }
    
    public int getGameID() {
        return gameID;
    }
}

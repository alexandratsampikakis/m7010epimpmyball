/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuDrawMessage extends AbstractGomokuMessage {
    
    public long id1;
    public long id2;
    
    public GomokuDrawMessage() {
    }

    public GomokuDrawMessage(GomokuGame game) {
        super(game);
        id1 = game.getFirstPlayer().getID();
        id2 = game.getSecondPlayer().getID();
    }
}
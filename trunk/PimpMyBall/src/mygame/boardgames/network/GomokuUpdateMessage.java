/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.util.GridPoint;
import mygame.boardgames.CellColor;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuUpdateMessage extends AbstractGomokuMessage {
    
    public long playerID;
    public GridPoint p;
    public CellColor color;
    
    public GomokuUpdateMessage() {
    }
    public GomokuUpdateMessage(GomokuGame game, CellColor color, GridPoint p) {
        super(game);
        this.playerID = game.getFirstPlayer().getColor() == color ?
                game.getFirstPlayer().getID() : game.getSecondPlayer().getID();
        this.p = p;
        this.color = color;
    }
}

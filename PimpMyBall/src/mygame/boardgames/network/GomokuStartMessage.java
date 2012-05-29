/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.util.GridSize;
import mygame.boardgames.CellColor;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GomokuStartMessage extends AbstractGomokuMessage {
    
    public Vector3f firstPlayerPos, secondPlayerPos;
    public long firstPlayerID, secondPlayerID;
    public CellColor startingColor;
    public GridSize boardSize;
    
    public GomokuStartMessage() {
    }
    
    public GomokuStartMessage(GomokuGame game, Vector3f firstPos, Vector3f secondPos) {
        super(game);
        
        firstPlayerID = game.getFirstPlayer().getID();
        secondPlayerID = game.getSecondPlayer().getID();
        startingColor = game.getFirstPlayer().getColor();
        boardSize = game.getGrid().getSize();
        
        firstPlayerPos = firstPos;
        secondPlayerPos = secondPos;
    }
}

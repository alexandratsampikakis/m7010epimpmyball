/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.network;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridSize;

/**
 *
 * @author Jimmy
 */
@Serializable
public class NewGameMessage  extends AbstractMessage {
    
    private final int gameID;
    private boolean start;
    private GridSize gridSize;
            
    public NewGameMessage() {
        gameID = 0;
        setReliable(true);
    }
    
    public NewGameMessage(GomokuGame game, boolean start) {
        this.gameID = game.getID();
        this.gridSize = new GridSize(game.getGrid().getSize());
        this.start = start;
        setReliable(true);
    }

    private NewGameMessage(NewGameMessage msg) {
        this.gameID = msg.gameID;
        this.gridSize = new GridSize(msg.gridSize);
        this.start = !msg.start;
        setReliable(true);
    }
    
    public NewGameMessage opponentMessage() {
        return new NewGameMessage(this);
    }
    
    public boolean isMyTurn() {
        return start;
    }
    public int getGameID() {
        return gameID;
    }
    public GridSize getGridSize() {
        return gridSize;
    }
}

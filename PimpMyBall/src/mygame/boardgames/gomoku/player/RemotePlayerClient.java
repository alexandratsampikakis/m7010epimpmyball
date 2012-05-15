/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku.player;

import com.jme3.network.Client;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.network.GomokuMessage;
import mygame.boardgames.network.NewGameMessage;

/**
 *
 * @author Jimmy
 */
public class RemotePlayerClient extends GomokuPlayer {
    
    private Client client;
    
    public RemotePlayerClient(Client client) {
        this.client = client;
    }
    
    @Override
    public void onOpponentMove(GridPoint p) {
        client.send(new GomokuMessage(game, p));
    }

    @Override
    public void onStartGame(boolean myTurn) {
        // client.send(new NewGameMessage(game, myTurn));
    }

    @Override
    public void onGameWon(CellColor winningColor) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onOpponentSurrender() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }
}

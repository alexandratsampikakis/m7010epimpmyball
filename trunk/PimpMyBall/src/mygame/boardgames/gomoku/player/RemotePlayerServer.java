/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku.player;

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.network.GomokuMessage;
import mygame.boardgames.GridPoint;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.network.NewGameMessage;

/**
 *
 * @author Jimmy
 */
public class RemotePlayerServer extends GomokuPlayer {

    private Server server;
    private HostedConnection connection;
    
    public RemotePlayerServer(Server server, HostedConnection connection) {
        this.server = server;
        this.connection = connection;
    }
    
    public HostedConnection getConnection() {
        return connection;
    }
    
    @Override
    public void onOpponentMove(GridPoint p) {
        server.broadcast(Filters.in(connection), new GomokuMessage(game, p));
    }

    @Override
    public void onStartGame(boolean myTurn) {
        server.broadcast(Filters.in(connection), new NewGameMessage(game, myTurn));
    }
    
    @Override
    public void onGameWon(CellColor winningColor) {   
        
    }
    @Override
    public void onOpponentSurrender() {   
    }
}

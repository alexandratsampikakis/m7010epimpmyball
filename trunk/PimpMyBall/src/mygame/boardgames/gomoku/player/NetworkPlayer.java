/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boardgames.gomoku.player;

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import boardgames.GomokuGame;
import boardgames.network.GomokuMessage;
import boardgames.GridPoint;
import boardgames.network.NewGameMessage;

/**
 *
 * @author Jimmy
 */
public class NetworkPlayer extends GomokuPlayer {

    private Server server;
    private HostedConnection connection;
    
    public NetworkPlayer(Server server, HostedConnection connection) {
        this.server = server;
        this.connection = connection;
    }
    
    public HostedConnection getConnection() {
        return connection;
    }
    
    @Override
    public void onOpponentMove(GridPoint p) {
        server.broadcast(Filters.in(connection), new GomokuMessage(p));
    }

    @Override
    public void onStartGame(boolean myTurn) {
        server.broadcast(Filters.in(connection), new NewGameMessage(game, myTurn));
    }
    
    @Override
    public void onGameWon(boolean didWin) {
        
    }
    @Override
    public void onOpponentSurrender() {
        
    }
}

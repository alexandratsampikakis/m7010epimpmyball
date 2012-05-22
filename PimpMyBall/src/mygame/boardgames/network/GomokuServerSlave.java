
package mygame.boardgames.network;

import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.HashMap;
import java.util.Random;
import mygame.balls.server.BallServer;
import mygame.balls.server.User;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.WinningRow;
import mygame.boardgames.gomoku.player.GomokuPlayer;
import mygame.boardgames.gomoku.player.RemotePlayerServer;

/**
 * @author Jimmy
 */
public class GomokuServerSlave {

    /*
    public interface SlaveDriver {
        public void onGameEnded();
    }
     */
    
    private BallServer owner;
    private Random rand = new Random();
    private Server server;
    private HashMap<Integer, GomokuGame> hostedGames = new HashMap<Integer, GomokuGame>();

    public GomokuServerSlave(BallServer owner, Server server) {

        this.owner = owner;
        this.server = server;
        
        server.addMessageListener(new MessageListener<HostedConnection>() {
            public void messageReceived(HostedConnection source, Message m) {
                
                if (m instanceof GomokuMessage) {
                    
                    GomokuMessage gm = (GomokuMessage) m;
                    GomokuGame game = hostedGames.get(gm.gameID);
                    GomokuPlayer cp = (game == null) ? null : game.getCurrentPlayer();
                    
                    if (game != null && cp instanceof RemotePlayerServer) {
                        
                        RemotePlayerServer p = (RemotePlayerServer) cp;
                        
                        if (source == p.getConnection()) {
                            game.tryMove(p, gm.p);
                        }
                    }
                }
                // TODO: Svara med MoveFailedMessage!!!!!!!!!
            }
        });
    }
    
    public void startGame(User u1, User u2) {
        
        RemotePlayerServer p1 = new RemotePlayerServer(server, u1);
        // AIPlayer p2 = new AIPlayer();
        RemotePlayerServer p2 = new RemotePlayerServer(server, u2);
        
        GomokuGame newGame = new GomokuGame();
        boolean first = rand.nextBoolean();
        newGame.setPlayers(first ? p1 : p2, first ? p2 : p1);

        newGame.addListener(new GomokuGame.Listener() {
            public void onMove(GomokuGame game, CellColor color, GridPoint p) {
                owner.broadcastGomokuUpdate(game, color, p);
            }
            public void onWin(GomokuGame game, WinningRow wr) {
                owner.broadcastGomokuGameFinished(game, wr);
            }
            public void onReset(GomokuGame game) {
            }
        });
        
        hostedGames.put(newGame.getID(), newGame);
        newGame.start();
        
        owner.broadcastGomokuGameStarted(newGame);
    }
}


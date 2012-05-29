
package mygame.balls.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.HashMap;
import java.util.Random;
import mygame.balls.Ball;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GomokuPlayer;
import mygame.util.GridPoint;
import mygame.boardgames.CellColor;
import mygame.boardgames.WinningRow;
import mygame.boardgames.network.GomokuDrawMessage;
import mygame.boardgames.network.GomokuEndMessage;
import mygame.boardgames.network.GomokuStartMessage;
import mygame.boardgames.network.GomokuUpdateMessage;

/**
 * @author Jimmy
 */
public class GomokuServerSlave {
    
    private BallServer owner;
    private Random rand = new Random();
    private Server server;
    private HashMap<Integer, GomokuGame> hostedGames = new HashMap<Integer, GomokuGame>();
    private HashMap<Long, GomokuGame> players = new HashMap<Long, GomokuGame>();
            
    public GomokuServerSlave(BallServer owner, Server server) {

        this.owner = owner;
        this.server = server;
        
        server.addMessageListener(new MessageListener<HostedConnection>() {
            public void messageReceived(HostedConnection source, Message m) {
                
                if (m instanceof GomokuUpdateMessage) {
                    
                    GomokuUpdateMessage gm = (GomokuUpdateMessage) m;
                    GomokuGame game = hostedGames.get(gm.getGameID());
                    GomokuPlayer player = (game == null) ? null : game.getCurrentPlayer();
                    
                    if (game != null) {
                        long playerID = player.getID();
                        if (playerID == source.getAttribute("ID")) {
                            game.tryMove(playerID, gm.p);
                        }
                    }
                }
                // TODO: Svara med MoveFailedMessage?
            }
        }, GomokuUpdateMessage.class);
    }
    
    public void startGame(User u1, User u2) {

        boolean first = rand.nextBoolean();
        final long firstID = first ? u1.getId() : u2.getId();
        final long secondID = first ? u2.getId() : u1.getId();
        final GomokuGame newGame = new GomokuGame(firstID, secondID);
        
        newGame.addListener(new GomokuGame.Listener() {
            public void onMove(GomokuGame game, CellColor color, GridPoint p) {
                broadcastGomokuUpdate(game, color, p);
            }
            public void onWin(GomokuGame game, WinningRow wr) {
                broadcastGomokuGameFinished(game, wr);
                hostedGames.remove(game.getID());
                players.remove(game.getFirstPlayer().getID());
                players.remove(game.getSecondPlayer().getID());
            }
            public void onReset(GomokuGame game) {
            }
            public void onDraw(GomokuGame game) {
                broadcastGomokuGameDraw(game);
                hostedGames.remove(game.getID());
                players.remove(game.getFirstPlayer().getID());
                players.remove(game.getSecondPlayer().getID());
            }
        });
        
        hostedGames.put(newGame.getID(), newGame);
        players.put(firstID, newGame);
        players.put(secondID, newGame);

        broadcastGomokuGameStarted(newGame);
    }
    
    public void playerLeftGame(User u1) {
        
        GomokuGame game = players.get(u1.getId());
        
        if (game != null) {
            hostedGames.remove(game.getID());
            players.remove(game.getFirstPlayer().getID());
            players.remove(game.getSecondPlayer().getID());
            
            broadcastGomokuGameSurrender(game, u1);
        }
    }
    
    
    
    public void broadcastGomokuUpdate(GomokuGame game, CellColor color, GridPoint p) {
        server.broadcast(new GomokuUpdateMessage(game, color, p));
    }

    public void broadcastGomokuGameStarted(GomokuGame game) {
        
        User u1 = owner.getUser(game.getFirstPlayer().getID());
        User u2 = owner.getUser(game.getSecondPlayer().getID());
        
        server.broadcast(new GomokuStartMessage(
                game, u1.getBall().getPosition(), u2.getBall().getPosition()));
    }

    public void broadcastGomokuGameFinished(GomokuGame game, WinningRow row) {

        long winnerID = game.getFirstPlayer().getColor() == row.getWinningColor() ?
                game.getFirstPlayer().getID() : game.getSecondPlayer().getID();
        long loserID = game.getOpponentID(winnerID);
       
        User winner = owner.getUser(winnerID);
        User loser = owner.getUser(loserID);
        
        winner.getBall().setMass(Ball.defaultMass);
        winner.setImmortal();
        loser.getBall().setMass(Ball.defaultMass);
        loser.setImmortal();

        int scoreChange = updateScore(winner, loser);

        server.broadcast(new GomokuEndMessage(game, row, scoreChange));
    }
    
    public void broadcastGomokuGameSurrender(GomokuGame game, User loser) {

        long winnerID = game.getOpponentID(loser.getId());
        User winner = owner.getUser(winnerID);
        
        winner.getBall().setMass(Ball.defaultMass);
        winner.setImmortal();

        int scoreChange = updateScore(winner, loser);
        
        server.broadcast(new GomokuEndMessage(game, winnerID, scoreChange));
    }
    
    public void broadcastGomokuGameDraw(GomokuGame game) {
        server.broadcast(new GomokuDrawMessage(game));
    }
    
    private int updateScore(User winner, User loser) {
        
        float winnerRank = winner.getUserData().rank;
        float loserRank = loser.getUserData().rank;
        int scoreChange = (int) Math.max(25 * loserRank / winnerRank, 1);
        
        winner.getUserData().rank += scoreChange;
        loser.getUserData().rank = (int) Math.max(loserRank - scoreChange, 1);
        
        return scoreChange;
    }
}


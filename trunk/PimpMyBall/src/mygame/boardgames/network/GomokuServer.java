package mygame.boardgames.network;

import mygame.boardgames.gomoku.GomokuAI;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.GomokuGrid;
import mygame.boardgames.gomoku.WinningRow;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;
import mygame.boardgames.GridSize;

import mygame.boardgames.Select3D;
import mygame.boardgames.gomoku.GomokuBoard3D;
import mygame.boardgames.gomoku.player.AIPlayer;
import mygame.boardgames.gomoku.player.GomokuPlayer;
import mygame.boardgames.gomoku.player.RemotePlayerServer;

/**
 * @author Jimmy
 */
public class GomokuServer {

    public static final String NAME = "Gomoku Server";
    public static final int VERSION = 1;
    public static final int PORT = 5110;
    public static final int UDP_PORT = 5110;

    private Random rand = new Random();
    private Server server;
    private ArrayList<HostedConnection> hostedConnections = new ArrayList<HostedConnection>();
    private HashMap<Integer, GomokuGame> hostedGames = new HashMap<Integer, GomokuGame>();

    public static void initializeClasses() {
        Serializer.registerClass(GridPoint.class);
        Serializer.registerClass(GridSize.class);
        Serializer.registerClass(GomokuMessage.class);
        Serializer.registerClass(NewGameMessage.class);
    }
    
    public static void main(String[] args) throws Exception {
        GomokuServer server = new GomokuServer();
        server.server.start();
        
        // Keep running basically forever
        synchronized (server) {
            server.wait();
        }
    }
    
    public GomokuServer() throws Exception {
        
        initializeClasses();

        server = Network.createServer(NAME, VERSION, PORT, UDP_PORT);
        server.addMessageListener(new MessageListener<HostedConnection>() {
            public void messageReceived(HostedConnection source, Message m) {
                if (m instanceof GomokuMessage) {
                    // app.enqueue(app.new MyCallable((GomokuMessage) m));
                    GomokuMessage gm = (GomokuMessage) m;
                    
                    System.out.println("Servern tar emot!! AHA :D id:" + gm.gameID);
                    
                    GomokuGame game = hostedGames.get(gm.gameID);
                    GomokuPlayer cp = (game == null) ? null : game.getCurrentPlayer();
                    
                    if (game != null && cp instanceof RemotePlayerServer) {
                        RemotePlayerServer p = (RemotePlayerServer) cp;
                        if (source == p.getConnection()) {
                            game.tryMove(p, gm.p);
                        }
                    }
                }
            }
        });
        server.addConnectionListener(new ConnectionListener() {
            public void connectionAdded(Server server, HostedConnection conn) {
                
                hostedConnections.add(conn);
                
                int size = hostedConnections.size();
                
                // startGame(conn, null);
                
                if (size == 2) {
                    startGame(hostedConnections.get(size - 2), hostedConnections.get(size - 1));
                }
            }
            public void connectionRemoved(Server server, HostedConnection conn) {
                hostedConnections.remove(conn);
            }
        });
    }
    
    public void startGame(HostedConnection c1, HostedConnection c2) {
        
        RemotePlayerServer p1 = new RemotePlayerServer(server, c1);
        // AIPlayer p2 = new AIPlayer();
        RemotePlayerServer p2 = new RemotePlayerServer(server, c2);
        
        GomokuGame newGame = new GomokuGame();
        boolean first = rand.nextBoolean();
        newGame.setPlayers(first ? p1 : p2, first ? p2 : p1);
        
        hostedGames.put(newGame.getID(), newGame);
        newGame.start();
    }
}

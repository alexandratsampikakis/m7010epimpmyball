package boardgames.network;

import boardgames.gomoku.GomokuAI;
import boardgames.gomoku.CellColor;
import boardgames.gomoku.GomokuGrid;
import boardgames.gomoku.WinningRow;
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
import boardgames.GomokuGame;
import boardgames.GridPoint;
import boardgames.GridSize;

import boardgames.Select3D;
import boardgames.gomoku.GomokuNode;
import boardgames.gomoku.player.AIPlayer;
import boardgames.gomoku.player.GomokuPlayer;
import boardgames.gomoku.player.NetworkPlayer;

/**
 * test
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
                    GomokuGame game = hostedGames.get(gm.gameID);
                    GomokuPlayer cp = game.getCurrentPlayer();
                    
                    if (game != null && cp instanceof NetworkPlayer) {
                        NetworkPlayer p = (NetworkPlayer) cp;
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
        
        NetworkPlayer p1 = new NetworkPlayer(server, c1);
        // AIPlayer p2 = new AIPlayer();
        NetworkPlayer p2 = new NetworkPlayer(server, c2);
        
        GomokuGame newGame = new GomokuGame(p1, p2, rand.nextBoolean());            
        hostedGames.put(newGame.getID(), newGame);
        newGame.start();
    }
}

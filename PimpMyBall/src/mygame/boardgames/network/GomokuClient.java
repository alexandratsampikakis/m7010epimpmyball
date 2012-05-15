package mygame.boardgames.network;

import mygame.boardgames.network.GomokuServer;
import mygame.boardgames.network.GomokuMessage;
import mygame.boardgames.network.NewGameMessage;
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
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import java.awt.Component;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;

import mygame.boardgames.Select3D;
import mygame.boardgames.Select3D;
import mygame.boardgames.gomoku.GomokuBoard3D;
import mygame.boardgames.gomoku.player.GomokuPlayer;
import mygame.boardgames.gomoku.player.LocalPlayer;
import mygame.boardgames.gomoku.player.RemotePlayerClient;
import mygame.boardgames.gomoku.player.RemotePlayerServer;

/**
 * test
 * @author Jimmy
 */
public class GomokuClient extends SimpleApplication implements ActionListener {

    private static boolean USE_CURSOR = false;
    
    private GomokuBoard3D board;
    private GomokuGame game;
    private Client client;
    
    private GomokuPlayer remotePlayer = null;
    private GomokuPlayer localPlayer = new LocalPlayer();
    
    
    public static void main(String[] args) throws Exception {
        
        GomokuServer.initializeClasses();

        // Grab a host string from the user
        String s = getString(null, "Host Info", "Enter gomoku host:", "localhost");
        if (s == null) {
            System.out.println("User cancelled.");
            return;
        }

        GomokuClient client = new GomokuClient();
        client.setHost(s);
        client.start();
    }

    public static String getString(Component owner, String title, String message, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                owner, message, title, 
                JOptionPane.PLAIN_MESSAGE,
                null, null, initialValue);
    }
    
    public void setHost(String host) throws IOException {
        client = Network.connectToServer(GomokuServer.NAME, GomokuServer.VERSION,
                host, GomokuServer.PORT, GomokuServer.UDP_PORT);
        
        MessageListener<Client> msgListener = new MessageListener<Client>() {
            public void messageReceived(Client source, Message m) {
                enqueue(new MessageParser(m));
            }
        };
        client.addMessageListener(msgListener, 
                GomokuMessage.class, NewGameMessage.class);
        client.start();
        
        // Create the remote player
        remotePlayer = new RemotePlayerClient(client);
    }
    
    private void startNewGame(NewGameMessage msg) {
        
        // Create a new game as specified by the message
        game = new GomokuGame(msg);
        
        // Add players to the game, in correct order
        if (msg.isMyTurn()) {
            game.setPlayers(localPlayer, remotePlayer);
        } else {
            game.setPlayers(remotePlayer, localPlayer);
        }
        
        // Create a 3D model of the board
        board = new GomokuBoard3D(assetManager, game);
        board.setLocalScale(0.5f);
        rootNode.attachChild(board);
        
        // Start the game
        game.start();
    }

    @Override
    public void simpleInitApp() {

        // Mouse
        inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("restart", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "click", "restart");
        
        initControls();
    }

    @Override
    public void simpleUpdate(float tpf) {}
    @Override
    public void simpleRender(RenderManager rm) {}

    public void onAction(String name, boolean isPressed, float tpf) {

        if (!isPressed) { // key was released
            if (name.equals("click")) {
                // Find the position on the board that was clicked
                CollisionResult closest;

                if (USE_CURSOR) {
                    Vector2f click2d = inputManager.getCursorPosition();
                    closest = Select3D.select(click2d, cam, board);
                } else {
                    closest = Select3D.select(cam, board);
                }

                if (closest != null) {

                    GridPoint p = closest.getGeometry().getUserData("pos");
                    
                    if (p != null) {
                        game.tryMove(localPlayer, p);
                    }
                }
                
            } else if (name.equals("restart")) {
                resetBoard();
            }
        }
    }
    
    /** Create crosshairs or cursor */
    private void initControls() {
        
        if (USE_CURSOR) {
            
            flyCam.setEnabled(false);
            
            Texture tex = assetManager.loadTexture("Interface/Logo/Cursor.png");
            cursor = new Picture("cursor");
            cursor.setTexture(assetManager, (Texture2D) tex, true);
            cursor.setWidth(64);
            cursor.setHeight(64);
            guiNode.attachChild(cursor);

            inputManager.addRawInputListener(inputListener);
            inputManager.setCursorVisible(false);
            
        } else {
            
            flyCam.setMoveSpeed(0f);
            
            guiNode.detachAllChildren();
            guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
            BitmapText ch = new BitmapText(guiFont, false);
            int size = guiFont.getCharSet().getRenderedSize();
            ch.setSize(size * 2);
            ch.setText("+");        // fake crosshairs :)
            ch.setLocalTranslation( // center
                    settings.getWidth() / 2 - size / 3 * 2,
                    settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
            guiNode.attachChild(ch);
        }
    }
    
    private void resetBoard() {
        /*board.reset();
        grid.reset();
        ai = new GomokuAI(grid); // , GomokuCellState.BLUE);
        LOCKED = false;*/
    }
    
    
    private Picture cursor;
    private RawInputListener inputListener = new RawInputListener() {

        private float x = 0, y = 0;

        public void beginInput() {}
        public void endInput() {}
        public void onJoyAxisEvent(JoyAxisEvent evt) {}
        public void onJoyButtonEvent(JoyButtonEvent evt) {}
        public void onMouseMotionEvent(MouseMotionEvent evt) {
            
            x = evt.getX(); // += evt.getDX();
            y = evt.getY(); // += evt.getDY();

            // Prevent mouse from leaving screen
            AppSettings settings = GomokuClient.this.settings;
            x = FastMath.clamp(x, 0, settings.getWidth());
            y = FastMath.clamp(y, 0, settings.getHeight());
            
            // adjust for hotspot
            cursor.setPosition(x, y - 64);
        }
        public void onMouseButtonEvent(MouseButtonEvent evt) {}
        public void onKeyEvent(KeyInputEvent evt) {}
        public void onTouchEvent(TouchEvent evt) {}
    };

    
    
    private class MessageParser implements Callable {
        
        private Message msg;
        
        public MessageParser(Message msg) {
            this.msg = msg;
        }
        
        @Override
        public Object call() throws Exception {
            if (msg instanceof GomokuMessage) {
                game.tryMove(remotePlayer, ((GomokuMessage) msg).p);
            } else if (msg instanceof NewGameMessage) {
                startNewGame((NewGameMessage) msg);
            }
            return msg;
        }
    }

}
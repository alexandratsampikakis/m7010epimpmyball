/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import com.jme3.app.Application;
import mygame.boardgames.network.GomokuServer;
import mygame.boardgames.network.GomokuMessage;
import mygame.boardgames.network.NewGameMessage;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.GomokuGrid;
import mygame.boardgames.gomoku.WinningRow;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.MotionAllowedListener;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
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
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import java.awt.Component;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import mygame.balls.UserData;
import mygame.balls.client.BallClient;
import mygame.balls.client.User;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;

import mygame.boardgames.Select3D;
import mygame.boardgames.gomoku.GomokuBoard3D;
import mygame.boardgames.gomoku.player.GomokuPlayer;
import mygame.boardgames.gomoku.player.LocalPlayer;
import mygame.boardgames.gomoku.player.RemotePlayerClient;
import mygame.boardgames.gomoku.player.RemotePlayerServer;
import mygame.boardgames.network.GomokuClient;
import mygame.boardgames.network.broadcast.GomokuStartMessage;


/**
 *
 * @author Jimmy
 */
public class BoardGameAppState extends AbstractAppState implements ActionListener {

    private static boolean USE_CURSOR = false;
    
    private GomokuBoard3D board;
    private GomokuGame game;
   
    private BallClient app;
    private NewGameMessage msg;
    private Client client;
   
    private void setText(String text) {
        setText(text, ColorRGBA.White);
    }
    private void setText(String text, ColorRGBA color) {
        Node guiNode = app.getGuiNode();
        AssetManager assetManager = app.getAssetManager();
        
        Spatial child = guiNode.getChild("DisplayText");
        
        if (child == null) {
            BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/HelveticaNeue.fnt");
            BitmapText ch = new BitmapText(guiFont, false);
            int size = guiFont.getCharSet().getRenderedSize();
            ch.setSize(size);
            ch.setText(text);
            ch.setLocalTranslation(16, app.getCamera().getHeight() - 16, 0);
            ch.setName("DisplayText");
            ch.setColor(color);
            guiNode.attachChild(ch);
            
        } else {
            ((BitmapText) child).setText(text);
            ((BitmapText) child).setColor(color);
        }
    }
    
    
    private GomokuPlayer remotePlayer = null;
    private GomokuPlayer localPlayer = new GomokuPlayer() {
        @Override
        public void onOpponentMove(GridPoint p) {
            setText("Your turn! ", localPlayer.getColor().getColorRGBA());
        }
        @Override
        public void onStartGame(boolean myTurn) {
            if (myTurn)
                setText("Game started, your turn.", localPlayer.getColor().getColorRGBA());
            else
                setText("Game started, waiting for opponent", remotePlayer.getColor().getColorRGBA());
        }
        @Override
        public void onGameWon(CellColor winningColor) {
            setText(((color == winningColor) ? "You win!" : "You lose, haha..."), winningColor.getColorRGBA());
        }
        @Override
        public void onOpponentSurrender() {
            setText("Opponent surrendered, you win!");
        }
    };
    
    
    public BoardGameAppState(BallClient app, Client client) {
        super();
        
        this.app = app;
        this.client = client;
        
        client.addMessageListener(new MessageListener<Client>() {
            public void messageReceived(Client source, Message m) {
                BoardGameAppState.this.app.enqueue(new MessageParser(m));
            }
        }, GomokuMessage.class);
        
        // Create the remote player
        remotePlayer = new RemotePlayerClient(client);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        InputManager inputManager = app.getInputManager();
        
        // Mouse
        inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "click");
        
        initControls();
        
        System.out.println("Initializing " + this);
    }
   
    @Override
    public void stateAttached(AppStateManager stateManager) {
        super.stateAttached(stateManager);

        app.getChaseCamera().setEnabled(false);

        System.out.println("Attached " + this + ".");
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        
        app.chasePlayer();
        app.getRootNode().detachChild(board);
        
        System.out.println("Detached " + this + ".");
    }
    
    
    private static float ANIMATION_TIME = 5f;
    
    private float animationTime = 0f;
    private boolean animateCamera = false;
    private Vector3f cameraStart, cameraDest;
    private Vector3f boardStart, boardDest;
    private Vector3f lookAtStart, lookAtDest;
    
    public GomokuGame startNewGame(GomokuStartMessage msg) {
        
        // Create a new game as specified by the message
        game = new GomokuGame(msg);
        
        User player = app.getPlayer();
        UserData playerData = player.getUserData();
        
        // Add players to the game, in correct order
        if (msg.firstPlayerID == playerData.id) {
            game.setPlayers(localPlayer, remotePlayer);
        } else {
            game.setPlayers(remotePlayer, localPlayer);
        }
        
        Vector3f myPos = (msg.firstPlayerID == player.getId()) ?
                msg.firstPlayerPos : msg.secondPlayerPos;
        Vector3f oppPos = (msg.firstPlayerID == player.getId()) ?
                msg.secondPlayerPos : msg.firstPlayerPos;
        
        Vector3f fromTo = oppPos.add(myPos.negate()).mult(0.5f);
        Vector3f boardPos = myPos.add(fromTo).add(0, 4, 0);
        Vector3f cameraPos = myPos.add(fromTo.negate().mult(1.5f)).add(0, 3, 0);
        
        animationTime = ANIMATION_TIME;
        animateCamera = true;
        
        cameraStart = app.getCamera().getLocation().clone();
        cameraDest = cameraPos;
        
        boardStart = boardPos.add(0, -10, 0);
        boardDest = boardPos;
        
        lookAtStart = cameraStart.add(app.getCamera().getDirection());
        lookAtDest = boardDest.clone();

        // Create a 3D model of the board
        fromTo.y = 0;
        Quaternion q = new Quaternion();
        q.lookAt(fromTo.normalize(), Vector3f.UNIT_Y);
        
        board = new GomokuBoard3D(app.getAssetManager(), game);
        board.setLocalRotation(q);
                // app.getCamera().getRotation());
        board.setLocalTranslation(boardPos.add(0, -10, 0));
        board.setLocalScale(0.25f);
       
        app.getRootNode().attachChild(board);
        
        // Start the game
        game.start();
        
        return game;
    }

    @Override
    public void update(float tpf) {
        
        if (animateCamera) {
            
            if (animationTime > 0) {

                Camera cam = app.getCamera();

                float percent = 1f - animationTime / ANIMATION_TIME;
 
                System.out.println(tpf);
 
                // Vector3f temp;
                
                // temp = cameraStart.clone();
                cam.setLocation(cameraStart.interpolate(cameraDest, tpf));
                // cameraStart = temp;
                
                // temp = lookAtStart.clone();
                cam.lookAt(lookAtStart.interpolate(lookAtDest, tpf), Vector3f.UNIT_Y);
                // lookAtStart = temp;
                
                // temp = boardStart.clone();
                board.setLocalTranslation(boardStart.interpolate(boardDest, tpf));
                // boardStart = temp;
                
                animationTime -= tpf;
                
            } else {

                app.getFlyByCamera().setEnabled(true);
                app.getFlyByCamera().setMoveSpeed(0);
                animateCamera = false;
            }
        }
    }
    
    @Override
    public void render(RenderManager rm) {}

    public void onAction(String name, boolean isPressed, float tpf) {

        if (!isPressed) { // key was released
            if (name.equals("click")) {
                // Find the position on the board that was clicked
                CollisionResult closest;

                if (USE_CURSOR) {
                    Vector2f click2d = app.getInputManager().getCursorPosition();
                    closest = Select3D.select(click2d, app.getCamera(), board);
                } else {
                    closest = Select3D.select(app.getCamera(), board);
                }

                if (closest != null) {
                    
                    GridPoint p = closest.getGeometry().getUserData("pos");
                    
                    if (p != null && game.tryMove(localPlayer, p)) {
                        setText("Waiting for opponent. ", remotePlayer.getColor().getColorRGBA());
                    }
                }
            }
        }
    }
    
    /** Create crosshairs or cursor */
    private void initControls() {
        
        FlyByCamera flyCam = app.getFlyByCamera();
        AssetManager assetManager = app.getAssetManager();
        Node guiNode = app.getGuiNode(); 
        InputManager inputManager = app.getInputManager();
        
        if (USE_CURSOR) {
            
            flyCam.setEnabled(false);
            
            Texture tex = assetManager.loadTexture("Interface/Logo/Cursor.png");
            cursor = new Picture("cursor");
            cursor.setTexture(assetManager, (Texture2D) tex, true);
            cursor.setWidth(64);
            cursor.setHeight(64);
            app.getGuiNode().attachChild(cursor);

            inputManager.addRawInputListener(inputListener);
            inputManager.setCursorVisible(false);
            
        } else {
            
            flyCam.setMoveSpeed(0f);
            
            BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
            BitmapText ch = new BitmapText(guiFont, false);
            int size = guiFont.getCharSet().getRenderedSize();
            ch.setSize(size * 2);
            ch.setText("+");        // fake crosshairs :)
            ch.setLocalTranslation( // center
                    app.getCamera().getWidth() / 2 - size / 3 * 2,
                    app.getCamera().getHeight() / 2 + ch.getLineHeight() / 2, 0);
            guiNode.attachChild(ch);
        }
    }
    
    private Picture cursor;
    private RawInputListener inputListener = new RawInputListener() {
        public void beginInput() {}
        public void endInput() {}
        public void onJoyAxisEvent(JoyAxisEvent evt) {}
        public void onJoyButtonEvent(JoyButtonEvent evt) {}
        public void onMouseMotionEvent(MouseMotionEvent evt) { 
            // Prevent mouse from leaving screen
            float x = FastMath.clamp(evt.getX(), 0, app.getCamera().getWidth());
            float y = FastMath.clamp(evt.getY(), 0, app.getCamera().getHeight());
            // Adjust for hotspot
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
            }
            return msg;
        }
    }
    
}

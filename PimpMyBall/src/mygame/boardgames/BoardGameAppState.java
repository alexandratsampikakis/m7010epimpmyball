/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.concurrent.Callable;
import mygame.admin.ChatMessage;
import mygame.balls.UserData;
import mygame.balls.client.BallClient;
import mygame.balls.client.User;
import mygame.util.GridPoint;

import mygame.util.Select3D;
import mygame.boardgames.network.GomokuDrawMessage;
import mygame.boardgames.network.GomokuEndMessage;
import mygame.boardgames.network.GomokuStartMessage;
import mygame.boardgames.network.GomokuUpdateMessage;


/**
 *
 * @author Jimmy
 */
public class BoardGameAppState extends AbstractAppState {

    private BallClient app;
    private Client client;
    
    private GomokuGame currentGame;
    private CellColor myColor = CellColor.NONE;
    
    private HashMap<Integer, GomokuBoard3D> currentGames =
            new HashMap<Integer, GomokuBoard3D>();
    
    private static float ANIMATION_TIME = 3f;
    private float animationTime = 0f;
    private boolean animateCamera = false;
    private Vector3f cameraStart, cameraDest;
    private Vector3f lookAtStart, lookAtDest;
    
    public BoardGameAppState(BallClient app, Client client) {
        
        this.app = app;
        this.client = client;

        client.addMessageListener(new GomokuMessageListener(),
                GomokuStartMessage.class,
                GomokuEndMessage.class,
                GomokuUpdateMessage.class);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);  
        app.getInputManager().addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, "click");
    }
    
    public GomokuGame startNewGame(GomokuStartMessage msg) {
        
        // Create a new game as specified by the message
        GomokuGame newGame = new GomokuGame(msg);
        GomokuBoard3D newBoard = new GomokuBoard3D(app.getAssetManager(), newGame);
        User player = app.getPlayer();
        long pid = player.getId();
        
        boolean myTurn = msg.firstPlayerID == pid;
        
        // If the local player is involved in the game,
        // start a camera animation towards the board
        if (myTurn || msg.secondPlayerID == pid) {
            
            app.getChaseCamera().setEnabled(false);
            initControls();
            
            Vector3f myPos, oppPos;
            
            if (myTurn) {
                myColor = msg.startingColor;
                myPos = msg.firstPlayerPos;
                oppPos = msg.secondPlayerPos;
                setText("Game started, your turn.", msg.startingColor.getColorRGBA());
            } else {
                myColor = msg.startingColor.opponent();
                oppPos = msg.firstPlayerPos;
                myPos = msg.secondPlayerPos;
                setText("Game started, waiting for opponent", msg.startingColor.getColorRGBA());
            }
            
            Vector3f fromTo = oppPos.add(myPos.negate()).mult(0.5f);
            Vector3f boardPos = myPos.add(fromTo).add(0, 5, 0);
            Vector3f cameraPos = myPos.add(fromTo.negate().mult(1.5f)).add(0, 4, 0);
            
            animationTime = ANIMATION_TIME;
            animateCamera = true;
        
            cameraStart = app.getCamera().getLocation().clone();
            cameraDest = cameraPos;

            lookAtStart = myPos.clone();
            lookAtDest = boardPos.clone();
        
            currentGame = newGame;
            currentGame.addListener(localGameListener);
            
        }
        
        newBoard.positionBetween(msg.firstPlayerPos, msg.secondPlayerPos);
        currentGames.put(newGame.getID(), newBoard);
        app.getRootNode().attachChild(newBoard);
        
        return currentGame;
    }
    
    private void endGame(int gameID) {
        
        GomokuBoard3D board = currentGames.get(gameID);
        
        if (gameID == currentGame.getID()) {                 
            app.chasePlayer();
            app.getGuiNode().detachChildNamed("CrossHairs");
            app.getGuiNode().detachChildNamed("DisplayText");
            
            currentGame.removeListener(localGameListener);
            currentGame = null;    
        }
        
        app.getRootNode().detachChild(board);
        app.showFireworks(board.getLocalTranslation());
    }
            
    private class GomokuMessageListener implements MessageListener<Client> {

        public void messageReceived(Client source, Message message) {
            app.enqueue(new GomokuMessageReceiver(message));
        }
    }

    private class GomokuMessageReceiver implements Callable {

        Message message;

        public GomokuMessageReceiver(Message message) {
            this.message = message;
        }

        public Object call() {

            if (message instanceof GomokuEndMessage) {
                GomokuEndMessage gem = (GomokuEndMessage) message;
                
                if (gem.getGameID() == currentGame.getID()) {   
                    long pid = app.getPlayer().getId();
                    if (gem.winnerID == pid) {
                        client.send(new ChatMessage("I win, +" + gem.scoreChange, pid)); 
                    } else {
                        client.send(new ChatMessage("I lose, -" + gem.scoreChange, pid)); 
                    }
                }
                endGame(gem.getGameID());
                unfreezePlayers(gem.winnerID, gem.loserID);
                app.getUser(gem.winnerID).updateScore(gem.scoreChange);
                app.getUser(gem.loserID).updateScore(-gem.scoreChange);
                
            } else if (message instanceof GomokuStartMessage) {
                GomokuStartMessage gsm = (GomokuStartMessage) message;
                startNewGame(gsm);
                freezePlayers(gsm.firstPlayerID, gsm.firstPlayerPos,
                        gsm.secondPlayerID, gsm.secondPlayerPos);

            } else if (message instanceof GomokuUpdateMessage) {
                GomokuUpdateMessage gum = (GomokuUpdateMessage) message;

                if (gum.getGameID() == currentGame.getID()) {
                    currentGame.tryMove(gum.playerID, gum.p);
                } else {
                    GomokuBoard3D board = currentGames.get(gum.getGameID());
                    if (board != null) {
                        board.setColor(gum.p, gum.color);
                    }
                }

            } else if (message instanceof GomokuDrawMessage) {
                GomokuDrawMessage gdm = (GomokuDrawMessage) message;
                
                if (gdm.getGameID() == currentGame.getID()) {
                    long pid = app.getPlayer().getId();
                    client.send(new ChatMessage("Draw...", pid));
                }
                endGame(gdm.getGameID());
                unfreezePlayers(gdm.id1, gdm.id2);
                
            } else {
                System.err.println("Received odd message:" + message);
            }

            return message;
        }
    }

    public void freezePlayers(long firstID, Vector3f firstPos,
            long secondID, Vector3f secondPos) {
        app.getUser(firstID).setFrozen(firstPos);
        app.getUser(secondID).setFrozen(secondPos);
    }
    public void unfreezePlayers(long firstID, long secondID) {
        app.getUser(firstID).setFrozen(false);
        app.getUser(secondID).setFrozen(false);
    }
    
    @Override
    public void update(float tpf) {
        
        if (animateCamera) {
            if (animationTime > 0) {

                Camera cam = app.getCamera();
                
                float percent = 1f - animationTime / ANIMATION_TIME;
                // Vector3f temp;
                // temp = cameraStart.clone();
                cam.setLocation(cameraStart.interpolate(cameraDest, percent));
                // cameraStart = temp;
                // temp = lookAtStart.clone();
                cam.lookAt(lookAtStart.interpolate(lookAtDest, percent), Vector3f.UNIT_Y);
                // lookAtStart = temp;

                animationTime -= tpf;
                
            } else {
                app.getFlyByCamera().setEnabled(true);
                app.getFlyByCamera().setMoveSpeed(0);
                animateCamera = false;
            }
        }
        
        for (GomokuBoard3D b : currentGames.values()) {
            b.animate(tpf);
        }
    }
    
    @Override
    public void render(RenderManager rm) {}

    /** Create crosshairs or cursor */
    private void initControls() {
        
        FlyByCamera flyCam = app.getFlyByCamera();
        AssetManager assetManager = app.getAssetManager();
        Node guiNode = app.getGuiNode(); 
        flyCam.setMoveSpeed(0f);
        
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/HelveticaNeue.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        int size = guiFont.getCharSet().getRenderedSize();
        ch.setName("CrossHairs");
        ch.setSize(size);
        ch.setText("+");        // fake crosshairs :)
        ch.setLocalTranslation( // center
                    app.getCamera().getWidth() / 2 - size / 3 * 2,
                    app.getCamera().getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }
    
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
    
    private GomokuGame.Listener localGameListener = new GomokuGame.Listener() {
        public void onMove(GomokuGame game, CellColor color, GridPoint p) {
            if (color == myColor) {
                client.send(new GomokuUpdateMessage(game, color, p));
                setText("Waiting for opponent. ", color.getColorRGBA());
            } else {
                setText("Your turn! ", color.getColorRGBA());
            }
            
        }
        public void onWin(GomokuGame game, WinningRow wr) {
        }
        public void onReset(GomokuGame game) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        public void onDraw(GomokuGame game) {
        }
    };
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {

            if (!isEnabled())
                return;

            if (!isPressed) { // key was released
                
                if (name.equals("click") && currentGame != null) {

                    // Find the position on the board that was clicked
                    GomokuBoard3D currentBoard = currentGames.get(currentGame.getID());
                    CollisionResult closest = Select3D.select(app.getCamera(), currentBoard);

                    if (closest != null) {
                        GridPoint p = closest.getGeometry().getUserData("pos");
                        if (p != null) {
                            currentGame.tryMove(app.getPlayer().getId(), p);
                        }
                    }
                }
            }
        }
    };
    
}

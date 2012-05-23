package mygame.balls.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.balls.messages.BallUpdateMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import java.awt.Component;
import java.util.ArrayList;
//import java.util.Timer.jme.util.Timer;
import java.util.HashMap;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import mygame.admin.CentralServer;
import mygame.admin.ChatMessage;
import mygame.admin.messages.LoginMessage;
import mygame.admin.messages.LoginSuccessMessage;
import mygame.admin.NetworkHelper;
import mygame.admin.SerializerHelper;
import mygame.admin.ServerInfo;
import mygame.balls.Ball;
import mygame.balls.BallUpdate;
import mygame.balls.TestLevel;
import mygame.balls.UserData;
import mygame.balls.messages.AggregateBallUpdatesMessage;
import mygame.balls.messages.BallDirectionMessage;
import mygame.balls.messages.ConnectedUsersMessage;
import mygame.balls.messages.HelloMessage;
import mygame.balls.messages.UserAddedMessage;
import mygame.boardgames.BoardGameAppState;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.GomokuBoard3D;
import mygame.boardgames.network.broadcast.GomokuEndMessage;
import mygame.boardgames.network.broadcast.GomokuStartMessage;
import mygame.boardgames.network.broadcast.GomokuUpdateMessage;
import mygame.util.BiMap;

public class BallClient extends SimpleApplication {

    private float timeCounter = 0f;
    private Client client;
    private BasicShadowRenderer bsr;
    private Vector3f[] points;
    protected BulletAppState viewAppState, ghostAppState;
    private BiMap<Long, User> users = new BiMap<Long, User>();
    private User playerUser;
    private Vector3f currentDirection = Vector3f.ZERO;
    private Vector3f lastSentDirection = Vector3f.ZERO;
    private boolean left = false,
            right = false,
            up = false,
            down = false;
    private TestLevel viewLevel, ghostLevel;
    private ChaseCamera chaseCamera;
    // Timer variables
    private final double smallAngle = Math.toRadians(5d); // 5 degrees in radians
    private final double shortUpdateTime = 0.1d;
    private final double longUpdateTime = 5 * shortUpdateTime;
    private double updateTime = longUpdateTime;
    
    
    private int currentGameId = -1;
    private HashMap<Integer, GomokuBoard3D> currentGames = 
            new HashMap<Integer, GomokuBoard3D>();
    
    public static boolean SHOW_ALL_GOMOKU_GAMES = true;
            
    
    //-----------------------------------
    //--Test Code
    //-----------------------------------
    private UserData playerUserData;
    static Client centralServerClient;
    static CentralServerListener centralServerListener;
    private int secret;
    private BoardGameAppState bgas;

    public static void main(String[] args) throws IOException, InterruptedException {
        SerializerHelper.initializeClasses();
        String userName = getString(null, "Login Info", "Enter username:", "nicke");
        String passWord = getString(null, "Login Info", "Enter Password:", "kass");

        ServerInfo centralServerInfo = CentralServer.info;
        centralServerClient = NetworkHelper.connectToServer(centralServerInfo);

        //centralServerClient.addMessageListener(new CentralServerListener());
        centralServerListener = new CentralServerListener();
        centralServerClient.addMessageListener(centralServerListener);
        centralServerClient.start();
        centralServerClient.send(new LoginMessage(userName, passWord));

        String stop = "STOOOP!";
        synchronized (stop) {
            stop.wait();
        }
    }

    private static class CentralServerListener implements MessageListener<Client> {

        public void messageReceived(Client source, Message message) {

            if (message instanceof LoginSuccessMessage) {

                LoginSuccessMessage loginMessage = (LoginSuccessMessage) message;
                BallClient app;
                try {
                    System.out.println("ServerInfo.NAME: " + loginMessage.serverInfo.NAME);
                    System.out.println("UserData.userName: " + loginMessage.userData.userName);

                    app = new BallClient(loginMessage.serverInfo, loginMessage.userData, loginMessage.secret);
                    app.start();
                    app.setPauseOnLostFocus(false);

                } catch (Exception ex) {
                    Logger.getLogger(BallClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static String getString(Component owner, String title, String message, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                owner, message, title,
                JOptionPane.PLAIN_MESSAGE,
                null, null, initialValue);
    }

    //-----------------------------------
    //--End of test code
    //-----------------------------------
    public BallClient(ServerInfo serverInfo, UserData userData, int secret) throws Exception {

        client = NetworkHelper.connectToServer(serverInfo);
        client.start();

        bgas = new BoardGameAppState(this, client);

        this.playerUserData = userData;
        this.secret = secret;
    }

    @Override
    public void simpleInitApp() {

        client.addMessageListener(new BallServerListener(),
                BallUpdateMessage.class,
                AggregateBallUpdatesMessage.class,
                UserAddedMessage.class,
                ConnectedUsersMessage.class);

        client.addMessageListener(new GomokuMessageListener(),
                GomokuStartMessage.class,
                GomokuEndMessage.class,
                GomokuUpdateMessage.class);

        client.send(new HelloMessage(secret, playerUserData.id));

        initAppStates();
        initKeys();
        initShadow();
        initLevel();
        setupChat();
        setupUser(playerUserData);

        playerUser = users.getValue(playerUserData.id);
        playerUser.makeBlue(assetManager);
        setCameraTarget(playerUser.getGeometry());

        client.addMessageListener(new MessageListener<Client>() {

            public void messageReceived(Client source, Message m) {
                BallClient.this.enqueue(new ChatCallable((ChatMessage) m));
                System.out.println("Received chat message. " + ((ChatMessage) m).getText());
            }
        }, ChatMessage.class);

        this.setDisplayFps(false);
        this.setDisplayStatView(false);
    }

    private class ChatCallable implements Callable {

        String text;
        long senderId;

        public ChatCallable(ChatMessage m) {
            text = m.getText();
            senderId = m.getSenderId();
        }

        public Object call() throws Exception {
            User user = users.getValue(senderId);
            user.showChatMessage(text);
            return null;
        }
    }

    private void sendBallDirectionMessage() {
        long playerId = playerUser.getId();
        Ball playerBall = playerUser.getBall();
        Vector3f playerDirection = playerBall.getDirection();
        BallDirectionMessage bdMessage = new BallDirectionMessage(playerId, playerDirection);
        client.send(bdMessage);
    }

    public void performUpdate(BallUpdate ballUpdate) {
        User user = users.getValue(ballUpdate.id);
        Ball ghost = user.getGhost();
        ghost.setPosition(ballUpdate.position);
        ghost.setVelocity(ballUpdate.velocity);
        ghost.setDirection(ballUpdate.direction);
    }

    private class BallServerListener implements MessageListener<Client> {

        public void messageReceived(Client source, Message message) {
            BallClient.this.enqueue(new MessageReceiver(message));
        }
    }

    private class MessageReceiver implements Callable {

        Message message;

        public MessageReceiver(Message message) {
            this.message = message;
        }

        public Object call() {
            if (message instanceof BallUpdateMessage) {
                BallUpdateMessage buMessage = (BallUpdateMessage) message;
                performUpdate(buMessage.ballUpdate);
                // System.out.print("Received message " + buMessage.position);
                System.out.println("    Received update: " + buMessage.ballUpdate);
                System.out.println();

            } else if (message instanceof AggregateBallUpdatesMessage) {
                AggregateBallUpdatesMessage abuMessage = (AggregateBallUpdatesMessage) message;
                System.out.println("Received aggregate update: " + abuMessage);
                System.out.println();
                for (BallUpdate ballUpdate : abuMessage.ballUpdates) {
                    performUpdate(ballUpdate);
                }

            } else if (message instanceof UserAddedMessage) {
                UserAddedMessage uaMessage = (UserAddedMessage) message;
                // System.out.println("Adding user " + uaMessage.userData.userName);
                setupUser(uaMessage.userData);

            } else if (message instanceof ConnectedUsersMessage) {
                ConnectedUsersMessage cuMessage = (ConnectedUsersMessage) message;
                ArrayList<UserData> userDataList = cuMessage.userDataList;
                for (UserData userData : userDataList) {
                    setupUser(userData);
                    // System.out.println("Connected user: " + userData.userName);
                }

            } else {

                System.err.println("Received odd message:" + message);
            }
            return message;
        }
    }

    //-----------------------------------------------------------
    //    Slut nätverksbekymmer, det nedanför kan skickas bort!!!
    //-----------------------------------------------------------
    @Override
    public void simpleUpdate(float tpf) {

        if (playerUser == null) {
            return;
        }

        currentDirection = new Vector3f(0, 0, 0);
        Ball playerBall = playerUser.getBall();

        if (!stateManager.hasState(bgas)) {

            Vector3f camDir = cam.getDirection().clone();
            Vector3f camLeft = cam.getLeft().clone();
            camDir.y = 0f;
            camLeft.y = 0f;

            if (left) {
                currentDirection.addLocal(camLeft);
            }
            if (right) {
                currentDirection.addLocal(camLeft.negate());
            }
            if (up) {
                currentDirection.addLocal(camDir);
            }
            if (down) {
                currentDirection.addLocal(camDir.negate());
            }
        }

        currentDirection.normalizeLocal();
        playerBall.setDirection(currentDirection);

        // Move all ghost objects
        for (User user : users.getValues()) {
            user.update();
        }

        // Send direction to server on a fixed interval
        renewUpdateTime();
        if (timeCounter > updateTime) {
            // Reset to long update time
            // System.out.println("Update time: " + updateTime);
            sendBallDirectionMessage();
            lastSentDirection = currentDirection;
            timeCounter = 0f;
            updateTime = longUpdateTime;
            System.out.println();
        }
        timeCounter += tpf;
    }

    private void renewUpdateTime() {
        
        double newUpdateTime;
        // If current direction is zero:
        if (currentDirection.equals(Vector3f.ZERO)) {
            // If zero was just pressed, time to start updating!
            if (lastSentDirection.equals(currentDirection)) {
                newUpdateTime = shortUpdateTime;
            } else {
                newUpdateTime = longUpdateTime;
            }
        } else {
            if (currentDirection.angleBetween(lastSentDirection) < smallAngle) {
                newUpdateTime = longUpdateTime;
            } else {
                newUpdateTime = shortUpdateTime;
            }
        }
        if (newUpdateTime < updateTime) {
            updateTime = newUpdateTime;
        }
    }
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String binding, boolean isPressed, float tpf) {

            if (isEnteringChat) {
                return;
            }

            if (binding.equals("CharLeft")) {
                left = isPressed;
            } else if (binding.equals("CharRight")) {
                right = isPressed;
            } else if (binding.equals("CharForward")) {
                up = isPressed;
            } else if (binding.equals("CharBackward")) {
                down = isPressed;
            }
        }
    };

    private void setupUser(UserData userData) {
        long callerId = userData.getId();
        User user = new User(assetManager, userData);
        users.put(callerId, user);

        viewLevel.attachChild(user.getGeometry());

        viewAppState.getPhysicsSpace().add(user.getBall());
        ghostAppState.getPhysicsSpace().add(user.getGhost());
        // Move the player to the correct position
        user.getBall().setPosition(userData.position);
        user.getGhost().setPosition(userData.position);
        // Make sure bling visible!
        rootNode.attachChild(user.getBlingNode());
    }

    private void initKeys() {

        inputManager.addRawInputListener(rawInputListener);

        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(actionListener, "CharLeft");
        inputManager.addListener(actionListener, "CharRight");
        inputManager.addListener(actionListener, "CharForward");
        inputManager.addListener(actionListener, "CharBackward");
    }

    private void initAppStates() {
        viewAppState = new BulletAppState();
        viewAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(viewAppState);

        ghostAppState = new BulletAppState();
        ghostAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(ghostAppState);
    }

    public void initShadow() {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector3f();
        }
        bsr = new BasicShadowRenderer(assetManager, 512);
        bsr.setDirection(new Vector3f(-0.5f, -.5f, -.5f).normalizeLocal());
        viewPort.addProcessor(bsr);

        Camera shadowCam = bsr.getShadowCamera(); //Behövs denna?
        ShadowUtil.updateFrustumPoints2(shadowCam, points);
    }

    public void chasePlayer() {
        setCameraTarget(playerUser.getGeometry());
    }

    private void setCameraTarget(Geometry target) {
        flyCam.setEnabled(false);
        chaseCamera = new ChaseCamera(cam, target, inputManager);
        chaseCamera.setDragToRotate(false);
        chaseCamera.setMaxVerticalRotation((float) Math.PI / 3);
        chaseCamera.setDefaultDistance(30f);
        chaseCamera.setLookAtOffset(new Vector3f(0, 5, 0));
    }

    private void initLevel() {
        viewLevel = new TestLevel(assetManager, viewAppState);
        ghostLevel = new TestLevel(assetManager, ghostAppState);

        viewLevel.initGraphics(assetManager);
        rootNode.attachChild(viewLevel);
        rootNode.attachChild(ghostLevel);
    }

    public User getPlayer() {
        return playerUser;
    }

    public ChaseCamera getChaseCamera() {
        return chaseCamera;
    }

    private class GomokuMessageListener implements MessageListener<Client> {

        public void messageReceived(Client source, Message message) {
            BallClient.this.enqueue(new GomokuMessageReceiver(message));
        }
    }

    private void updateScore(long winnerId, long loserId, int scoreChange) {
        users.getValue(winnerId).updateScore(scoreChange);
        users.getValue(loserId).updateScore(-scoreChange);
    }
    
    private class GomokuMessageReceiver implements Callable {

        Message message;

        public GomokuMessageReceiver(Message message) {
            this.message = message;
        }

        public Object call() {
            
            if (message instanceof GomokuEndMessage) {
                
                GomokuEndMessage gem = (GomokuEndMessage) message;
                
                if (gem.gameID == currentGameId) {
                    stateManager.detach(bgas);
                } else {   
                    GomokuBoard3D dummyBoard = currentGames.get(gem.gameID);
                    rootNode.detachChild(dummyBoard);
                }
                updateScore(gem.winnerID, gem.loserID, gem.scoreChange);
                
                User u1 = users.getValue(gem.winnerID);
                User u2 = users.getValue(gem.loserID);
                
                u1.setFrozen(false);
                u2.setFrozen(false);
                

            } else if (message instanceof GomokuStartMessage) {
                
                GomokuStartMessage gsm = (GomokuStartMessage) message;
                
                if (gsm.firstPlayerID == playerUser.getId() ||
                        gsm.secondPlayerID == playerUser.getId()) {
                    
                    stateManager.attach(bgas);
                    GomokuGame newGame = bgas.startNewGame(gsm);
                    currentGameId = newGame.getID();
                    
                } else if (SHOW_ALL_GOMOKU_GAMES) {
                    
                    GomokuGame game = new GomokuGame(gsm);
                    GomokuBoard3D dummyBoard = new GomokuBoard3D(assetManager, game);
                    dummyBoard.positionBetween(gsm.firstPlayerPos, gsm.secondPlayerPos);
                    rootNode.attachChild(dummyBoard);
                    
                    currentGames.put(gsm.gameID, dummyBoard);
                    
                }
                
                User u1 = users.getValue(gsm.firstPlayerID);
                User u2 = users.getValue(gsm.secondPlayerID);
                u1.setFrozen(gsm.firstPlayerPos);
                u2.setFrozen(gsm.secondPlayerPos);
                
                
            } else if (message instanceof GomokuUpdateMessage) {
                 GomokuUpdateMessage gum = (GomokuUpdateMessage) message;
                
                if (gum.gameID == currentGameId) {
                    // This is handled elsewhere for our own game
                } else {
                    GomokuBoard3D board = currentGames.get(gum.gameID);
                    if (board != null) {
                        board.setColor(gum.p, gum.color);
                    }
                }
                 
            } else {

                System.err.println("Received odd message:" + message);
            }
            
            return message;
        }
    }
    private BitmapText chatTextField;
    private String chatString = "";
    private boolean isEnteringChat = false;
    private RawInputListener rawInputListener = new RawInputListener() {

        public void beginInput() {
        }

        public void endInput() {
        }

        public void onJoyAxisEvent(JoyAxisEvent evt) {
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) {
        }

        public void onMouseMotionEvent(MouseMotionEvent evt) {
        }

        public void onMouseButtonEvent(MouseButtonEvent evt) {
        }

        public void onKeyEvent(KeyInputEvent evt) {

            if (!evt.isPressed()) {
                return;

            } else if (!isEnteringChat && evt.getKeyCode() == KeyInput.KEY_RETURN) {
                isEnteringChat = true;
                chatTextField.setText(((isEnteringChat) ? "Enter message: _" : ""));
                evt.setConsumed();

            } else if (isEnteringChat) {

                switch (evt.getKeyCode()) {
                    case KeyInput.KEY_ESCAPE:
                        isEnteringChat = false;
                        chatTextField.setText("");
                        evt.setConsumed();
                        return;

                    case KeyInput.KEY_RETURN:
                        if (chatString.equals("")) {
                            isEnteringChat = false;
                            chatTextField.setText("");
                            evt.setConsumed();
                            return;
                        } else {
                            client.send(new ChatMessage(chatString, playerUserData.id));
                            chatString = "";
                        }
                        break;

                    case KeyInput.KEY_BACK:
                        if (chatString.length() > 0) {
                            chatString = chatString.substring(0, chatString.length() - 1);
                        }
                        break;

                    default:
                        char c = evt.getKeyChar();
                        chatString += c;
                        break;
                }
                chatTextField.setText("Enter message: " + chatString + "_");
                evt.setConsumed();
            }

        }

        public void onTouchEvent(TouchEvent evt) {
        }
    };

    private void setupChat() {

        guiFont = assetManager.loadFont("Interface/Fonts/HelveticaNeue.fnt");
        chatTextField = new BitmapText(guiFont, false);

        chatTextField.setSize(guiFont.getCharSet().getRenderedSize());
        chatTextField.setText("");
        chatTextField.setColor(ColorRGBA.White);
        chatTextField.setLocalTranslation(new Vector3f(16, 40, 0f));

        guiNode.attachChild(chatTextField);
    }
}

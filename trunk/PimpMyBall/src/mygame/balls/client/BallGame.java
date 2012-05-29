package mygame.balls.client;

import com.jme3.app.Application;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.balls.messages.BallUpdateMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.shadow.PssmShadowRenderer;
import de.lessvoid.nifty.screen.ScreenController;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import mygame.Fireworks;
import mygame.Settings;
import mygame.admin.CentralServer;
import mygame.admin.ChatMessage;
import mygame.admin.Config;
import mygame.admin.messages.LoginMessage;
import mygame.admin.messages.LoginSuccessMessage;
import mygame.admin.NetworkHelper;
import mygame.admin.SerializerHelper;
import mygame.admin.ServerInfo;
import mygame.admin.messages.LogoutMessage;
import mygame.admin.messages.UserLeftServerMessage;
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
import mygame.boardgames.GomokuBoard3D;
import mygame.boardgames.network.GomokuEndMessage;
import mygame.boardgames.network.GomokuStartMessage;
import mygame.boardgames.network.GomokuUpdateMessage;
import mygame.util.BiMap;


public class BallGame extends AbstractAppState implements ChatDelegate, ScreenController {

    private Client client;
    
    private BulletAppState viewAppState, ghostAppState;
    private BoardGameAppState bgas;
    private ChatControl chatControl;
    
    private BiMap<Long, User> users = new BiMap<Long, User>();
    private User playerUser;
   
    private Vector3f currentDirection = Vector3f.ZERO;
    private Vector3f currentVelocity = Vector3f.ZERO;
    private Vector3f lastSentVelocity = Vector3f.ZERO;
    
    private boolean left = false, right = false, up = false, down = false;
    private TestLevel viewLevel, ghostLevel;
    private ChaseCamera chaseCamera = null;
    private Fireworks fireworks;
    
    // Timer variables
    private float timeCounter = 0f;
    private final double smallAngle = Math.toRadians(5d); // 5 degrees in radians
    private final double shortUpdateTime = 0.1d;
    private final double longUpdateTime = 5 * shortUpdateTime;
    private double updateTime = longUpdateTime;

    private UserData playerUserData;
    private static Client centralServerClient;
    // private static CentralServerListener centralServerListener;
    private static final String LOCK = "lock..";
    
    private int secret;

    private PssmShadowRenderer pssmRenderer;
    private boolean usesShadows = true;
    
    /*
    public static void main(String[] args) throws IOException, InterruptedException {
        
        SerializerHelper.initializeClasses();
        String userName = getString(null, "Login Info", "Enter username:", "nicke");
        String passWord = getString(null, "Login Info", "Enter Password:", "kass");

        centralServerListener = new CentralServerListener();
        centralServerClient = NetworkHelper.connectToServer(Config.getCentralServerInfo());
        centralServerClient.addMessageListener(centralServerListener);
        centralServerClient.start();
        centralServerClient.send(new LoginMessage(userName, passWord));
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
     */
    
    public static String getString(Component owner, String title, String message, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                owner, message, title, JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
    }

    public BallGame(ServerInfo serverInfo, UserData userData, int secret) throws Exception {
        client = NetworkHelper.connectToServer(serverInfo);
        client.start();
        this.playerUserData = userData;
        this.secret = secret;
    }

    private SimpleApplication app;
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        this.app = (SimpleApplication) app;
        
        client.addMessageListener(new BallServerListener(),
                BallUpdateMessage.class,
                AggregateBallUpdatesMessage.class,
                UserAddedMessage.class,
                ConnectedUsersMessage.class,
                UserLeftServerMessage.class);

        client.send(new HelloMessage(secret, playerUserData.id));

        initAppStates();
        initKeys();
        initShadow();
        initLevel();
        
        setupUser(playerUserData);

        fireworks = new Fireworks(app.getAssetManager());
        this.app.getRootNode().attachChild(fireworks);
        
        playerUser = users.getValue(playerUserData.id);
        playerUser.makeBlue(app.getAssetManager());
        setCameraTarget(playerUser.getGeometry());
    }

    @Override
    public void update(float tpf) {

        if (playerUser == null) {
            return;
        }

        currentDirection = new Vector3f(0, 0, 0);
        Ball playerBall = playerUser.getBall();
        
        if (playerBall.getMass() > 0) {

            Vector3f camDir = app.getCamera().getDirection().clone();
            Vector3f camLeft = app.getCamera().getLeft().clone();
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
        
        currentVelocity = playerBall.getVelocity();

        // Send direction to server on a fixed interval
        renewUpdateTime();
        if (timeCounter > updateTime) {
            // Reset to long update time
            sendBallDirectionMessage();
            lastSentVelocity = currentVelocity;
            timeCounter = 0f;
            updateTime = longUpdateTime;
        }
        timeCounter += tpf;
    }

    private void renewUpdateTime() {

        double newUpdateTime;
        // If current direction is zero:
        if (currentVelocity.equals(Vector3f.ZERO)) {
            // If zero was just pressed, time to start updating!
            if (lastSentVelocity.equals(currentVelocity)) {
                newUpdateTime = shortUpdateTime;
            } else {
                newUpdateTime = longUpdateTime;
            }
        } else {
            if (currentVelocity.angleBetween(lastSentVelocity) < smallAngle) {
                newUpdateTime = longUpdateTime;
            } else {
                newUpdateTime = shortUpdateTime;
            }
        }
        if (newUpdateTime < updateTime) {
            updateTime = newUpdateTime;
        }
    }
    
    
    @Override
    public void stateDetached(AppStateManager manager) {
        // client.close();
        // super.stateDetached(manager);
        // super.destroy();
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
        if (user != null) {
            Ball ghost = user.getGhost();
            ghost.setPosition(ballUpdate.position);
            ghost.setVelocity(ballUpdate.velocity);
            ghost.setDirection(ballUpdate.direction);
        }
    }

    public void bind(Nifty nifty, Screen screen) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onStartScreen() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onEndScreen() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    private class BallServerListener implements MessageListener<Client> {
        public void messageReceived(Client source, Message message) {
            app.enqueue(new MessageReceiver(message));
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
                
            } else if (message instanceof AggregateBallUpdatesMessage) {
                AggregateBallUpdatesMessage abuMessage = (AggregateBallUpdatesMessage) message;
                for (BallUpdate ballUpdate : abuMessage.ballUpdates) {
                    performUpdate(ballUpdate);
                }

            } else if (message instanceof UserAddedMessage) {
                UserAddedMessage uaMessage = (UserAddedMessage) message;
                setupUser(uaMessage.userData);

            } else if (message instanceof ConnectedUsersMessage) {
                ConnectedUsersMessage cuMessage = (ConnectedUsersMessage) message;
                ArrayList<UserData> userDataList = cuMessage.userDataList;
                for (UserData userData : userDataList) {
                    setupUser(userData);
                }
            } else if (message instanceof UserLeftServerMessage) {
                UserLeftServerMessage ulsMessage = (UserLeftServerMessage) message;
                long userId = ulsMessage.userId;
                removeUser(users.getValue(userId));

            } else if (message instanceof LogoutMessage) {
                LogoutMessage lMessage = (LogoutMessage) message;
                long userId = lMessage.userId;
                removeUser(users.getValue(userId));

            } else {
                System.err.println("Received odd message:" + message);
            }
            return message;
        }
    }

    private void setupUser(UserData userData) {
        long callerId = userData.getId();
        User user = new User(app.getAssetManager(), userData);
        users.put(callerId, user);
        user.getGeometry().setShadowMode(ShadowMode.CastAndReceive);

        viewLevel.attachChild(user.getGeometry());

        viewAppState.getPhysicsSpace().add(user.getBall());
        ghostAppState.getPhysicsSpace().add(user.getGhost());
        // Move the player to the correct position
        user.getBall().setPosition(userData.position);
        user.getGhost().setPosition(userData.position);
        // Make sure bling visible!
        app.getRootNode().attachChild(user.getBlingNode());
    }
    
    public User getUser(long id) {
        return users.getValue(id);
    }

    public void removeUser(User lostUser) {
        
        fireworks.explosionAtPosition(lostUser.getBall().getPosition());
        
        users.removeValue(lostUser);
        app.getRootNode().detachChild(lostUser.getBlingNode());
        viewLevel.detachChild(lostUser.getGeometry());
        ghostLevel.detachChild(lostUser.getGeometry());
        viewAppState.getPhysicsSpace().remove(lostUser.getBall());
        ghostAppState.getPhysicsSpace().remove(lostUser.getBall());
    }
    
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String binding, boolean isPressed, float tpf) {

            if (binding.equals("CharLeft")) {
                left = isPressed;
            } else if (binding.equals("CharRight")) {
                right = isPressed;
            } else if (binding.equals("CharForward")) {
                up = isPressed;
            } else if (binding.equals("CharBackward")) {
                down = isPressed;
                
            } else if (!isPressed && binding.equals("Shadow")) {
                if (usesShadows) {
                    app.getViewPort().removeProcessor(pssmRenderer);
                } else {
                    app.getViewPort().addProcessor(pssmRenderer);
                }
                usesShadows = !usesShadows;
            }
        }
    };
    
    private void initKeys() {

        InputManager inputManager = app.getInputManager();
        
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));
        
        inputManager.addListener(actionListener, "CharLeft");
        inputManager.addListener(actionListener, "CharRight");
        inputManager.addListener(actionListener, "CharForward");
        inputManager.addListener(actionListener, "CharBackward");
        
        inputManager.addMapping("Shadow", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addListener(actionListener, "Shadow");
    }

    private void initAppStates() {
        
        AppStateManager stateManager = app.getStateManager();
        
        viewAppState = new BulletAppState();
        stateManager.attach(viewAppState);

        ghostAppState = new BulletAppState();
        stateManager.attach(ghostAppState);
        
        // bgas = new BoardGameAppState(this, client);
        // stateManager.attach(bgas);
        
        chatControl = new ChatControl(this, client);
        stateManager.attach(chatControl);
    }

    public void initShadow() {
        pssmRenderer = new PssmShadowRenderer(app.getAssetManager(), 1024, 2);
        pssmRenderer.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal()); // light direction
        app.getViewPort().addProcessor(pssmRenderer);
        pssmRenderer.setShadowIntensity(0.3f);
    }

    private void setCameraTarget(Geometry target) {
        
        app.getFlyByCamera().setEnabled(false);

        chaseCamera = new ChaseCamera(app.getCamera(), target, app.getInputManager());
        chaseCamera.setDragToRotate(false);
        chaseCamera.setMaxVerticalRotation((float) Math.PI / 3);
        chaseCamera.setDefaultDistance(30f);
        chaseCamera.setLookAtOffset(new Vector3f(0, 5, 0));
        chaseCamera.setUpVector(Vector3f.UNIT_Y.clone());
    }

    private void initLevel() {
        viewLevel = new TestLevel(app.getAssetManager(), viewAppState);
        ghostLevel = new TestLevel(app.getAssetManager(), ghostAppState);

        viewLevel.initGraphics(app.getAssetManager());
        app.getRootNode().attachChild(viewLevel);
        app.getRootNode().attachChild(ghostLevel);
    }

    public User getPlayer() {
        return playerUser;
    }

    public ChaseCamera getChaseCamera() {
        return chaseCamera;
    }
    public void chasePlayer() {
        setCameraTarget(playerUser.getGeometry());
    }
    
    public void showFireworks(Vector3f location) {
        if (Settings.FANCY_EFFECTS)
            fireworks.emitAtPosition(location);
    }
    
    /*
     * ChatDelegate methods
     */
    @Override
    public void onIncomingMessage(String msg, long sender) {
        users.getValue(sender).displayMessage(msg);
    }
    @Override
    public long getChatterId() {
        return playerUserData.id;
    }
}

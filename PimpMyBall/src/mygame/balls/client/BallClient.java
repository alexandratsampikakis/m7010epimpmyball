package mygame.balls.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.balls.messages.BallUpdateMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import com.jme3.system.NanoTimer;
import com.jme3.system.Timer;
import java.awt.Component;
import java.util.ArrayList;
//import java.util.Timer.jme.util.Timer;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import mygame.admin.CentralServer;
import mygame.admin.LoginMessage;
import mygame.admin.LoginSuccessMessage;
import mygame.admin.NetworkHelper;
import mygame.admin.SerializerHelper;
import mygame.admin.ServerInfo;
import mygame.balls.Ball;
import mygame.balls.TestLevel;
import mygame.balls.UserData;
import mygame.balls.messages.BallDirectionMessage;
import mygame.balls.messages.ConnectedUsersMessage;
import mygame.balls.messages.HelloMessage;
import mygame.balls.messages.UserAddedMessage;
import mygame.boardgames.BoardGameAppState;
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
    private boolean left = false,
            right = false,
            up = false,
            down = false;
    private mygame.balls.Level level;
    private ChaseCamera chaseCamera;
    NanoTimer timer;
    //-----------------------------------
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
    //-----------------------------------
    public BallClient(ServerInfo serverInfo, UserData userData, int secret) throws Exception {

        client = NetworkHelper.connectToServer(serverInfo);
        client.start();

        bgas = new BoardGameAppState(this, client);

        this.playerUserData = userData;
        this.secret = secret;
        timer = new NanoTimer();
    }

    @Override
    public void simpleInitApp() {

        client.addMessageListener(new BallServerListener(),
                BallUpdateMessage.class,
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
        setupUser(playerUserData);
        playerUser = users.getValue(playerUserData.id);
        playerUser.makeBlue(assetManager);
        setCameraTarget(playerUser.getGeometry());
    }

    private void sendBallDirectionMessage() {
        long playerId = playerUser.getId();
        Ball playerBall = playerUser.getBall();
        Vector3f playerDirection = playerBall.getDirection();
        BallDirectionMessage bdMessage = new BallDirectionMessage(playerId, playerDirection);
        client.send(bdMessage);
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
                User user = users.getValue(buMessage.id);
                Ball ghost = user.getGhost();
                System.out.println("Received position " + buMessage.position);

                // Update the ghost
                ghost.setPosition(buMessage.position);
                ghost.setVelocity(buMessage.velocity);
                ghost.setDirection(buMessage.direction);

            } else if (message instanceof UserAddedMessage) {
                UserAddedMessage uaMessage = (UserAddedMessage) message;
                System.out.println("Add user " + uaMessage.userData.userName);
                setupUser(uaMessage.userData);

            } else if (message instanceof ConnectedUsersMessage) {
                ConnectedUsersMessage cuMessage = (ConnectedUsersMessage) message;
                ArrayList<UserData> userDataList = cuMessage.userDataList;
                for (UserData userData : userDataList) {
                    setupUser(userData);
                    System.out.println("Connected user: " + userData.userName);
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

        Vector3f direction = new Vector3f(0, 0, 0);
        Ball playerBall = playerUser.getBall();

        if (!stateManager.hasState(bgas)) {

            Vector3f camDir = cam.getDirection().clone();
            Vector3f camLeft = cam.getLeft().clone();
            camDir.y = 0f;
            camLeft.y = 0f;

            if (left) {
                direction.addLocal(camLeft);
            }
            if (right) {
                direction.addLocal(camLeft.negate());
            }
            if (up) {
                direction.addLocal(camDir);
            }
            if (down) {
                direction.addLocal(camDir.negate());
            }
        }
        playerBall.setDirection(direction);
        // Move all ghost objects
        for (User user : users.getValues()) {
            user.Update();
        }

        // Send direction to server on a fixed interval
        if (timeCounter > 0.1f) {
            System.out.println("At timely time " + timer.getTimeInSeconds());
            sendBallDirectionMessage();
            timeCounter = 0f;

        }
        timeCounter += tpf;
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
            }
        }
    };

    private void setupUser(UserData userData) {
        long callerId = userData.getId();
        User user = new User(assetManager, userData);
        users.put(callerId, user);
        level.attachChild(user.getGeometry());
        viewAppState.getPhysicsSpace().add(user.getBall());
        ghostAppState.getPhysicsSpace().add(user.getGhost());
        // Move the player to the correct position
        user.getBall().setPosition(userData.position);
        user.getGhost().setPosition(userData.position);
        // Make sure bling visible!
        rootNode.attachChild(user.getBlingNode());
    }

    private void initKeys() {
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
        // chaseCamera.setDefaultDistance(5f);
    }

    private void initLevel() {
        level = new TestLevel(assetManager);
        level.initLighting();
        rootNode.attachChild(level);
        viewAppState.getPhysicsSpace().add(level.getTerrain());
        ghostAppState.getPhysicsSpace().add(level.getTerrain().clone());//?!?!?!?!!

        //((TestLevel) level).initTrees(assetManager, viewAppState);
        //((TestLevel) level).initTrees(assetManager, ghostAppState);
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

    private class GomokuMessageReceiver implements Callable {

        Message message;

        public GomokuMessageReceiver(Message message) {
            this.message = message;
        }

        public Object call() {
            if (message instanceof GomokuEndMessage) {
                stateManager.detach(bgas);

            } else if (message instanceof GomokuStartMessage) {
                stateManager.attach(bgas);
                bgas.startNewGame((GomokuStartMessage) message);

            } else if (message instanceof GomokuUpdateMessage) {
            } else {

                System.err.println("Received odd message:" + message);
            }
            return message;
        }
    }
}

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
import com.jme3.network.Network;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import java.awt.Component;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import javax.swing.JOptionPane;
import mygame.admin.CentralServer;
import mygame.admin.LoginMessage;
import mygame.admin.LoginSuccessMessage;
import mygame.admin.SerializerHelper;
import mygame.admin.ServerInfo;
import mygame.balls.Ball;
import mygame.balls.TestLevel;
import mygame.balls.UserData;
import mygame.balls.messages.BallDirectionMessage;
import mygame.balls.messages.ConnectedUsersMessage;
import mygame.balls.messages.HelloMessage;
import mygame.balls.messages.UserAddedMessage;
import mygame.util.BiMap;

public class BallClient extends SimpleApplication {

    private int timeCounter = 0;
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
    //-----------------------------------
    //-----------------------------------
    private UserData playerUserData;
    static Client centralServerClient;
    static CentralServerListener centralServerListener;
    private int secret;

    public static void main(String[] args) throws IOException, InterruptedException {
        SerializerHelper.initializeClasses();
        String userName = getString(null, "Login Info", "Enter username:", "nicke");
        String passWord = getString(null, "Login Info", "Enter Password:", "kass");

        ServerInfo centralServerInfo = CentralServer.info;
        centralServerClient = Network.connectToServer(centralServerInfo.NAME, centralServerInfo.VERSION,
                centralServerInfo.ADDRESS, centralServerInfo.PORT, centralServerInfo.UDP_PORT);


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

        client = Network.connectToServer(serverInfo.NAME, serverInfo.VERSION,
                serverInfo.ADDRESS, serverInfo.PORT, serverInfo.UDP_PORT);
        client.start();

        this.playerUserData = userData;
        this.secret = secret;
    }

    @Override
    public void simpleInitApp() {
        client.addMessageListener(new BallServerListener(), BallUpdateMessage.class,
                UserAddedMessage.class, ConnectedUsersMessage.class);
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

        Ball playerBall = playerUser.getBall();
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
        camDir.y = 0f;
        camLeft.y = 0f;
        playerBall.setDirection(Vector3f.ZERO);


        if (left) {
            playerBall.setDirection(camLeft);
        }
        if (right) {
            playerBall.setDirection(camLeft.negate());
        }
        if (up) {
            playerBall.setDirection(camDir);
        }
        if (down) {
            playerBall.setDirection(camDir.negate());
        }

        // Move all ghost objects
        for (User user : users.getValues()) {
            Ball ball = user.getBall();
            Ball ghost = user.getGhost();

            ball.moveForward();
            ghost.moveForward();
            ball.adjustToBall(ghost);
            System.out.println("Client's ghost position: " + ghost.getPosition());
        }

        // Send direction to server on a fixed interval
        if (timeCounter > 5) {
            sendBallDirectionMessage();
            timeCounter = 0;
        }
        timeCounter++;
    }
    private ActionListener actionListener = new ActionListener() {

        public void onAction(String binding, boolean isPressed, float tpf) {



            if (binding.equals("CharLeft")) {
                if (isPressed) {
                    left = true;
                } else {
                    left = false;
                }
            } else if (binding.equals("CharRight")) {
                if (isPressed) {
                    right = true;
                } else {
                    right = false;
                }
            } else if (binding.equals("CharForward")) {
                if (isPressed) {
                    up = true;
                } else {
                    up = false;
                }
            } else if (binding.equals("CharBackward")) {
                if (isPressed) {
                    down = true;
                } else {
                    down = false;
                }
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
        user.getBall().setPosition(userData.position);
        user.getGhost().setPosition(userData.position);
        System.out.println("Setting up user with id " + user.getId() + ".");
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

    private void setCameraTarget(Geometry target) {
        flyCam.setEnabled(false);
        ChaseCamera camera = new ChaseCamera(cam, target, inputManager);
        camera.setDragToRotate(false);
    }

    private void initLevel() {
        level = new TestLevel(assetManager);
        level.initLighting();
        rootNode.attachChild(level);
        viewAppState.getPhysicsSpace().add(level.getTerrain());
        ghostAppState.getPhysicsSpace().add(level.getTerrain().clone());//?!?!?!?!!
    }
}

package mygame.balls.client;

import mygame.balls.messages.BallUpdateMessage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import java.util.concurrent.Callable;
import mygame.balls.Ball;
import mygame.balls.server.BallServer;
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

    public static void main(String[] args) {
        BallServer.initializeClasses();
        BallClient app = new BallClient();
        app.start();
        app.setPauseOnLostFocus(false);
    }

    @Override
    public void simpleInitApp() {
        try {
            client = Network.connectToServer(BallServer.NAME, BallServer.VERSION,
                    //"192.168.1.6", // Nickes ip
                    "localhost", BallServer.PORT, BallServer.UDP_PORT);
        } catch (IOException ex) {
            Logger.getLogger(BallClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        client.addMessageListener(new ClientMessageListener(), BallUpdateMessage.class,
                UserAddedMessage.class, ConnectedUsersMessage.class);
        client.start();
        initAppStates();
        initKeys();
        initShadow();
        initLevel();
    }

    private void sendBallDirectionMessage() {
        long playerId = playerUser.getId();
        Ball playerBall = playerUser.getBall();
        Vector3f playerDirection = playerBall.getDirection();
        BallDirectionMessage bdMessage = new BallDirectionMessage(playerId, playerDirection);
        client.send(bdMessage);
    }

    private void sendHelloMessage(long id, long authCode) {
        HelloMessage helloMessage = new HelloMessage(id, authCode);
        client.send(helloMessage);
    }

    private class ClientMessageListener implements MessageListener<Client> {

        public void messageReceived(Client source, Message message) {
            BallClient.this.enqueue(new MyCallable(message));
        }

        private class MyCallable implements Callable {

            Message message;

            public MyCallable(Message message) {
                this.message = message;
            }

            public Object call() {
                if (message instanceof BallUpdateMessage) {

                    BallUpdateMessage ballMessage = (BallUpdateMessage) message;
                    long callerId = ballMessage.getId();
                    User user = users.getValue(callerId);
                    Ball ghost = user.getGhost();

                    if (ghost != null) {

                        // Update the ghost
                        ghost.setPosition(ballMessage.getPosition());
                        ghost.setVelocity(ballMessage.getVelocity());
                        ghost.setDirection(ballMessage.getDirection());
                    }

                } else if (message instanceof UserAddedMessage) {

                    UserAddedMessage uaMessage = (UserAddedMessage) message;
                    UserData userData = uaMessage.getUserData();
                    setupUser(userData);

                } else if () {
                sendHelloMessage();
                
                } 
                
                
                else {

                    System.err.println("Received odd message:" + message);
                }
                return message;
            }
        }
    }

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

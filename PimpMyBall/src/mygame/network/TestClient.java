package mygame.network;

import mygame.network.messages.BallMessage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.Camera;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import java.util.concurrent.Callable;
import mygame.network.messages.UserAddedMessage;

public class TestClient extends SimpleApplication {

    Material matRock;
    Material matWire;
    private int timeCounter = 0;
    private Client client;
    private BasicShadowRenderer bsr;
    private Vector3f[] points;
    private long id = 0;
    private UserList userList = new UserList();
    protected BulletAppState viewAppState, ghostAppState;

    {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector3f();
        }
    }
    private ClientSideUser player;
    private boolean left = false,
            right = false,
            up = false,
            down = false;
    private mygame.network.Level level;

    public static void main(String[] args) {
        BallServer.initializeClasses();
        TestClient app = new TestClient();
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
            Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        client.addMessageListener(new ClientMessageListener(), BallMessage.class, UserAddedMessage.class);
        client.start();
        initAppStates();
        initKeys();
        initShadow();
        initLevel();
        //Create the player:
        addNewUser(id);
        /*/OSPARVÄRT//////////*/ player = (ClientSideUser) userList.getUserWithId(id);
        /*/OSPARVÄRT//////////*/ player.setPosition(new Vector3f(0f, 100f, 0f));
        initCamera();
    }

    private void sendBallMessage() {
        BallMessage ballMessage = new BallMessage(id, Vector3f.ZERO, Vector3f.ZERO, player.getDirection());
        ballMessage.setReliable(false);
        client.send(ballMessage);
        System.out.println("Sending direction: " + ballMessage.getDirection());
        System.out.println("Position is: " + player.getPosition());
        System.out.println("Ghost position is: " + player.getGhostControl().getPhysicsLocation());


    }

    private class ClientMessageListener implements MessageListener<Client> {

        public void messageReceived(Client source, Message message) {
            TestClient.this.enqueue(new MyCallable(message));
        }

        private class MyCallable implements Callable {

            Message message;

            public MyCallable(Message message) {
                this.message = message;
            }

            public Object call() {
                if (message instanceof BallMessage) {
                    BallMessage ballMessage = (BallMessage) message;
                    long callerId = ballMessage.getId();
                    ClientSideUser user = (ClientSideUser) userList.getUserWithId(callerId);
                    Vector3f position = ballMessage.getPosition();
                    Vector3f velocity = ballMessage.getVelocity();
                    Vector3f direction = ballMessage.getDirection();
                    user.setGhostData(position, velocity, direction);

                } else if (message instanceof UserAddedMessage) {
                    UserAddedMessage userAddedMessage = (UserAddedMessage) message;
                    addNewUser(userAddedMessage.getId());
                }
                return message;
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone();
        Vector3f camLeft = cam.getLeft().clone();
        camDir.y = 0f;
        camLeft.y = 0f;
        player.setDirection(Vector3f.ZERO);

        if (left) {
            player.setDirection(camLeft);
        }
        if (right) {
            player.setDirection(camLeft.negate());
        }
        if (up) {
            player.setDirection(camDir);
        }
        if (down) {
            player.setDirection(camDir.negate());
        }

        // Move all users
        for (User user : userList) {
            ClientSideUser clientSideUser = (ClientSideUser) user;
            clientSideUser.moveForward();
            clientSideUser.moveGhost();
        }

        //For the shadow
        Camera shadowCam = bsr.getShadowCamera(); //Behövs denna?
        ShadowUtil.updateFrustumPoints2(shadowCam, points);

        // Send direction to server on a fixed interval
        if (timeCounter > 5) {
            sendBallMessage();
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

    private void addNewUser(long id) {
        ClientSideUser newUser = new ClientSideUser(assetManager, id);
        userList.add(newUser);
        ghostAppState.getPhysicsSpace().add(newUser.getGhostControl());
        viewAppState.getPhysicsSpace().add(newUser.getControl());
        level.attachChild(newUser.getGeometry());
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
        bsr = new BasicShadowRenderer(assetManager, 512);
        bsr.setDirection(new Vector3f(-0.5f, -.5f, -.5f).normalizeLocal());
        viewPort.addProcessor(bsr);
    }

    private void initCamera() {
        flyCam.setEnabled(false);
        ChaseCamera camera = new ChaseCamera(cam, player.getGeometry(), inputManager);
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

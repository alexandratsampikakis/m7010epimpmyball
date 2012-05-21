package mygame.balls.server;

import mygame.balls.messages.BallUpdateMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.AbstractMessage;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import mygame.balls.Ball;
import mygame.balls.Level;
import mygame.balls.TestLevel;
import mygame.balls.UserData;
import mygame.balls.messages.BallDirectionMessage;
import mygame.balls.messages.ConnectedUsersMessage;
import mygame.balls.messages.HelloMessage;
import mygame.balls.messages.RequestUsersMessage;
import mygame.balls.messages.UserAddedMessage;
import mygame.util.BiMap;

public class BallServer extends SimpleApplication {

    public static void initializeClasses() {
        // Doing it here means that the client code only needs to
        // call our initialize. 
        Serializer.registerClass(BallUpdateMessage.class);
        Serializer.registerClass(BallDirectionMessage.class);
        Serializer.registerClass(UserAddedMessage.class);
        Serializer.registerClass(HelloMessage.class);
        Serializer.registerClass(ConnectedUsersMessage.class);
        Serializer.registerClass(RequestUsersMessage.class);
    }
    private Level level;
    private BulletAppState bulletAppState;
    private static Server server;
    public static final String NAME = "Pimp My Ball Server";
    public static final int VERSION = 1;
    public static final int PORT = 5110;
    public static final int UDP_PORT = 5110;
    private static int timeCounter = 0;
    private BiMap<Long, User> users = new BiMap<Long, User>();
    private BiMap<Long, UserData> pendingUserData = new BiMap<Long, UserData>();
    AreaOfInterestManager aoiManager;

    public static void main(String[] args) throws Exception {
        initializeClasses();
        BallServer app = new BallServer();
        app.start();
        app.setPauseOnLostFocus(false);

        // Use this to test the client/server name version check
        server = Network.createServer(NAME, VERSION, PORT, UDP_PORT);
        server.start();

        synchronized (NAME) {
            NAME.wait();
        }
    }

    @Override
    public void simpleInitApp() {
        initAppState();
        initLevel();
        aoiManager = new AreaOfInterestManager(users);
        MessageListener messageListener = new MessageListener<HostedConnection>() {

            public void messageReceived(HostedConnection conn, Message message) {
                if (message instanceof BallDirectionMessage) {
                    BallServer.this.enqueue(new MessageReceived((BallDirectionMessage) message, conn));
                }
            }
        };
        server.addMessageListener(messageListener);

        ConnectionListener connectionListener = new ConnectionListener() {

            public void connectionAdded(Server server, HostedConnection conn) {
                //Nåt alls?
            }

            public void connectionRemoved(Server server, HostedConnection conn) {
                BallServer.this.enqueue(new ConnectionLost(conn));
            }
        };
        server.addConnectionListener(connectionListener);
        flyCam.setMoveSpeed(30f);//KAAAST!!!!!
    }

    @Override
    public void update() {
        super.update();
        if (timeCounter > 5) {
            timeCounter = 0;
            broadcastBallData();
        }
        timeCounter++;
        for (User user : users.getValues()) {
            user.getBall().moveForward();
        }
    }

    private void broadcastBallData() {

        for (User user : users.getValues()) {
            Ball ball = user.getBall();
            BallUpdateMessage ballMessage = new BallUpdateMessage(ball);

            HashSet filter = aoiManager.getAreaOfInterest(user);
            server.broadcast(Filters.in(filter), ballMessage);
        }
    }

    /**
     * Send information about a new user to all other users
     * @param newId The ID of the new user
     */
    private void broadUserAddedMessage(long newId) {
        User newUser = users.getValue(newId);
        HostedConnection newConnection = newUser.getConnection();
        UserData newUserData = newUser.getUserData();

        // Inform all other users that the new user should be added.
        UserAddedMessage userAddedMessage = new UserAddedMessage(newUserData);
        server.broadcast(Filters.notEqualTo(newConnection), userAddedMessage);
    }

    /**
     * Transmit data on all users to the sender
     * @param connection 
     */
    private void sendConnectedUsersMessage(HostedConnection connection) {
        ArrayList<UserData> userData = new ArrayList<UserData>();

        for (User user : users.getValues()) {
            userData.add(user.getUserData());
        }
        ConnectedUsersMessage cuMessage = new ConnectedUsersMessage(userData);
        server.broadcast(Filters.notEqualTo(connection), cuMessage);
    }

    private class MessageReceived implements Callable {

        AbstractMessage message;
        HostedConnection conn;

        public MessageReceived(AbstractMessage message, HostedConnection conn) {
            this.message = message;
            this.conn = conn;
        }

        public Object call() {

            // If it is a ballmessage, set the direction of the ball
            if (message instanceof BallDirectionMessage) {

                BallDirectionMessage bdMessage = (BallDirectionMessage) message;
                long id = bdMessage.getId();
                Ball ball = users.getValue(id).getBall();
                ball.setDirection(bdMessage.getDirection());

            } else if (message instanceof HelloMessage) {

                HelloMessage helloMessage = (HelloMessage) message;
                long authCode = helloMessage.getAuthCode();
                UserData userData = pendingUserData.getValue(authCode);
                // check that everything is in order!
                if (userData != null) {
                    long callerId = userData.getId();
                    // Send information about all active player the the new user:
                    sendConnectedUsersMessage(conn);
                    // Add the new user
                    setupUser(userData, conn);
                    // Inform the other players of the new user.
                    broadUserAddedMessage(callerId);
                }
                // else felmeddelande???


            } else if (message instanceof RequestUsersMessage) {
                RequestUsersMessage ruMessage = (RequestUsersMessage) message;
                long id = ruMessage.getId();
                sendConnectedUsersMessage(conn);

            } else {

                System.err.println("Received odd message:" + message);
            }
            return message;
        }
    }

    private class ConnectionLost implements Callable {

        HostedConnection conn;
        boolean added;

        public ConnectionLost(HostedConnection conn) {
            this.conn = conn;
        }

        public Object call() {
            System.err.println("Ååå nej, en connection tappades, och Nicke gör inget åt det...");
            return conn;
        }
    }

    private void setupUser(UserData userData, HostedConnection conn) {
        long callerId = userData.getId();
        User user = new User(assetManager, userData, conn);
        users.put(callerId, user);

        level.attachChild(user.getGeometry());
        bulletAppState.getPhysicsSpace().add(user.getBall());
    }

    private void initAppState() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        BallCollisionListener collisionListener = new BallCollisionListener();
        bulletAppState.getPhysicsSpace().addCollisionListener(collisionListener);
    }

    private void initLevel() {
        level = new TestLevel(assetManager);
        rootNode.attachChild(level);
        level.initLighting(); //Kasta sen!!!
        bulletAppState.getPhysicsSpace().add(level.getTerrain());
    }

    @Override
    public void destroy() {
        server.close();
        super.destroy();
    }
}

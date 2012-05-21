package mygame.balls.server;

import mygame.balls.messages.BallUpdateMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.Client;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import mygame.admin.BallAcceptedMessage;
import mygame.admin.GameServerStartedMessage;
import mygame.admin.IncomingBallMessage;
import mygame.admin.SerializerHelper;
import mygame.admin.ServerInfo;
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

    private Server server;
    private Client centralServerClient;
    public static final String NAME = "Pimp My Ball Server";
    public static final ServerInfo info = new ServerInfo("Ball Server", "192.168.1.3", 5110);
    private static int timeCounter = 0;
    private BiMap<Long, User> users = new BiMap<Long, User>();
    private Level level;
    private BulletAppState bulletAppState;
    private BiMap<Integer, UserData> pendingUserData = new BiMap<Integer, UserData>();
    AreaOfInterestManager aoiManager;

    /*public static void main(String[] args) throws Exception {
    initializeClasses();
    BallServer app = new BallServer();
    app.start();
    app.setPauseOnLostFocus(false);
    synchronized (NAME) {
    NAME.wait();
    }
    }*/
    public BallServer(ServerInfo centralServerInfo) throws Exception {
        server = Network.createServer(info.NAME, info.VERSION, info.PORT, info.UDP_PORT);
        server.addMessageListener(new ClientMessageListener());
        server.addConnectionListener(new ClientConnectionListener());

        centralServerClient = Network.connectToServer(centralServerInfo.NAME, centralServerInfo.VERSION,
                centralServerInfo.ADDRESS, centralServerInfo.PORT, centralServerInfo.UDP_PORT);
        centralServerClient.addMessageListener(new CentralServerListener());
    }

    @Override
    public void simpleInitApp() {
        SerializerHelper.initializeClasses();
        initAppState();
        initLevel();
        aoiManager = new AreaOfInterestManager(users);
        flyCam.setMoveSpeed(30f);//KAAAST!!!!!
        server.start();
        centralServerClient.start();
        // Send its own server info to the central server
        centralServerClient.send(new GameServerStartedMessage(info));
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
    private void broadcastNewUser(long newId) {
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
    private void broadcastConnectedUsers(HostedConnection connection) {
        ArrayList<UserData> userDataList = new ArrayList<UserData>();

        for (User user : users.getValues()) {
            userDataList.add(user.getUserData());
        }
        ConnectedUsersMessage cuMessage = new ConnectedUsersMessage(userDataList);
        server.broadcast(Filters.equalTo(connection), cuMessage);
    }

    private class MessageReceived implements Callable {

        Message message;
        HostedConnection conn;

        public MessageReceived(Message message, HostedConnection conn) {
            this.message = message;
            this.conn = conn;
        }

        public Object call() {

            System.out.println("BallServer Received message " + message);
            
            // If it is a ballmessage, set the direction of the ball
            if (message instanceof BallDirectionMessage) {
                
                BallDirectionMessage bdMessage = (BallDirectionMessage) message;
                
                long uid = bdMessage.id;
                User user = users.getValue(uid);
                Vector3f dir = bdMessage.direction;
                
                System.out.println("Id: " + uid);
                System.out.println("User: " + user);
                System.out.println("Direction: " + dir);
                
                if (user != null && dir != null) {
                    Ball ball = user.getBall();
                    ball.setDirection(dir);
                }
                
            } else if (message instanceof HelloMessage) {

                HelloMessage helloMessage = (HelloMessage) message;
                // extract the messages UserData
                UserData userData = pendingUserData.removeKey(helloMessage.secret);
                // check that everything is in order!
                if (userData != null) {
                    long callerId = userData.getId();
                    // Send information about all active player the the new user:
                    broadcastConnectedUsers(conn);
                    // Add the new user
                    setupUser(userData, conn);
                    // Inform the other players of the new user.
                    broadcastNewUser(callerId);
                }
                // else felmeddelande???

            } else if (message instanceof RequestUsersMessage) {
                RequestUsersMessage ruMessage = (RequestUsersMessage) message;
                long id = ruMessage.id;
                broadcastConnectedUsers(conn);

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

    private class ClientMessageListener implements MessageListener<HostedConnection> {

        public void messageReceived(HostedConnection source, Message message) {
            BallServer.this.enqueue(new MessageReceived(message, source));
        }
    }

    private class CentralServerListener implements MessageListener<Client> {

        public void messageReceived(Client source, Message message) {

            if (message instanceof IncomingBallMessage) {
                
                System.out.println("BallServer Received message " + message);
                
                IncomingBallMessage ibMessage = (IncomingBallMessage) message;
                pendingUserData.put(ibMessage.secret, ibMessage.userData);
                centralServerClient.send(new BallAcceptedMessage(ibMessage.secret));
            }
        }
    }

    private class ClientConnectionListener implements ConnectionListener {

        public void connectionAdded(Server server, HostedConnection conn) {
            System.out.println("Connection added " + conn);
        }

        public void connectionRemoved(Server server, HostedConnection conn) {
            BallServer.this.enqueue(new ConnectionLost(conn));
        }
    }

    @Override
    public void destroy() {
        server.close();
        super.destroy();
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

    private void setupUser(UserData userData, HostedConnection conn) {
        long callerId = userData.getId();
        
        System.out.println("setupUser with id: " + callerId);
        
        User user = new User(assetManager, userData, conn);
        users.put(callerId, user);

        System.out.println("user is " + users.getValue(callerId));
        
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
}

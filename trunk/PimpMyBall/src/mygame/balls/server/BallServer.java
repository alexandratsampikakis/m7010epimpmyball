package mygame.balls.server;

import mygame.balls.messages.BallUpdateMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import mygame.admin.ChatMessage;
import mygame.admin.Config;
import mygame.admin.NetworkHelper;
import mygame.admin.SerializerHelper;
import mygame.admin.ServerInfo;
import mygame.admin.messages.BackupDataMessage;
import mygame.admin.messages.BallAcceptedMessage;
import mygame.admin.messages.GameServerStartedMessage;
import mygame.admin.messages.IncomingBallMessage;
import mygame.admin.messages.LogoutMessage;
import mygame.admin.messages.UserEnteredServerMessage;
import mygame.admin.messages.UserLeftServerMessage;
import mygame.balls.Ball;
import mygame.balls.TestLevel;
import mygame.balls.UserData;
import mygame.balls.messages.AggregateBallUpdatesMessage;
import mygame.balls.messages.BallDirectionMessage;
import mygame.balls.messages.ConnectedUsersMessage;
import mygame.balls.messages.HelloMessage;
import mygame.balls.messages.RequestUsersMessage;
import mygame.balls.messages.UserAddedMessage;
import mygame.boardgames.GomokuGame;
import mygame.util.GridPoint;
import mygame.boardgames.CellColor;
import mygame.boardgames.WinningRow;
import mygame.boardgames.network.GomokuDrawMessage;
import mygame.boardgames.network.GomokuEndMessage;
import mygame.boardgames.network.GomokuStartMessage;
import mygame.boardgames.network.GomokuUpdateMessage;
import mygame.util.BiMap;

public class BallServer extends SimpleApplication {

    private static final String NAME = "Pimp My Ball Server";
    private static final float shortUpdateTime = 0.1f;
    private static final float longUpdateTime = shortUpdateTime * 50f;
    
    private Server server;
    private Client centralServerClient;
    private ServerInfo info;
    
    private float shortTimeCounter = 0f;
    private float longTimeCounter = 0f;
    
    private BiMap<Long, User> users = new BiMap<Long, User>();
    private BiMap<Integer, UserData> pendingUserData = new BiMap<Integer, UserData>();
    private TestLevel level;
    private BulletAppState bulletAppState;
    private AreaOfInterestManager aoiManager;
    private GomokuServerSlave gomokuSlave;
    
    
    public static void main(String[] args) throws Exception {
        BallServer balls = new BallServer(Config.getCentralServerInfo());
        balls.start(); // JmeContext.Type.Headless);
    }
    
    public BallServer(ServerInfo centralServerInfo) throws Exception {

        String address = NetworkHelper.getLocalIP();
        System.out.println("IP: " + address);
        info = new ServerInfo("Ball Server", address, 5110);

        server = NetworkHelper.createServer(info);
        server.addConnectionListener(new ClientConnectionListener());
        server.addMessageListener(new ClientMessageListener());
        server.addMessageListener(new MessageListener<HostedConnection>() {
            public void messageReceived(HostedConnection source, Message m) {
                server.broadcast(m);
            }
        }, ChatMessage.class);

        gomokuSlave = new GomokuServerSlave(this, server);

        centralServerClient = NetworkHelper.connectToServer(centralServerInfo);
        centralServerClient.addMessageListener(new CentralServerListener());

        this.setPauseOnLostFocus(false);
    }

    @Override
    public void simpleInitApp() {
        
        SerializerHelper.initializeClasses();
        initAppState();
        initLevel();
        aoiManager = new AreaOfInterestManager();
        flyCam.setMoveSpeed(30f); // KAAAST!!!!!
        server.start();
        centralServerClient.start();
        
        // Send its own server info to the central server
        centralServerClient.send(new GameServerStartedMessage(info));
    }

    @Override
    public void simpleUpdate(float tpf) {

        if (longTimeCounter > longUpdateTime) {

            server.broadcast(new AggregateBallUpdatesMessage(users.getValues()));
            longTimeCounter = 0;

        } else if (shortTimeCounter > shortUpdateTime) {
            sendBallUpdatesToAOIs();
            shortTimeCounter = 0;
        }

        shortTimeCounter += tpf;
        longTimeCounter += tpf;

        for (User user : users.getValues()) {
            user.update(tpf);
            aoiManager.setAOIMidpoint(user);
        }
    }

    @Override
    public void destroy() {
        
        centralServerClient.send(new BackupDataMessage(getUserDataList()));
        centralServerClient.close();
        
        server.close();
        
        super.destroy();
    }
    
    
    /**
     * For every user, send an updatemessage to everyone who's interested
     */
    private void sendBallUpdatesToAOIs() {
        for (User user : users.getValues()) {
            Ball ball = user.getBall();
            HashSet filter = aoiManager.getInterestedConnections(user);
            server.broadcast(Filters.in(filter), new BallUpdateMessage(ball));
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
     * Transmit data on all users to a single client
     * @param connection 
     */
    private void sendConnectedUsersToSingleUser(HostedConnection connection) {
        ConnectedUsersMessage cuMessage = new ConnectedUsersMessage(getUserDataList());
        server.broadcast(Filters.equalTo(connection), cuMessage);
    }

    private ArrayList<UserData> getUserDataList() {
        ArrayList<UserData> userDataList = new ArrayList<UserData>();
        for (User user : users.getValues()) {
            UserData userData = user.getUserData();
            userData.position = user.getBall().getPosition();
            userDataList.add(user.getUserData());
        }
        return userDataList;
    }

    private class MessageReceived implements Callable {

        Message message;
        HostedConnection conn;

        public MessageReceived(Message message, HostedConnection conn) {
            this.message = message;
            this.conn = conn;
        }

        public Object call() {

            // If it is a ballmessage, set the direction of the ball
            if (message instanceof BallDirectionMessage) {

                BallDirectionMessage bdMessage = (BallDirectionMessage) message;

                long uid = bdMessage.id;
                User user = users.getValue(uid);
                Vector3f dir = bdMessage.direction;

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
                    sendConnectedUsersToSingleUser(conn);
                    // Add the new user
                    User user = setupUser(userData, conn);
                    // Inform the other players of the new user.
                    broadcastNewUser(callerId);
                    // Inform the central server that the new user is added.
                    centralServerClient.send(new UserEnteredServerMessage(user));
                }
                // else felmeddelande???

            } else if (message instanceof LogoutMessage) {
                LogoutMessage lMessage = (LogoutMessage) message;
                User user = users.getValue(lMessage.userId);
                removeUser(user);
                
            } else if (message instanceof RequestUsersMessage) {
                RequestUsersMessage ruMessage = (RequestUsersMessage) message;
                sendConnectedUsersToSingleUser(conn);

            } else {
                System.err.println("Received odd message:" + message);
            }
            return message;
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
    
    private class ConnectionLost implements Callable {

        HostedConnection conn;
        boolean added;

        public ConnectionLost(HostedConnection conn) {
            this.conn = conn;
        }

        public Object call() {
            long lostId = (Long) conn.getAttribute("ID");
            User lostUser = users.getValue(lostId);
            removeUser(lostUser);
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
                IncomingBallMessage ibMessage = (IncomingBallMessage) message;
                pendingUserData.put(ibMessage.secret, ibMessage.userData);
                centralServerClient.send(new BallAcceptedMessage(ibMessage.secret));
            }
        }
    }

    public class BallCollisionListener implements PhysicsCollisionListener {

        public void collision(PhysicsCollisionEvent event) {
            Object a = event.getObjectA();
            Object b = event.getObjectB();
            if (a instanceof Ball && b instanceof Ball) {

                Ball ballA = (Ball) a;
                Ball ballB = (Ball) b;
                User userA = users.getValue(ballA.getId());
                User userB = users.getValue(ballB.getId());

                if (userA.canPlay() && userB.canPlay()) {

                    // Stop the balls from moving
                    ballA.setMass(0);
                    ballB.setMass(0);

                    gomokuSlave.startGame(userA, userB);
                }
            }
        }
    }

    private User setupUser(UserData userData, HostedConnection conn) {
        long callerId = userData.getId();

        System.out.println("setupUser with id: " + callerId);

        User user = new User(assetManager, userData, conn);
        users.put(callerId, user);

        System.out.println("user is " + users.getValue(callerId));

        level.attachChild(user.getGeometry());
        bulletAppState.getPhysicsSpace().add(user.getBall());

        user.getBall().setPosition(userData.position);

        conn.setAttribute("ID", callerId);

        return user;
    }
    
    public User getUser(long id) {
        return users.getValue(id);
    }

    private void removeUser(User lostUser) {
        
        // Player loses any gomoku game he's currently playing
        gomokuSlave.playerLeftGame(lostUser);
        
        UserLeftServerMessage ulMessage = new UserLeftServerMessage(lostUser);

        // Inform the central server that the user has logged out
        centralServerClient.send(new BackupDataMessage(lostUser.getUserData()));
        centralServerClient.send(ulMessage);
        
        // Inform all remaining users
        server.broadcast(ulMessage);
        
        // Remove the user completely
        users.removeValue(lostUser);
        level.detachChild(lostUser.getGeometry());
        bulletAppState.getPhysicsSpace().remove(lostUser.getBall());
    }

    private void initAppState() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        BallCollisionListener collisionListener = new BallCollisionListener();
        bulletAppState.getPhysicsSpace().addCollisionListener(collisionListener);
    }

    private void initLevel() {
        level = new TestLevel(assetManager, bulletAppState);
        rootNode.attachChild(level);
        level.initGraphics(assetManager); //Kasta sen!!!
    }
}

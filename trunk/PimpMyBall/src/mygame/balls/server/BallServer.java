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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import mygame.admin.CentralServer;
import mygame.admin.ChatMessage;
import mygame.admin.NetworkHelper;
import mygame.admin.SerializerHelper;
import mygame.admin.ServerInfo;
import mygame.admin.messages.BallAcceptedMessage;
import mygame.admin.messages.GameServerStartedMessage;
import mygame.admin.messages.IncomingBallMessage;
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
import mygame.boardgames.GridPoint;
import mygame.boardgames.gomoku.CellColor;
import mygame.boardgames.gomoku.WinningRow;
import mygame.boardgames.network.GomokuServerSlave;
import mygame.boardgames.network.broadcast.GomokuEndMessage;
import mygame.boardgames.network.broadcast.GomokuStartMessage;
import mygame.boardgames.network.broadcast.GomokuUpdateMessage;
import mygame.util.BiMap;

public class BallServer extends SimpleApplication {

    private Server server;
    private Client centralServerClient;
    public static final String NAME = "Pimp My Ball Server";
    public ServerInfo info;// = new ServerInfo("Ball Server", "192.168.1.5", 5110);
    // = new ServerInfo("Ball Server", "130.240.110.57", 5110);
    private float shortTimeCounter = 0f;
    private float longTimeCounter = 0f;
    private BiMap<Long, User> users = new BiMap<Long, User>();
    private TestLevel level;
    private BulletAppState bulletAppState;
    private BiMap<Integer, UserData> pendingUserData = new BiMap<Integer, UserData>();
    private AreaOfInterestManager aoiManager;
    private GomokuServerSlave gomokuSlave;
    private final float shortUpdateTime = 0.1f;
    private final float longInterval = shortUpdateTime * 10f;

    public BallServer(ServerInfo centralServerInfo) throws Exception {

        String address = getIp();
        System.out.println("ip: " + address);
        info = new ServerInfo("Ball Server", address, 5110);

        server = NetworkHelper.createServer(info);
        server.addMessageListener(new ClientMessageListener());
        server.addConnectionListener(new ClientConnectionListener());

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

    public static void main(String[] args) throws Exception {

        BallServer balls = new BallServer(CentralServer.info);
        balls.start(); // JmeContext.Type.Headless);

        // Run FOREVER!
        synchronized (CentralServer.info) {
            CentralServer.info.wait();
        }
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
    
    private void broadcastAggregateBallUpdates() {
        ArrayList<Ball> allBalls = new ArrayList<Ball>();
        for (User user : users.getValues()) {
            allBalls.add(user.getBall());
        }
        server.broadcast(new AggregateBallUpdatesMessage(allBalls));
        System.out.append("Agge!");
    }
            
    //SNÖÖÖR!!
    /*
    private void broadcastBallData(boolean toAll) {
        for (User user : users.getValues()) {
            Ball ball = user.getBall();
            BallUpdateMessage ballMessage = new BallUpdateMessage(ball);

            if (toAll) { // check if the message should be sent to all clients
                server.broadcast(ballMessage);
                //.---------
                System.out.println("TO ALL!!!!!!");
                //.---------

            } else {// Otherwise, send only to interested clients
                HashSet filter = aoiManager.getInterestedConnections(user);
                server.broadcast(Filters.in(filter), ballMessage);
                //.---------
                System.out.println("TO SOME: ");
                for (Object o : filter) {
                    HostedConnection h = (HostedConnection) o;
                    System.out.println("    " + h.getAttribute("ID"));
                }
                //.---------
            }
        }
    }*/

    public void broadcastGomokuUpdate(GomokuGame game, CellColor color, GridPoint p) {
        server.broadcast(new GomokuUpdateMessage(game, color, p));
    }

    public void broadcastGomokuGameStarted(GomokuGame game) {
        server.broadcast(new GomokuStartMessage(game));
    }

    public void broadcastGomokuGameFinished(GomokuGame game, WinningRow row) {
        int scoreChange = 50; // TODO: Compute score change
        server.broadcast(new GomokuEndMessage(game, row, scoreChange));
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

    /*
    private void broadcastConnectedUsers() {
        ConnectedUsersMessage cuMessage = new ConnectedUsersMessage(getUserDataList());
        server.broadcast(cuMessage);
    }*/
    
    

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

            // System.out.println("BallServer Received message " + message);

            // If it is a ballmessage, set the direction of the ball
            if (message instanceof BallDirectionMessage) {

                BallDirectionMessage bdMessage = (BallDirectionMessage) message;

                long uid = bdMessage.id;
                User user = users.getValue(uid);
                Vector3f dir = bdMessage.direction;

                /*
                System.out.println("Id: " + uid);
                System.out.println("User: " + user);
                System.out.println("Direction: " + dir);
                 */

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
                    setupUser(userData, conn);
                    // Inform the other players of the new user.
                    broadcastNewUser(callerId);
                }
                // else felmeddelande???

            } else if (message instanceof RequestUsersMessage) {
                RequestUsersMessage ruMessage = (RequestUsersMessage) message;
                //long id = ruMessage.id;
                sendConnectedUsersToSingleUser(conn);

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

                // System.out.println("BallServer Received message " + message);

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

    public class BallCollisionListener implements PhysicsCollisionListener {

        public void collision(PhysicsCollisionEvent event) {
            Object a = event.getObjectA();
            Object b = event.getObjectB();
            if (a instanceof Ball && b instanceof Ball) {

                Ball ballA = (Ball) a;
                Ball ballB = (Ball) b;
                User userA = users.getValue(ballA.getId());
                User userB = users.getValue(ballB.getId());

                if (ballA.isKinematic() && ballB.isKinematic()) {

                    // Stop the balls from moving
                    ballA.setKinematic(true);
                    ballA.setKinematic(true);
                    gomokuSlave.startGame(userA, userB);
                }
            }
        }
    }

    @Override
    public void destroy() {
        server.close();
        super.destroy();
    }

    @Override
    public void simpleUpdate(float tpf) {

        if (longTimeCounter > longInterval) {
            broadcastAggregateBallUpdates();
            longTimeCounter = 0;
        } else if (shortTimeCounter > shortUpdateTime) {
            sendBallUpdatesToAOIs();
            shortTimeCounter = 0;
        }

        shortTimeCounter += tpf;
        longTimeCounter += tpf;

        for (User user : users.getValues()) {
            user.update();
            aoiManager.setAOIMidpoint(user);
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

        user.getBall().setPosition(userData.position);

        conn.setAttribute("ID", callerId);
    }

    private void removeUser(User lostUser) {
        users.removeValue(lostUser);
        level.detachChild(lostUser.getGeometry());
        bulletAppState.getPhysicsSpace().remove(lostUser.getBall());
        //server.broadcast(UserLeftServerMessage(lostUser.getId()));
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

    private String getIp() throws UnknownHostException {
        String address = "";

        InetAddress addr = InetAddress.getLocalHost();
        // Get IP Address
        byte[] ipAddr = addr.getAddress();
        String i0 = Integer.toString((ipAddr[0] & 0xFF));
        String i1 = Integer.toString((ipAddr[1] & 0xFF));
        String i2 = Integer.toString((ipAddr[2] & 0xFF));
        String i3 = Integer.toString((ipAddr[3] & 0xFF));
        address = i0 + "." + i1 + "." + i2 + "." + i3;
        return address;
    }
}

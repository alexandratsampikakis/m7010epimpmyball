package mygame.network;

import mygame.network.messages.BallMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import java.util.concurrent.Callable;
import mygame.network.messages.UserAddedMessage;

public class BallServer extends SimpleApplication {

    public static void initializeClasses() {
        // Doing it here means that the client code only needs to
        // call our initialize. 
        Serializer.registerClass(BallMessage.class);
        Serializer.registerClass(UserAddedMessage.class);
    }
    private Level level;
    private BulletAppState bulletAppState;
    private static Server server;
    public static final String NAME = "Pimp My Ball Server";
    public static final int VERSION = 1;
    public static final int PORT = 5110;
    public static final int UDP_PORT = 5110;
    private static int timeCounter = 0;
    private static UserList userList = new UserList();

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
        MessageListener messageListener = new MessageListener<HostedConnection>() {

            public void messageReceived(HostedConnection conn, Message message) {
                if (message instanceof BallMessage) {
                    BallServer.this.enqueue(new MyCallable((BallMessage) message, conn));
                } else {
                    System.err.println("Received odd message:" + message);
                }
            }
        };
        server.addMessageListener(messageListener);

        ConnectionListener connectionListener = new ConnectionListener() {

            public void connectionAdded(Server server, HostedConnection conn) {
                addNewUser(conn.getId(), conn);//Using connection id as user id, temporary solution
            }

            public void connectionRemoved(Server server, HostedConnection conn) {
                for (User user : userList) {
                    ServerSideUser serverSideUser = (ServerSideUser) user;
                    if (conn == serverSideUser.getHostedConnection()) {
                        userList.remove(user);
                    }
                }
            }
        };
        server.addConnectionListener(connectionListener);
        flyCam.setMoveSpeed(30f);//KAAAST!!!!!
    }

    private void initAppState() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
    }

    private void initLevel() {
        level = new TestLevel(assetManager);
        rootNode.attachChild(level);
        level.initLighting(); //Kasta sen!!!
        bulletAppState.getPhysicsSpace().add(level.getTerrain());
    }

    @Override
    public void update() {
        super.update();
        if (timeCounter > 5) {
            timeCounter = 0;
            broadcastData();
        }
        timeCounter++;
        for (User user : userList) {
            user.moveForward();
            System.out.print("Position: " + user.getPosition());
        }
    }

    private void broadcastData() {
        Vector3f pos = Vector3f.ZERO;
        Vector3f vel = Vector3f.ZERO;
        Vector3f dir = Vector3f.ZERO;
        for (User user : userList) {
            long id = user.getId();
            pos = user.getPosition();
            vel = user.getVelocity();
            dir = user.getDirection();
            BallMessage ballMessage = new BallMessage(id, pos, vel, dir);
            ballMessage.setReliable(false);
            server.broadcast(ballMessage);
            System.out.println("Position: " + user.getPosition());

            System.out.println("Direction: " + user.getDirection());

        }
    }

    private class MyCallable implements Callable {

        AbstractMessage message;
        HostedConnection conn;

        public MyCallable(AbstractMessage message, HostedConnection conn) {
            this.message = message;
            this.conn = conn;
        }

        public Object call() {
            // If it is a ballmessage, set the direction of the ball
            if (message instanceof BallMessage) {
                BallMessage ballMessage = (BallMessage) message;
                long id = ballMessage.getId();
                User user = userList.getUserWithId(id);
                user.setDirection(ballMessage.getDirection());
            }
            return message;
        }
    }

    @Override
    public void destroy() {
        server.close();
        super.destroy();
    }

    private void addNewUser(long id, HostedConnection conn) {
        ServerSideUser newUser = new ServerSideUser(assetManager, id, conn);
        userList.add(newUser);
        bulletAppState.getPhysicsSpace().add(newUser.getControl());
        level.attachChild(newUser.getGeometry());
        /*/OSPARVÄRT//////////*/ newUser.setPosition(new Vector3f(0f, 100f, 0f));

    }
}

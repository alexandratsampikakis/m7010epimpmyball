/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.app.SimpleApplication;
import mygame.admin.messages.GameServerStartedMessage;
import mygame.admin.messages.LoginSuccessMessage;
import mygame.admin.messages.LoginMessage;
import mygame.admin.messages.LoginFailedMessage;
import mygame.admin.messages.LoginError;
import mygame.admin.messages.IncomingBallMessage;
import mygame.admin.messages.UserLeftServerMessage;
import mygame.admin.messages.UserEnteredServerMessage;
import mygame.admin.messages.BackupDataMessage;
import mygame.admin.messages.BallAcceptedMessage;
import mygame.admin.messages.BallRejectedMessage;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import mygame.admin.messages.FailedToRegisterMessage;
import mygame.admin.messages.RegisterUserMessage;
import mygame.balls.UserData;
import mygame.balls.server.BallServer;

/**
 *
 * @author Jimmy
 */
public class CentralServer extends SimpleApplication {

    private Server server;
    private AuthServer authServer = AuthServer.getInstance();
    private ArrayList<HostedConnection> hostedConnections = new ArrayList<HostedConnection>();
    private ArrayList<HostedConnection> gameServerConnections = new ArrayList<HostedConnection>();
    private HashMap<Integer, PendingUserInfo> pendingUsers = new HashMap<Integer, PendingUserInfo>();

    public static void main(String[] args) throws Exception {

        CentralServer cs = new CentralServer();
        cs.start(JmeContext.Type.Headless);

        BallServer balls = new BallServer(Config.getCentralServerInfo());
        balls.start(); // JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {}
    
    @Override
    public void simpleUpdate(float tpf) {}

    @Override
    public void destroy() {
        authServer.saveData(null);
        server.close();
    }
    
    private class PendingUserInfo {
        ServerInfo serverInfo;
        HostedConnection conn;
        UserData userData;
    }

    public CentralServer() throws Exception {

        SerializerHelper.initializeClasses();
        
        server = NetworkHelper.createServer(Config.getCentralServerInfo());
        server.addMessageListener(clientListener,
                LoginMessage.class,
                RegisterUserMessage.class);
        server.addMessageListener(gameServerListener,
                GameServerStartedMessage.class,
                BackupDataMessage.class,
                BallAcceptedMessage.class,
                BallRejectedMessage.class);

        server.addConnectionListener(new ConnectionListener() {
            public void connectionAdded(Server server, HostedConnection conn) {
                hostedConnections.add(conn);
            }
            public void connectionRemoved(Server server, HostedConnection conn) {
                hostedConnections.remove(conn);
            }
        });
        
        server.start();
    }

    private HostedConnection chooseGameServer(UserData userData) {
        // TODO: load balancing here! :)
        return hostedConnections.get(0);
    }

    private void sendMessage(HostedConnection conn, Message m) {
        server.broadcast(Filters.in(conn), m);
    }
    
    private MessageListener<HostedConnection> clientListener =
            new MessageListener<HostedConnection>() {
                @Override
                public void messageReceived(HostedConnection source, Message m) {
                    CentralServer.this.enqueue(new ClientMessageCallable(source, m));
                }
            };

    
    private class ClientMessageCallable implements Callable {

        HostedConnection source;
        Message m;

        public ClientMessageCallable(HostedConnection source, Message m) {
            this.source = source;
            this.m = m;
        }

        public Object call() throws Exception {

            if (m instanceof LoginMessage) {

                LoginMessage lm = (LoginMessage) m;
                UserData userData = authServer.authenticate(lm.userName, lm.password);

                System.out.println("Received message from " + lm.userName);
                System.out.println("Password is " + lm.password);
                System.out.println("Found user data: " + userData);

                if (userData == null) {
                    sendMessage(source, new LoginFailedMessage(LoginError.WRONG_PASSWORD));

                } else if (authServer.isLoggedIn(userData.getId())) {
                    sendMessage(source, new LoginFailedMessage(LoginError.ALREADY_LOGGED_IN));

                } else {
                    int hash = lm.password.hashCode();
                    int id = (int) userData.getId();
                    int secret = hash ^ id;

                    IncomingBallMessage warning = new IncomingBallMessage(userData, secret);
                    HostedConnection gameServerConn = chooseGameServer(userData);

                    sendMessage(gameServerConn, warning);

                    PendingUserInfo info = new PendingUserInfo();
                    info.conn = source;
                    info.userData = userData;
                    info.serverInfo = gameServerConn.getAttribute("ServerInfo");

                    // Store the connection and wait for
                    pendingUsers.put(secret, info);

                    System.out.println("Sending IncomingBallMessage.");
                }

            } else if (m instanceof RegisterUserMessage) {

                RegisterUserMessage rum = (RegisterUserMessage) m;
                UserData data = authServer.createUser(rum.userName, rum.password);

                if (data == null) {
                    sendMessage(source, new FailedToRegisterMessage("User name is taken."));

                } else {
                    int hash = rum.password.hashCode();
                    int id = (int) data.getId();
                    int secret = hash ^ id;

                    IncomingBallMessage warning = new IncomingBallMessage(data, secret);
                    HostedConnection gameServerConn = chooseGameServer(data);

                    sendMessage(gameServerConn, warning);

                    PendingUserInfo info = new PendingUserInfo();
                    info.conn = source;
                    info.userData = data;
                    info.serverInfo = gameServerConn.getAttribute("ServerInfo");

                    // Store the connection and wait for
                    pendingUsers.put(secret, info);

                }
            }
            return m;
        }
    }
    
    
    private MessageListener<HostedConnection> gameServerListener =
            new MessageListener<HostedConnection>() {
                @Override
                public void messageReceived(HostedConnection source, Message m) {
                    CentralServer.this.enqueue(new GameServerCallable(source, m));
                }
            };

    
    private class GameServerCallable implements Callable {

        HostedConnection source;
        Message m;

        GameServerCallable(HostedConnection source, Message m) {
            this.source = source;
            this.m = m;
        }

        @Override
        public Object call() throws Exception {

            if (m instanceof BallAcceptedMessage) {
                BallAcceptedMessage bam = (BallAcceptedMessage) m;
                PendingUserInfo info = pendingUsers.remove(bam.secret);

                if (info != null) {
                    LoginSuccessMessage message = new LoginSuccessMessage(
                            info.userData, bam.secret, info.serverInfo);
                    sendMessage(info.conn, message);
                } else {
                    System.out.println("Null info!!!");
                }

            } else if (m instanceof BallRejectedMessage) {
                BallRejectedMessage brm = (BallRejectedMessage) m;
                PendingUserInfo info = pendingUsers.remove(brm.secret);
                sendMessage(info.conn, new LoginFailedMessage(LoginError.SERVER_FULL));

            } else if (m instanceof GameServerStartedMessage) {
                GameServerStartedMessage gssm = (GameServerStartedMessage) m;
                source.setAttribute("ServerInfo", gssm.serverInfo);
                source.setAttribute("NumPlayers", new Integer(0));
                gameServerConnections.add(source);

            } else if (m instanceof BackupDataMessage) {
                authServer.saveData(((BackupDataMessage) m).data);

            } else if (m instanceof UserEnteredServerMessage) {
                Integer numPlayers = source.getAttribute("NumPlayers");
                source.setAttribute("NumPlayers", new Integer(numPlayers + 1));
                authServer.userLoggedIn(((UserEnteredServerMessage) m).userId);

            } else if (m instanceof UserLeftServerMessage) {
                Integer numPlayers = source.getAttribute("NumPlayers");
                source.setAttribute("NumPlayers", new Integer(numPlayers - 1));
                authServer.userLoggedOut(((UserEnteredServerMessage) m).userId);
            }
            return m;
        }
    }
}

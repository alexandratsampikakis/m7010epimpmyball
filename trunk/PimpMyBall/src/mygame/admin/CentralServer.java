/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;
import java.util.HashMap;
import mygame.balls.UserData;

/**
 *
 * @author Jimmy
 */
public class CentralServer {
    
    private static final String NAME = "Central Server";
    private static final int VERSION = 1;
    private static final int PORT = 5110;
    private static final int UDP_PORT = 5110;

    public static final ServerInfo info = new ServerInfo("Central Server", "localhost", 5110);
            
    private Server server;
    
    private ArrayList<HostedConnection> hostedConnections = new ArrayList<HostedConnection>();
    private ArrayList<HostedConnection> gameServerConnections = new ArrayList<HostedConnection>();
    private HashMap<Integer, PendingUserInfo> pendingUsers = new HashMap<Integer, PendingUserInfo>();
    
    private AuthServer authServer = AuthServer.createServer("fakeAuth");
    
    public static void initializeClasses() {
        Serializer.registerClass(LoginMessage.class);
        Serializer.registerClass(LoginFailedMessage.class);
    }
    
    private class PendingUserInfo {
         ServerInfo serverInfo;
         HostedConnection conn;
         UserData userData;
    }
    
    
    public CentralServer() throws Exception {
        
        initializeClasses();

        server = Network.createServer(NAME, VERSION, PORT, UDP_PORT);
        
        server.addMessageListener(clientListener, 
                LoginMessage.class);
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
    }
    
    
    private HostedConnection chooseGameServer(UserData userData) {
        return hostedConnections.get(0);
    }
    
    private void sendMessage(HostedConnection conn, Message m) {
        server.broadcast(Filters.in(conn), m);
    }
    
    private MessageListener<HostedConnection> clientListener =
            new MessageListener<HostedConnection>() {

                @Override
                public void messageReceived(HostedConnection source, Message m) {

                    if (m instanceof LoginMessage) {

                        LoginMessage lm = (LoginMessage) m;
                        UserData userData = authServer.authenticate(lm.userName, lm.password);

                        if (userData == null) {
                            sendMessage(source, new LoginFailedMessage(LoginError.WRONG_PASSWORD));
                            
                        } else {
                            
                            int hash = lm.password.hashCode();
                            int id = (int) userData.getId();
                            int secret = hash ^ id;
                            
                            IncomingBallMessage warning = 
                                    new IncomingBallMessage(userData, secret);
                            HostedConnection gameServerConn = 
                                    chooseGameServer(userData);
                            
                            sendMessage(gameServerConn, warning);
                            
                            PendingUserInfo info = new PendingUserInfo();
                            info.conn = source;
                            info.userData = userData;
                            info.serverInfo = gameServerConn.getAttribute("ServerInfo");
                            
                            // Store the connection and wait for
                            pendingUsers.put(secret, info);
                        }

                    } 
                    // Kanske från AuthServer:
                    // else if (m instanceof LoginResponseMessage) {
                    //    LoginResponseMessage lrm = (LoginResponseMessage) m;
                    // }
                }
            };
    
    
     private MessageListener<HostedConnection> gameServerListener =
            new MessageListener<HostedConnection>() {
                @Override
                public void messageReceived(HostedConnection source, Message m) {
                    
                    if (m instanceof BallAcceptedMessage) {
                        BallAcceptedMessage bam = (BallAcceptedMessage) m;
                        PendingUserInfo info = pendingUsers.remove(bam.secret);
                        
                        if (info != null) {
                            LoginSuccessMessage message = new LoginSuccessMessage(
                                    info.userData, bam.secret, info.serverInfo);
                            sendMessage(info.conn, message);
                        }
                        
                    } else if (m instanceof BallRejectedMessage) {
                        // BallRejectedMessage brm = (BallRejectedMessage) m;
                        // TODO: FEL!! SKICKA TILL RÄTT SPELARE!!!
                        sendMessage(source, new LoginFailedMessage(LoginError.SERVER_FULL));
                        
                    } else if (m instanceof GameServerStartedMessage) {
                        GameServerStartedMessage gssm = (GameServerStartedMessage) m;
                        source.setAttribute("ServerInfo", gssm.serverInfo);
                        gameServerConnections.add(source);
                        
                    } else if (m instanceof BackupDataMessage) {
                        BackupDataMessage bdm = (BackupDataMessage) m;
                        
                    }
                }
            };
}

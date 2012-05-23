/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

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
import java.util.ArrayList;
import java.util.HashMap;
import mygame.balls.UserData;
import mygame.balls.server.BallServer;

/**
 *
 * @author Jimmy
 */
public class CentralServer {

    public static final ServerInfo info = new ServerInfo("Central Server", "192.168.1.3", 5111);
            // = new ServerInfo("Central Server", "130.240.110.57", 5111);
            
    private Server server;
    private AuthServer authServer = AuthServer.createServer("fakeAuth");
    private ArrayList<HostedConnection> hostedConnections = new ArrayList<HostedConnection>();
    private ArrayList<HostedConnection> gameServerConnections = new ArrayList<HostedConnection>();
    private HashMap<Integer, PendingUserInfo> pendingUsers = new HashMap<Integer, PendingUserInfo>();
    
    public static void main(String[] args) throws Exception {
        
        CentralServer cs = new CentralServer();
        cs.server.start();
        
        BallServer balls = new BallServer(info);
        balls.start(); // JmeContext.Type.Headless);
        
        // Run FOREVER!
        synchronized (info) {
            info.wait();
        }
    }
    
    private class PendingUserInfo {
         ServerInfo serverInfo;
         HostedConnection conn;
         UserData userData;
    }
    
    public CentralServer() throws Exception {
        
        SerializerHelper.initializeClasses();

        server = NetworkHelper.createServer(info);
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
                            
                            System.out.println("Sending IncomingBallMessage.");
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
                        
                        System.out.println("Reply from BallServer.");
                        
                        if (info != null) {
                            LoginSuccessMessage message = new LoginSuccessMessage(
                                    info.userData, bam.secret, info.serverInfo);
                            sendMessage(info.conn, message);
                            
                            System.out.println("Sending LoginSuccessMessage.");
                        } else {
                            System.out.println("Null info!!!");
                        }
                        
                    } else if (m instanceof BallRejectedMessage) {
                        // BallRejectedMessage brm = (BallRejectedMessage) m;
                        // TODO: FEL!! SKICKA TILL RÄTT SPELARE!!!
                        // TODO: Plus, prova nästa server om den finns
                        sendMessage(source, new LoginFailedMessage(LoginError.SERVER_FULL));
                       
                    } else if (m instanceof GameServerStartedMessage) {
                        GameServerStartedMessage gssm = (GameServerStartedMessage) m;
                        source.setAttribute("ServerInfo", gssm.serverInfo);
                        gameServerConnections.add(source);
                        
                    } else if (m instanceof BackupDataMessage) {
                        authServer.saveData(((BackupDataMessage) m).data);
                    
                    } else if (m instanceof UserEnteredServerMessage) {
                        authServer.setUserOnline(((UserEnteredServerMessage) m).userId);
                        
                    } else if (m instanceof UserLeftServerMessage) {
                        authServer.setUserOffline(((UserEnteredServerMessage) m).userId);
                        
                    }
                }
            };
}

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
import mygame.balls.UserData;

/**
 *
 * @author Jimmy
 */
public class CentralServer {
    
    public static final String NAME = "Central Server";
    public static final int VERSION = 1;
    public static final int PORT = 5110;
    public static final int UDP_PORT = 5110;

    private Server server;
    private ArrayList<HostedConnection> hostedConnections = new ArrayList<HostedConnection>();
    
    private AuthServer authServer = AuthServer.createServer("fakeAuth");
    
    public static void initializeClasses() {
        Serializer.registerClass(LoginMessage.class);
        Serializer.registerClass(LoginFailedMessage.class);
    }
    
    public CentralServer() throws Exception {
        
        initializeClasses();

        server = Network.createServer(NAME, VERSION, PORT, UDP_PORT);
        server.addMessageListener(new MessageListener<HostedConnection>() {
            
            @Override
            public void messageReceived(HostedConnection source, Message m) {
                
                if (m instanceof LoginMessage) {
                
                    LoginMessage lm = (LoginMessage) m;
                    UserData userData = authServer.authenticate(lm.userName, lm.password);
                    
                    if (userData == null) {
                        server.broadcast(Filters.in(source), new LoginFailedMessage(
                                LoginError.WRONG_PASSWORD));
                    } else {
                        
                        int hash = lm.password.hashCode();
                        int id = (int) userData.getId();
                        int secret = hash ^ id;
                        
                        LoginMessage warning = new LoginMessage(secret);
                        
                        // Skicka till spelservern på nåt sätt och
                        // invänta svar
                        
                        /* OBS!
                         * source.setAttribute("id", blabla)
                         */
                    }
                    
                } else if (m instanceof LoginResponseMessage) {
                    
                    LoginResponseMessage lrm = (LoginResponseMessage) m;
                    
                    
                    
                }
                
                    
            }
        }); // , LoginMessage.class, LoginResponseMessage.class);
        
        server.addConnectionListener(new ConnectionListener() {
            public void connectionAdded(Server server, HostedConnection conn) {                
                hostedConnections.add(conn);
            }
            public void connectionRemoved(Server server, HostedConnection conn) {
                hostedConnections.remove(conn);
            }
        });
    }
}

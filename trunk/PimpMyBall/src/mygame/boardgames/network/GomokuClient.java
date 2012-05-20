package mygame.boardgames.network;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.RenderManager;
import java.awt.Component;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mygame.boardgames.BoardGameAppState;


/**
 * test
 * @author Jimmy
 */
public class GomokuClient extends SimpleApplication {
    
    private String host;
    private Client client;
    
    public static void main(String[] args) throws Exception {
        
        GomokuServer.initializeClasses();

        // Grab a host string from the user
        String s = getString(null, "Host Info", "Enter gomoku host:", "localhost");
        if (s == null) {
            System.out.println("User cancelled.");
            return;
        }

        GomokuClient client = new GomokuClient(s);
        client.start();
    }

    public static String getString(Component owner, String title, String message, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                owner, message, title, 
                JOptionPane.PLAIN_MESSAGE,
                null, null, initialValue);
    }
    
    public GomokuClient(String host) {
        this.host = host;
    }
    
    @Override
    public void simpleInitApp() {
        
        try {
            client = Network.connectToServer(GomokuServer.NAME, GomokuServer.VERSION,
                    host, GomokuServer.PORT, GomokuServer.UDP_PORT);
            
            client.addMessageListener(new MessageListener<Client>() {
                public void messageReceived(Client source, Message m) {
                    enqueue(new MessageParser(m));
                }
            }, NewGameMessage.class);
            
            client.start();
            
        } catch (IOException ex) {
            Logger.getLogger(GomokuClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void simpleUpdate(float tpf) {}
    @Override
    public void simpleRender(RenderManager rm) {}
   
    
    private class MessageParser implements Callable {
        
        private Message msg;
      
        public MessageParser(Message msg) {
            this.msg = msg;
        }
        
        @Override
        public Object call() throws Exception {
            if (msg instanceof NewGameMessage) {
                BoardGameAppState bgas = new BoardGameAppState(client, (NewGameMessage) msg);
                stateManager.attach(bgas);
            }
            return msg;
        }
    }
    
    @Override
    public void destroy() {
        client.close();
    }
}

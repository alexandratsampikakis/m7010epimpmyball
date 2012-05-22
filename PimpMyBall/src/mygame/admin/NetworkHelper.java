/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.Client;
import com.jme3.network.Network;
import com.jme3.network.Server;
import java.io.IOException;

/**
 *
 * @author Jimmy
 */
public class NetworkHelper {
    
    public static Client connectToServer(ServerInfo info) throws IOException {
        return Network.connectToServer(
                info.NAME, info.VERSION, info.ADDRESS, 
                info.PORT, info.UDP_PORT);
    }
    
    public static Server createServer(ServerInfo info) throws IOException {
        return Network.createServer(
                info.NAME, info.VERSION, 
                info.PORT, info.UDP_PORT);
    }
}

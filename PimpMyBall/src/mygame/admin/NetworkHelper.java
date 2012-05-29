/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.Client;
import com.jme3.network.Network;
import com.jme3.network.Server;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
    
    
    public static String getLocalIP() throws UnknownHostException {
        
        InetAddress addr = InetAddress.getLocalHost();
        // Get IP Address
        byte[] ipAddr = addr.getAddress();
        String i0 = Integer.toString((ipAddr[0] & 0xFF));
        String i1 = Integer.toString((ipAddr[1] & 0xFF));
        String i2 = Integer.toString((ipAddr[2] & 0xFF));
        String i3 = Integer.toString((ipAddr[3] & 0xFF));
        String address = i0 + "." + i1 + "." + i2 + "." + i3;
        return address;
    }
}

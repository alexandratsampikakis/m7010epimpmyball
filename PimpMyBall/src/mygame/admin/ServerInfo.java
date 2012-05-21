/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.serializing.Serializable;

/**
 *  Data holder for server information.
 * 
 * @author Jimmy
 */
@Serializable
public class ServerInfo {
    
    public final String NAME;
    public final String ADDRESS;
    public final int VERSION = 1;
    public final int PORT;
    public final int UDP_PORT;

    public ServerInfo(String name, String ip, int port) {
        this.NAME = name;
        this.ADDRESS = ip;
        this.PORT = this.UDP_PORT = port;
    }
}

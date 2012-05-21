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
    
    public String NAME;
    public String ADDRESS;
    public int VERSION = 1;
    public int PORT;
    public int UDP_PORT;

    public ServerInfo() {
    }
    public ServerInfo(String name, String ip, int port) {
        this.NAME = name;
        this.ADDRESS = ip;
        this.PORT = this.UDP_PORT = port;
    }
}

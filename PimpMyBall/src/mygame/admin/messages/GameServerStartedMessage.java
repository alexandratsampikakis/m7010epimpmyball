/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.admin.ServerInfo;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GameServerStartedMessage extends AbstractMessage {
    
    public ServerInfo serverInfo;
            
    public GameServerStartedMessage() {    
    }
    
    public GameServerStartedMessage(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}

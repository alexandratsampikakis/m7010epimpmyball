/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Very safe login message sends the password in plaintext.
 * 
 * @author Jimmy
 */
@Serializable
public class LogoutMessage extends AbstractMessage {
    
    public long userId = -1;
    
    public LogoutMessage() {
    }
    public LogoutMessage(mygame.balls.client.User user) {
        this.userId = user.getId();
    }
    public LogoutMessage(mygame.balls.server.User user) {
        this.userId = user.getId();
    }
}

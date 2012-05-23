/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.balls.server.User;

/**
 *
 * @author Jimmy
 */
@Serializable
public class UserEnteredServerMessage extends AbstractMessage {
    
    public long userId = -1;
    
    public UserEnteredServerMessage() {
    }
    
    public UserEnteredServerMessage(User user) {
        userId = user.getId();
    }
}

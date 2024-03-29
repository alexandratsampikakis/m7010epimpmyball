/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Jimmy
 */
@Serializable
public class LoginFailedMessage extends AbstractMessage {
    
    public LoginError error;
    
    public LoginFailedMessage() {
        error = LoginError.NO_ERROR;
    }
    
    public LoginFailedMessage(LoginError error) {
        this.error = error;
    }
}

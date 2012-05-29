
package mygame.admin.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Very safe login message sends the password in plaintext.
 * 
 * @author Jimmy
 */
@Serializable
public class FailedToRegisterMessage extends AbstractMessage {
    
    String error = "";
    
    public FailedToRegisterMessage() {
    }

    public FailedToRegisterMessage(String error) {
        this.error = error;
    }
}
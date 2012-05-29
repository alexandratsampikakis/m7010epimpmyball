
package mygame.admin.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Very safe login message sends the password in plaintext.
 * 
 * @author Jimmy
 */
@Serializable
public class RegisterUserMessage extends AbstractMessage {
    
    public String userName, password;

    public RegisterUserMessage() {
        userName = password = null;
    }

    public RegisterUserMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}
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
public class LoginMessage extends AbstractMessage {
    
    public String userName, password;

    public LoginMessage() {
        userName = password = null;
    }

    public LoginMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}

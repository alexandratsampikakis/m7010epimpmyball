/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.balls.UserData;


/**
 * @author Jimmy
 */
@Serializable
public class LoginSuccessMessage extends AbstractMessage {

    public ServerInfo serverInfo;
    public UserData userData;
    public int secret;
    
    public LoginSuccessMessage() {
    }
    
    public LoginSuccessMessage(UserData data, int secret, ServerInfo serverInfo) {
        this.userData = data;
        this.secret = secret;
        this.serverInfo = serverInfo;
    }
}


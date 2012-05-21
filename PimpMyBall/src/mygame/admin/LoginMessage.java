/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Very safe login message sends the password in plaintext.
 * This message is used for two different purposes since
 * I couldn't come a with a new class name :)
 * 
 * @author Jimmy
 */
@Serializable
public class LoginMessage extends AbstractMessage {
    
    public String userName, password;
    public int secret = 0; // public secret, haha..
    
    /**
     * Central server -> GameServer (BallServer)
     * @param secret 
     */
    public LoginMessage(int secret) {
        userName = password = null;
        this.secret = secret;
    }
    
    /**
     * Client -> CentralServer (and maybe CentralServer -> AuthServer)
     * 
     * @param userName
     * @param password 
     */
    public LoginMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}

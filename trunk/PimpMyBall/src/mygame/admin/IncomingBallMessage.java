/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.balls.UserData;

/**
 *  Sent from CentralServer to BallServer
 *
 * @author Jimmy
 */
@Serializable
public class IncomingBallMessage extends AbstractMessage {
    
    public UserData userData;
    public int secret;  // public secret, haha..

    public IncomingBallMessage() {
    }
    
    public IncomingBallMessage(UserData data, int secret) {
        this.userData = data;
        this.secret = secret;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Jimmy
 */
@Serializable
public class BallAcceptedMessage {

    public int secret;
    
    public BallAcceptedMessage() {
    }

    public BallAcceptedMessage(int secret) {
        this.secret = secret;
    }
}
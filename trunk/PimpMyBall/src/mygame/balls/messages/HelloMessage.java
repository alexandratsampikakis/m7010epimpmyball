/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class HelloMessage extends AbstractMessage {

    private long id;
    private long authCode;

    public HelloMessage() {
        setReliable(true);
    }
    
    public HelloMessage(long authCode, long id) {
        super();
        this.authCode = authCode;
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
    
    public long getid() {
        return id;
    }
    
    public long getAuthCode() {
        return authCode;
    }
}

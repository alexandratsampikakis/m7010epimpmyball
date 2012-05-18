/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class UserAddedMessage extends AbstractMessage {

    private long id;

    public UserAddedMessage() {
    }
    
    public UserAddedMessage(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
}

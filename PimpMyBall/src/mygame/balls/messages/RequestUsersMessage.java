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
public class RequestUsersMessage extends AbstractMessage {

    public long id;

    public RequestUsersMessage() {
        setReliable(true);

    }

    public RequestUsersMessage(long id) {
        super();
        this.id = id;
    }

    /*public long getId() {
        return this.id;
    }*/
}

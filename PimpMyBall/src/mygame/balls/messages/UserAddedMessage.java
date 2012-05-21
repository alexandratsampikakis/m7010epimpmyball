/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.balls.UserData;
import mygame.balls.client.User;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class UserAddedMessage extends AbstractMessage {

    private UserData userData;

    public UserAddedMessage() {
        setReliable(true);

    }

    public UserAddedMessage(UserData userData) {
        super();
        this.userData = userData;
    }

    public UserData getUserData() {
        return userData;
    }
}

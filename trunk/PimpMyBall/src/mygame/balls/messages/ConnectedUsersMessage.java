/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import mygame.balls.UserData;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class ConnectedUsersMessage extends AbstractMessage {

    public ArrayList<UserData> userDataList;

    public ConnectedUsersMessage() {
        setReliable(true);
    }

    public ConnectedUsersMessage(ArrayList<UserData> userData) {
        super();
        this.userDataList = userData;
    }

    /*public ArrayList<UserData> getUserDataList() {
        return userData;
    }*/
}

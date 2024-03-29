/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import mygame.balls.UserData;

/**
 *
 * @author Jimmy
 */
@Serializable
public class BackupDataMessage extends AbstractMessage {
    
    public ArrayList<UserData> data;
    
    public BackupDataMessage() {
    }
    
    public BackupDataMessage(ArrayList<UserData> data) {
        this.data = data;
    }
    
    public BackupDataMessage(UserData ud) {
        ArrayList<UserData> list = new ArrayList<UserData>();
        list.add(ud);
        this.data = list;
    }
}

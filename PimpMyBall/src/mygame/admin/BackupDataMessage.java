/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.admin;

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
}

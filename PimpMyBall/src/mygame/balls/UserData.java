/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class UserData {
    
    //private Vector3f latestCorrectPosition, latestCorrectVelocity;
    
    public long id;
    public String userName;
    public int rank;
    public int materialIndex;
    public Vector3f position;
    public long bling;

    public UserData() {
    }
    
    public UserData(long id) {
        this.id = id;
    }
    
    public long getId() {
        return id;
    }
    
    public String getUserName() {
        return userName;
    }

}
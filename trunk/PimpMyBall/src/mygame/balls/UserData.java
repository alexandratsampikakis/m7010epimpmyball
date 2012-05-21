/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;

/**
 *
 * @author nicnys-8
 */

public class UserData {
    //private Vector3f latestCorrectPosition, latestCorrectVelocity;
    private long id;
    private String userName;
    private int rank;
    private int materialIndex;
    private Vector3f position;
    private long bling;

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
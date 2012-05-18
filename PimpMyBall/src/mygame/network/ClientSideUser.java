/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;

/**
 *
 * @author nicnys-8
 */
public class ClientSideUser extends User {
    //private Vector3f latestCorrectPosition, latestCorrectVelocity;
    private Vector3f ghostDirection = new Vector3f();
    private RigidBodyControl ghostControl;

    public ClientSideUser(AssetManager assetManager, long id) {
        super(assetManager, id);
        SphereCollisionShape sphereShape = new SphereCollisionShape(radius);
        ghostControl = new RigidBodyControl(sphereShape, mass);
        ghostControl.setFriction(friction);
    }

    /*public void setLatestCorrectPosition(Vector3f latestPosition) {
        this.latestCorrectPosition = latestPosition;
    }
    
    public void setLatestCorrectVelocity(Vector3f latestVelocity) {
        this.latestCorrectVelocity = latestVelocity;
    }
    
    public Vector3f getLatestCorrectPosition() {
        return latestCorrectPosition;
    }

    public Vector3f getLatestCorrectVelocity() {
        return latestCorrectVelocity;
    }

    public Vector3f getDistanceToRealPosition() {
        return latestCorrectPosition.subtract(getPosition());
    }*/
    
    /*public void move() {
        // Replace this later!!!!!!!
        Vector3f currentToReal = new Vector3f();// getDistanceToRealPosition();
        float distanceAbs = currentToReal.length();
        // Move toward the correct position...
        if (distanceAbs > 0.5f) {
            Vector3f newVelocity = this.getVelocity().add(currentToReal);
            this.setVelocity(newVelocity);

            // ...or, if distance is too great, snap!
        } else if (distanceAbs > 5f) {
            //player.setPosition(player.getLatestCorrectPosition());
        }
    }*/
    
    public void setGhostData(Vector3f  position, Vector3f velocity, Vector3f direction) {
        ghostControl.setPhysicsLocation(position);
        ghostControl.setLinearVelocity(velocity);
        ghostDirection = direction;
    }
    
    public RigidBodyControl getGhostControl() {
        return ghostControl;
    }
    
    public void moveGhost() {
        ghostControl.applyCentralForce(ghostDirection.mult(20f));
        if (getVelocity().length() > 20) {
            setVelocity(getVelocity().normalize().mult(20f));
        }
    }
}
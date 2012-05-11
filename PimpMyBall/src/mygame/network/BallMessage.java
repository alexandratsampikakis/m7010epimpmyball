/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class BallMessage extends AbstractMessage {

    private Vector3f position = Vector3f.ZERO,
            velocity = Vector3f.ZERO, 
            acceleration = Vector3f.ZERO;

    public BallMessage() {
    }

    public BallMessage(Vector3f position, Vector3f velocity, Vector3f acceleration) {
        setPosition(position);
        setVelocity(velocity);
        setAcceleration(acceleration);
    }

     private void setPosition(Vector3f position) {
        this.position = position;
    }
     
     private void setVelocity(Vector3f Velocity) {
        this.velocity = Velocity;
    }
     
    private void setAcceleration(Vector3f acceleration) {
        this.acceleration = acceleration;
    }

    Vector3f getPosition() {
        return position;
    }
    
    Vector3f getVelocity() {
        return velocity;
    }
    
    Vector3f getAcceleration() {
        return acceleration;
    }
}

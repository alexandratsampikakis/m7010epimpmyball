/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network.messages;

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
    private long id = 0;

    public BallMessage() {
    }
    
    public BallMessage(long id, Vector3f position, Vector3f velocity, Vector3f direction) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = direction;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getDirection() {
        return acceleration;
    }

    public long getId() {
        return id;
    }
}

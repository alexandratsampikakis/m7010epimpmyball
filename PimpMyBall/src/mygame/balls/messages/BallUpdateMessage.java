/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.messages;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.balls.Ball;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class BallUpdateMessage extends AbstractMessage {

    private Vector3f position = Vector3f.ZERO,
            velocity = Vector3f.ZERO,
            acceleration = Vector3f.ZERO;
    private long id = 0;

    public BallUpdateMessage() {
        setReliable(false);
    }

    public BallUpdateMessage(Ball ball) {
        super();
        this.id = ball.getId();
        this.position = ball.getPosition();
        this.velocity = ball.getVelocity();
        this.acceleration = ball.getDirection();
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.messages;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mygame.balls.Ball;
import mygame.balls.BallUpdate;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class BallUpdateMessage extends AbstractMessage {

    public BallUpdate ballUpdate;

    public BallUpdateMessage() {
        setReliable(false);
    }

    public BallUpdateMessage(Ball ball) {
        super();
        this.ballUpdate = new BallUpdate(ball);
    }

    public Vector3f getPosition() {
        return ballUpdate.position;
    }

    public Vector3f getVelocity() {
        return ballUpdate.velocity;
    }

    public Vector3f getDirection() {
        return ballUpdate.direction;
    }

    public long getId() {
        return ballUpdate.id;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.messages;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class BallDirectionMessage extends AbstractMessage {

    public Vector3f direction = Vector3f.ZERO;
    public long id = 0;

    public BallDirectionMessage() {
        setReliable(false);
    }

    public BallDirectionMessage(long id, Vector3f direction) {
        super();
        this.id = id;
        this.direction = direction;
    }

    /*public Vector3f getDirection() {
        return direction;
    }

    public long getId() {
        return id;
    }*/
}

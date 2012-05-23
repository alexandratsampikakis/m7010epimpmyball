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
public class BallUpdate {
    
    public long id;
    public Vector3f position;
    public Vector3f velocity;
    public Vector3f direction;
       
    public BallUpdate() {
    }
    
    public BallUpdate(Ball ball) {
        this.id = ball.getId();
        this.position = ball.getPosition();
        this.velocity = ball.getVelocity();
        this.direction = ball.getDirection();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.server;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import mygame.balls.Ball;

/**
 *
 * @author nicnys-8
 */
public class BallCollisionListener implements PhysicsCollisionListener {

    public void collision(PhysicsCollisionEvent event) {
        Object a = event.getObjectA();
        Object b = event.getObjectB();
        if (a instanceof Ball && b instanceof Ball) {
            Ball ballA = (Ball) a;
            Ball ballB = (Ball) b;
            
        }
    }
}

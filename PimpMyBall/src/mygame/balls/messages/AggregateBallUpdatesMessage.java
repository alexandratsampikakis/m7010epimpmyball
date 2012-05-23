/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.messages;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import mygame.balls.Ball;
import mygame.balls.BallUpdate;

/**
 *
 * @author nicnys-8
 */
@Serializable
public class AggregateBallUpdatesMessage extends AbstractMessage {

    public ArrayList<BallUpdate> ballUpdates;

    public AggregateBallUpdatesMessage() {
    }

    public AggregateBallUpdatesMessage(ArrayList<Ball> balls) {
        super();
        ballUpdates = new ArrayList<BallUpdate>();
        BallUpdate update;
        for (Ball ball : balls) {
            update = new BallUpdate(ball);
            ballUpdates.add(update);
        }
    }
}

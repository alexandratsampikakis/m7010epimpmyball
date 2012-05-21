/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.server;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.HostedConnection;
import com.jme3.scene.Node;
import mygame.balls.Level;
import mygame.balls.TestLevel;
import mygame.balls.UserData;
import mygame.util.BiMap;

/**
 *
 * @author nicnys-8
 */
public class ServerGame {
    private BiMap<Long, User> users = new BiMap<Long, User>();
    private Level level;
    private BulletAppState bulletAppState;
    
    private void setupUser(AssetManager assetManager, UserData userData, HostedConnection conn) {
        long callerId = userData.getId();
        User user = new User(assetManager, userData, conn);
        users.put(callerId, user);

        level.attachChild(user.getGeometry());
        bulletAppState.getPhysicsSpace().add(user.getBall());
    }

    private void initAppState(AppStateManager stateManager) {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);

        BallCollisionListener collisionListener = new BallCollisionListener();
        bulletAppState.getPhysicsSpace().addCollisionListener(collisionListener);
    }
    

    private void initLevel(AssetManager assetManager, Node node) {
        level = new TestLevel(assetManager);
        node.attachChild(level);
        level.initLighting(); //Kasta sen!!!
        bulletAppState.getPhysicsSpace().add(level.getTerrain());
    }
    
}

package mygame.balls.client;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.ShadowUtil;
import mygame.balls.Ball;
import mygame.balls.TestLevel;
import mygame.balls.UserData;
import mygame.util.BiMap;


/**
 *
 * @author nicnys-8
 */
public class BallGame extends SimpleApplication{

    @Override
    public void simpleInitApp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

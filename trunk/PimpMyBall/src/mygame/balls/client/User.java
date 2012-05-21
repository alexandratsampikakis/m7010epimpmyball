/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.client;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import mygame.balls.Ball;
import mygame.balls.UserData;

/**
 *
 * @author nicnys-8
 */
public class User {

    private UserData userData;
    private Ball ball;
    private Ball ghost;
    private Geometry geometry;
    private long id;

    public User(AssetManager assetManager, UserData userData) {
        this.userData = userData;
        id = userData.getId();
        ball = new Ball(assetManager, id);
        ghost = new Ball(assetManager, id);
        ball.setPosition(userData.position);
        ghost.setPosition(userData.position);
        addGeometry(assetManager);
    }
    
    private void addGeometry(AssetManager assetManager) {
        float radius = 2f;
        int samples = 25;
        geometry = new Geometry("PlayerGeometry", new Sphere(samples, samples, radius));
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/nickeFace.png"));
        geometry.setMaterial(material);
        geometry.addControl(ball);
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Ball getBall() {
        return ball;
    }

    public Ball getGhost() {
        return ghost;
    }
    
    public long getId() {
        return id;
    }
}

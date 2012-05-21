/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.client;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
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
        addGeometry(assetManager);
    }

    private void addGeometry(AssetManager assetManager) {
        float radius = 2f;
        int samples = 25;
        geometry = new Geometry("PlayerGeometry", new Sphere(samples, samples, radius));
        geometry.setMaterial(getRedMaterial(assetManager));
        
        /*
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/nickeFace.png"));
        */
        
        geometry.addControl(ball);
    }

    public void makeBlue(AssetManager assetManager) {
        geometry.setMaterial(getBlueMaterial(assetManager));
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

    public Material getBlueMaterial(AssetManager assetManager) {
        Material matBlue = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matBlue.setColor("Diffuse", ColorRGBA.Blue);
        matBlue.setColor("Ambient", ColorRGBA.Blue.mult(0.3f));
        matBlue.setColor("Specular", ColorRGBA.White.mult(0.6f));
        matBlue.setFloat("Shininess", 24f);
        matBlue.setBoolean("UseMaterialColors", true);
        return matBlue;
    }

    public Material getRedMaterial(AssetManager assetManager) {
        Material matRed = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matRed.setColor("Diffuse", ColorRGBA.Red);
        matRed.setColor("Ambient", ColorRGBA.Red.mult(0.3f));
        matRed.setColor("Specular", ColorRGBA.White.mult(0.6f));
        matRed.setFloat("Shininess", 24f);
        matRed.setBoolean("UseMaterialColors", true);
        return matRed;
    }
}

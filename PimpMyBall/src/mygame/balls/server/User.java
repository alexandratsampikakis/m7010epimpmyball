/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.server;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.network.HostedConnection;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import mygame.balls.Ball;
import mygame.balls.UserData;
import mygame.util.GridPoint;

/**
 *
 * @author nicnys-8
 */
public class User {

    private UserData userData;
    private Ball ball;
    private Geometry geometry;
    private HostedConnection connection;
    private long id;
    private GridPoint currentArea;
    private float immortality;
    private float maxImmortalityTime = 5f;


    public User(AssetManager assetManager, UserData userData, HostedConnection connection) {
        this.userData = userData;
        id = userData.getId();
        ball = new Ball(assetManager, id);
        this.connection = connection;
        addGeometry(assetManager);
        immortality = maxImmortalityTime;
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
 
    public void update(float tpf) {
        ball.moveForward();
        if (immortality > 0) immortality -= tpf;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Ball getBall() {
        return ball;
    }

    public HostedConnection getConnection() {
        return connection;
    }

    public long getId() {
        return id;
    }

    public UserData getUserData() {
        return userData;
    }
    
    public GridPoint getCurrentArea() {
        return currentArea;
    }
    
    public void setCurrentArea(GridPoint p) {
        currentArea = p;
    }
    
    public boolean canPlay() {
        return (ball.getMass() > 0 && immortality <= 0);
    }
    
    public void setImmortal() {
        immortality = maxImmortalityTime;
    }
}

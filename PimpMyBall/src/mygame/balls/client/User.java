/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.client;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Sphere;
import mygame.balls.Ball;
import mygame.balls.UserData;
import mygame.boardgames.GridPoint;

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
    private Node blingNode;

    public User(AssetManager assetManager, UserData userData) {
        this.userData = userData;
        id = userData.getId();
        ball = new Ball(assetManager, id);
        ghost = new Ball(assetManager, id);
        blingNode = new Node();
        addGeometry(assetManager);
        setupUserNameText(assetManager);
    }

    private void addGeometry(AssetManager assetManager) {
        float radius = 2f;
        int samples = 25;
        geometry = new Geometry("PlayerGeometry", new Sphere(samples, samples, radius));
        geometry.setMaterial(getRedMaterial(assetManager));
        geometry.addControl(ball);
    }

    public void makeBlue(AssetManager assetManager) {
        geometry.setMaterial(getBlueMaterial(assetManager));
    }

    public UserData getUserData() {
        return userData;
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

    public Node getBlingNode() {
        return blingNode;
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

    public void update() {
        ball.moveForward();
        ghost.moveForward();
        ball.adjustToBall(ghost);
        blingNode.setLocalTranslation(ball.getPosition());
    }

    public void setFrozen(boolean bool) {
        ball.setFrozen(bool);
        ghost.setFrozen(bool);
    }
    
    private void setupUserNameText(AssetManager assetManager) {
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/HelveticaNeue.fnt");
        BitmapText userNameText = new BitmapText(guiFont, false);
        userNameText.setSize(1);
        userNameText.setText(userData.userName);
        userNameText.setColor(ColorRGBA.Red);

        userNameText.setQueueBucket(Bucket.Transparent);
        Node textNode = new Node();
        textNode.addControl(new BillboardControl());
        textNode.attachChild(userNameText);
        blingNode.attachChild(textNode);
        float xOffset = userNameText.getLineWidth() * -0.5f;
        float yOffset = 3.5f;
        userNameText.setLocalTranslation(new Vector3f(xOffset, yOffset, 0f));
    }    
}

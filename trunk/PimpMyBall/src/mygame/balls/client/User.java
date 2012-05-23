/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls.client;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.font.Rectangle;
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

    private static int NUM_CHAT_LINES = 3;
    private BitmapFont guiFont;
    private BitmapText[] chatLines = new BitmapText[NUM_CHAT_LINES];
    private Node chatNode;
    private int removeChatDelay = 0;
    
    public User(AssetManager assetManager, UserData userData) {
        this.userData = userData;
        id = userData.getId();
        ball = new Ball(assetManager, id);
        ghost = new Ball(assetManager, id);
        blingNode = new Node();
        addGeometry(assetManager);
        
        guiFont = assetManager.loadFont("Interface/Fonts/HelveticaNeue.fnt");
        
        setupUserNameText(assetManager);
        
        chatNode = new Node();
        chatNode.addControl(new BillboardControl());
        
        float yOffset = 4.5f;
        
        for (int i = 0; i < NUM_CHAT_LINES; i++, yOffset += 1.5f) {
            BitmapText chatLine = new BitmapText(guiFont, false);
            chatLine.setSize(1);
            chatLine.setText("");
            chatLine.setColor(ColorRGBA.White);
            chatLine.setQueueBucket(Bucket.Transparent);
            chatLine.setLocalTranslation(new Vector3f(0, yOffset, 0f));
            /*
            chatLine.setBox(new Rectangle(0, 0, 800, 600));
            chatLine.setLineWrapMode(LineWrapMode.Word);
            chatLine.setAlignment(BitmapFont.Align.Center);
            chatLine.setVerticalAlignment(BitmapFont.VAlign.Center);
             */
            chatNode.attachChild(chatLine);
        
            chatLines[i] = chatLine;
        }
  
        blingNode.attachChild(chatNode);        
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
        
        if (removeChatDelay > 0) {
            removeChatDelay--;
            if (removeChatDelay == 0) {
                chatLines[removeChatIndex--].setText("");
                
                if (removeChatIndex >= 0) {
                    removeChatDelay = 200;
                }
            }
        }
    }

    public void setFrozen(boolean bool) {
        ball.setFrozen(bool);
        ghost.setFrozen(bool);
    }
    
    int removeChatIndex = -1;
    
    public void showChatMessage(String text) {
        
        for (int i = NUM_CHAT_LINES - 1; i > 0; i--) {
            chatLines[i].setText(chatLines[i - 1].getText());
        }
        
        chatLines[0].setText(text);
        
        removeChatDelay = 200;
        removeChatIndex = Math.min(removeChatIndex + 1, NUM_CHAT_LINES - 1);
    }
    
    private void setupUserNameText(AssetManager assetManager) {
        
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

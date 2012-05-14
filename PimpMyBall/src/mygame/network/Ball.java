/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.network;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author nicnys-8
 */
public class Ball extends Node {
    private Geometry geometry;
    private RigidBodyControl control;
    
    public Ball(AssetManager assetManager) {
        float radius = 2;
        geometry = new Geometry("PlayerGeometry", new Sphere(25, 25, radius));
        this.attachChild(geometry);
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/nickeFace.png"));
        geometry.setMaterial(material);
        SphereCollisionShape sphereShape = new SphereCollisionShape(radius);
        float stepHeight = radius * 0.3f;
        control = new RigidBodyControl(sphereShape, 1f);
        geometry.addControl(control);
        //playerControl.setRestitution(0.001f);
        control.setFriction(12f);
        this.setShadowMode(ShadowMode.CastAndReceive);
    }
    
    public RigidBodyControl getControl() {
        return control;
    }

    Geometry getGeometry() {
        return geometry;
    }
}

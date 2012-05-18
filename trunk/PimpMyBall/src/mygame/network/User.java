package mygame.network;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author nicnys-8
 */
public class User {

    long id;
    private Vector3f direction = new Vector3f();
    protected Geometry ballGeometry;
    private RigidBodyControl ballControl;
    private Node ball;
    float radius = 2;
    protected static int samples = 25;
    protected float friction = 12f, mass = 1f;

    public User(AssetManager assetManager, long id) {
        initBall();
        this.id = id;
        setUpMaterial(assetManager);

    }

    public RigidBodyControl getControl() {
        return ballControl;
    }

    public Geometry getGeometry() {
        return ballGeometry;
    }

    public long getId() {
        return id;
    }

    Vector3f getPosition() {
        return ballGeometry.getWorldTranslation();
    }

    Vector3f getVelocity() {
        return ballControl.getLinearVelocity();
    }

    void setPosition(Vector3f newPosition) {
        ballControl.setPhysicsLocation(newPosition);
    }

    void setVelocity(Vector3f newVelocity) {
        ballControl.setLinearVelocity(newVelocity);
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    private void moveInDirection(Vector3f walkDirection) {
        ballControl.applyCentralForce(walkDirection.mult(20f));
        if (getVelocity().length() > 20) {
            setVelocity(getVelocity().normalize().mult(20f));
        }
    }

    public void moveForward() {
        moveInDirection(direction);
    }

    private void initBall() {
        SphereCollisionShape sphereShape = new SphereCollisionShape(radius);
        ball = new Node();
        ballGeometry = new Geometry("PlayerGeometry", new Sphere(samples, samples, radius));
        ball.attachChild(ballGeometry);
        ballControl = new RigidBodyControl(sphereShape, mass);
        ballControl.setFriction(friction);
        ballGeometry.addControl(ballControl);
        ball.setShadowMode(ShadowMode.CastAndReceive);
    }
    
    private void setUpMaterial(AssetManager assetManager) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/nickeFace.png"));
        ballGeometry.setMaterial(material);
    }
}

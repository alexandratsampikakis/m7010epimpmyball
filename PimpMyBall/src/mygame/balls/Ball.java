package mygame.balls;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;

/**
 *
 * @author nicnys-8
 */
public class Ball extends RigidBodyControl {

    private Vector3f direction = new Vector3f();
    static float radius = 2;
    protected static int samples = 25;
    static protected float friction = 25f, ballMass = 1f, maxSpeed = 20f;
    private long id;

    public Ball(AssetManager assetManager, long id) {
        super(new SphereCollisionShape(radius), ballMass);
        setFriction(friction);
        this.id = id;
    }

    public Vector3f getPosition() {
        return getPhysicsLocation();
    }

    public Vector3f getVelocity() {
        return getLinearVelocity();
    }

    public void setPosition(Vector3f newPosition) {
        setPhysicsLocation(newPosition);
    }

    public void setVelocity(Vector3f newVelocity) {
        setLinearVelocity(newVelocity);
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    private void moveInDirection(Vector3f walkDirection) {
        Vector3f currentVelocity = getVelocity();
        clearForces();
        applyCentralForce(walkDirection.mult(20f));
        if (currentVelocity.length() > maxSpeed) {
            setVelocity(currentVelocity.normalize().mult(maxSpeed));
        }
    }

    public void moveForward() {
        moveInDirection(direction);
    }

    public void adjustToBall(Ball adjustor) {
        // If too far, oh snap!
        Vector3f adjustPosition = adjustor.getPosition();
        Vector3f distanceVector = adjustPosition.subtract(getPosition());
        if (distanceVector.length() > 5) { //Randomly chosen, fix this later!!
            setPosition(adjustPosition);
        } else if (distanceVector.length() > 1) {
            applyCentralForce(distanceVector.normalize().mult(20f));
        }
    }
}

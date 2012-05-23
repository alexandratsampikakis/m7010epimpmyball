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
    
    public static final float friction = 25f, defaultMass = 1f, maxSpeed = 20f;
    protected static float radius = 2;
    protected static int samples = 25;
    
    private long id;

    public Ball(AssetManager assetManager, long id) {
        super(new SphereCollisionShape(radius), defaultMass);
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
        
        if (mass <= 0)
            return;
        
        Vector3f currentVelocity = getVelocity();
        clearForces();
        if (walkDirection.equals(Vector3f.ZERO)) {
            Vector3f frictionForce = getVelocity().clone();
            frictionForce = frictionForce.negate();
            frictionForce.y = 0f;
            applyCentralForce(frictionForce);
        } else {
            applyCentralForce(walkDirection.mult(20f));
            if (currentVelocity.length() > maxSpeed) {
                setVelocity(currentVelocity.normalize().mult(maxSpeed));
            }
        }
    }

    public void moveForward() {
        moveInDirection(direction);
    }

    public void adjustToBall(Ball adjustor) {
        Vector3f adjustmentVector = adjustor.getPosition().subtract(getPosition());
        adjustmentVector = adjustmentVector.mult(0.1f);
        Vector3f newPosition = getPosition().add(adjustmentVector);
        setPosition(newPosition);
    }
}

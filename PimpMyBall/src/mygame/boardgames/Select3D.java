
package boardgames;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

/**
 *
 * @author Jimmy
 */
public class Select3D {
    
    private static CollisionResults results = new CollisionResults();
    
    public static CollisionResult select(Camera cam, Node n, float maxDistance) {
        
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        CollisionResult closest = Select3D.select(ray, n);
        
        if (closest != null && closest.getDistance() < maxDistance) {
            return closest;
        }
        
        return null;
    }
    
    public static CollisionResult select(Camera cam, Node n) {
        
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        
        return Select3D.select(ray, n);
    }

    public static CollisionResult select(Vector2f click2d, Camera cam, Node n) {
        Vector3f click3d = cam.getWorldCoordinates(
                new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(
                new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
        Ray ray = new Ray(click3d, dir);
        
        return Select3D.select(ray, n);
    }
     
    public static CollisionResult select(Ray ray, Node n) {
        
        // 1. Reset results list.
        results.clear();

        // 2. Collect intersections between Ray and Shootables in results list.
        n.collideWith(ray, results);

        // 3. Check the result
        if (results.size() > 0) {
            // The closest collision point is what was truly hit:
            return results.getClosestCollision();
        }
        
        return null;
    }
}

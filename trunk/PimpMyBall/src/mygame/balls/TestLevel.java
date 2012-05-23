/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

/**
 *
 * @author nicnys-8
 */
public final class TestLevel extends Node {

    private Spatial tree;
    private RigidBodyControl treeControl;
    private BoxCollisionShape wallShape = new BoxCollisionShape(new Vector3f(512, 512, 512));
    private TerrainQuad terrain;

    public TestLevel(AssetManager assetManager, BulletAppState appState) {
        setUpTerrain(assetManager);
        appState.getPhysicsSpace().add(terrain);
        addWalls(appState);
        //initTrees(assetManager, appState);
    }

    final protected void setUpTerrain(AssetManager assetManager) {
        Material matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex1", grass);
        matRock.setFloat("Tex1Scale", 64f);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex2", dirt);
        matRock.setFloat("Tex2Scale", 32f);
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", 128f);
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
            heightmap.load();
        } catch (Exception e) {
        }

        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        terrain.setMaterial(matRock);
        terrain.setLocalScale(new Vector3f(2, 2, 2));
        terrain.setLocked(false); // unlock it so we can edit the height
        attachChild(terrain);
        terrain.addControl(new RigidBodyControl(0));
        terrain.setShadowMode(ShadowMode.CastAndReceive);
        terrain.getControl(RigidBodyControl.class).setLinearDamping(5);
        terrain.getControl(RigidBodyControl.class).setAngularDamping(5);
    }

    public void addWalls(BulletAppState bas) {
        addWall(new Vector3f(1024, 0, 0), bas);
        addWall(new Vector3f(0, 0, 1024), bas);
        addWall(new Vector3f(-1024, 0, 0), bas);
        addWall(new Vector3f(0, 0, -1024), bas);
    }

    private void addWall(Vector3f position, BulletAppState bas) {
        RigidBodyControl wall = new RigidBodyControl(wallShape);
        wall.setPhysicsLocation(position);
        wall.setMass(0);
        bas.getPhysicsSpace().add(wall);
    }

    public void initTrees(AssetManager assetManager, BulletAppState bulletAppState) {
        createTree(-300, 500, 7, 100, 2, assetManager, bulletAppState);
        createTree(-210, 315, 5, 50, 2, assetManager, bulletAppState);
        createTree(-20, 130, 5, 50, 5, assetManager, bulletAppState);
        createTree(-50, 0, 10, 250, 5, assetManager, bulletAppState);
        createTree(-150, 10, 10, 0, 6, assetManager, bulletAppState);
        createTree(-100, 150, 15, 100, 5, assetManager, bulletAppState);
        createTree(-250, 200, 8, 200, 3, assetManager, bulletAppState);
    }

    public void createTree(float xTree, float zTree, float sTree, float rTree,
            int toHigherTree, AssetManager assetManager, BulletAppState bulletAppState) {

        tree = assetManager.loadModel("Models/Tree/Tree.mesh.xml");
        Vector2f xz = new Vector2f(xTree, zTree);
        float yTree = terrain.getHeightmapHeight(xz) + toHigherTree;
        tree.setLocalTranslation(xTree, yTree, zTree);
        tree.scale(sTree);
        tree.rotate(0, rTree, 0);

        CollisionShape treeCollisionShape = CollisionShapeFactory.createMeshShape((Node) tree);
        treeControl = new RigidBodyControl(treeCollisionShape, 0);
        tree.addControl(treeControl);
        terrain.attachChild(tree);
        bulletAppState.getPhysicsSpace().add(treeControl);
    }
    
        public void initGraphics(AssetManager assetManager) {
        // Sky
        initSky(assetManager);
        initLight();
    }

    public void initLight() {
        // Create directional light
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-0.5f, -.5f, -.5f).normalizeLocal());
        directionalLight.setColor(new ColorRGBA(0.50f, 0.50f, 0.50f, 1.0f));
        addLight(directionalLight);
        //Create ambient light
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor((ColorRGBA.White).mult(2.5f));
        addLight(ambientLight);
    }
    
    final protected void initSky(AssetManager assetManager) {
        attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
    }
}
